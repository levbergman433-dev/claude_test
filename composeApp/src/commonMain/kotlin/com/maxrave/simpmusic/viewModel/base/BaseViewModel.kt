package com.maxrave.simpmusic.viewModel.base

import com.maxrave.domain.repository.ListenTogetherRepository
import com.maxrave.domain.data.model.listentogether.RoomControlCommand
import com.maxrave.domain.data.model.listentogether.RoomTrack
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.domain.utils.connectArtists
import com.maxrave.domain.utils.toListName
import simpmusic.composeapp.generated.resources.lt_playing_for_room
import simpmusic.composeapp.generated.resources.lt_suggestion_sent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxrave.domain.extension.now
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.StreamRepository
import com.maxrave.logger.LogLevel
import com.maxrave.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.loading

private const val PREFETCH_TRACKS = 2

abstract class BaseViewModel :
    ViewModel(),
    KoinComponent {
    protected val mediaPlayerHandler: MediaPlayerHandler by inject<MediaPlayerHandler>()
    private val listenTogether: ListenTogetherRepository by inject<ListenTogetherRepository>()
    private val prefetchStreamRepository: StreamRepository by inject<StreamRepository>()
    private val prefetchDataStoreManager: DataStoreManager by inject<DataStoreManager>()

    /**
     * Resolve the stream links of the first few [videoIds] in the background, so the tap that most
     * likely follows opening a page (Play, or the top track) starts from a cached link instead of a
     * fresh extraction. A track whose cached link is still valid is skipped; the whole thing is
     * gated on the "Fast song loading" setting because it spends data on songs that may never play.
     */
    protected fun prefetchStreams(videoIds: List<String>) {
        val ids = videoIds.filter { it.isNotBlank() }.distinct().take(PREFETCH_TRACKS)
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            if (prefetchDataStoreManager.fastStreamLoading.first() != DataStoreManager.TRUE) return@launch
            // Battery saver means "spend nothing on songs that may never play".
            if (prefetchDataStoreManager.batterySaver.first() == DataStoreManager.TRUE) return@launch
            for (id in ids) {
                val cached = prefetchStreamRepository.getNewFormat(id).firstOrNull()
                if (cached?.audioUrl != null && cached.expiredTime > now()) continue
                runCatching {
                    prefetchStreamRepository
                        .getStream(prefetchDataStoreManager, id, isDownloading = false, isVideo = false)
                        .firstOrNull()
                }
            }
        }
    }
    private val _nowPlayingVideoId: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * Get now playing video id
     * If empty, no video is playing
     */
    val nowPlayingVideoId: StateFlow<String> get() = _nowPlayingVideoId

    /**
     * Tag for logging
     */
    protected val tag: String = this::class.simpleName ?: "BaseViewModel"

    /**
     * Log with viewModel tag
     */
    protected fun log(
        message: String,
        logType: LogLevel = LogLevel.WARN,
    ) {
        when (logType) {
            LogLevel.DEBUG -> Logger.d(tag, message)
            LogLevel.INFO -> Logger.i(tag, message)
            LogLevel.WARN -> Logger.w(tag, message)
            LogLevel.ERROR -> Logger.e(tag, message)
        }
    }

    /**
     * Cancel all jobs
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        log("ViewModel cleared", LogLevel.WARN)
    }

    init {
        getNowPlayingVideoId()
    }

    fun makeToast(message: String?) {
        showToast(
            message = message ?: "NO MESSAGE",
            duration = ToastDuration.Short,
            gravity = ToastGravity.Bottom,
        )
    }

    protected fun getString(resId: StringResource): String =
        runBlocking {
            org.jetbrains.compose.resources
                .getString(resId)
        }

    // Loading dialog
    private val _showLoadingDialog: MutableStateFlow<Pair<Boolean, String>> = MutableStateFlow(false to getString(Res.string.loading))
    val showLoadingDialog: StateFlow<Pair<Boolean, String>> get() = _showLoadingDialog

    fun showLoadingDialog(message: String? = null) {
        _showLoadingDialog.value = true to (message ?: getString(Res.string.loading))
    }

    fun hideLoadingDialog() {
        _showLoadingDialog.value = false to getString(Res.string.loading)
    }

    private fun getNowPlayingVideoId() {
        viewModelScope.launch {
            combine(mediaPlayerHandler.nowPlayingState, mediaPlayerHandler.controlState) { nowPlayingState, controlState ->
                Pair(nowPlayingState, controlState)
            }.collect { (nowPlayingState, controlState) ->
                if (controlState.isPlaying) {
                    _nowPlayingVideoId.value = nowPlayingState.songEntity?.videoId ?: ""
                } else {
                    _nowPlayingVideoId.value = ""
                }
            }
        }
    }

    /**
     * Communicate with SimpleMediaServiceHandler to load media item
     */
    fun setQueueData(queueData: QueueData.Data) {
        // A guest's player follows the room's queue; replacing it here would only be undone by the
        // next room update, after cutting the music for everyone's-song-but-yours. See loadMediaItem.
        if (isRoomGuest()) return
        mediaPlayerHandler.reset()
        mediaPlayerHandler.setQueueData(queueData)
    }

    private fun isRoomGuest(): Boolean = listenTogether.room.value.let { it.inRoom && !it.isHost }

    /**
     * A guest in a Listen Together room picking a song: instead of playing it on their own (and
     * falling out of sync), it goes to the room — played straight away when the host has given
     * this guest controls, otherwise as a suggestion the host can approve.
     */
    private fun <T> sendToRoom(anyTrack: T): Boolean {
        if (!isRoomGuest()) return false
        val track =
            when (anyTrack) {
                is Track -> anyTrack
                is SongEntity -> anyTrack.toTrack()
                else -> null
            } ?: return true
        listenTogether.sendControl(
            command = RoomControlCommand.PLAY_NOW,
            track =
                RoomTrack(
                    id = track.videoId,
                    title = track.title,
                    artist = track.artists?.toListName()?.connectArtists().orEmpty(),
                    durationMs = (track.durationSeconds ?: 0) * 1000L,
                    thumbnail = track.thumbnails?.lastOrNull()?.url.orEmpty(),
                ),
        )
        viewModelScope.launch {
            makeToast(
                getString(
                    if (listenTogether.room.value.hasControl) Res.string.lt_playing_for_room else Res.string.lt_suggestion_sent,
                ),
            )
        }
        return true
    }

    fun <T> loadMediaItem(
        anyTrack: T,
        type: String,
        index: Int? = null,
    ) {
        if (sendToRoom(anyTrack)) return
        viewModelScope.launch {
            mediaPlayerHandler.loadMediaItem(
                anyTrack = anyTrack,
                type = type,
                index = index,
            )
        }
    }

    fun shufflePlaylist(firstPlayIndex: Int = 0) {
        mediaPlayerHandler.shufflePlaylist(firstPlayIndex)
    }
}