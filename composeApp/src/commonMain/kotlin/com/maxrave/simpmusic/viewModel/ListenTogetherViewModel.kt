package com.maxrave.simpmusic.viewModel

import com.maxrave.simpmusic.ui.theme.PersonalizationKeys
import com.maxrave.domain.data.model.listentogether.RoomConnection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxrave.data.listentogether.ListenTogetherPlaybackBridge
import com.maxrave.data.listentogether.ListenTogetherPrefs
import com.maxrave.domain.data.model.listentogether.ListenTogetherRoom
import com.maxrave.domain.repository.ListenTogetherRepository
import com.maxrave.domain.manager.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Screen state for Listen Together.
 *
 * The session owns everything the server says; this holds only what the user has typed but not yet
 * sent, so a half-filled room code survives a recomposition without being confused for room state.
 */
class ListenTogetherViewModel(
    private val repository: ListenTogetherRepository,
    private val dataStore: DataStoreManager,
    bridge: ListenTogetherPlaybackBridge,
) : ViewModel() {
    init {
        // The bridge is a singleton that outlives this screen; starting it here is simply the
        // first moment anything asks for it. Koin's `createdAtStart` does not fire for modules
        // added with loadKoinModules, so without an explicit injection it is never constructed
        // at all — and then a room syncs membership while no audio follows anyone.
        bridge.start()

        // Host conveniences live in settings but are applied by the session, so they are mirrored
        // onto it for as long as this screen exists.
        viewModelScope.launch {
            dataStore.getString(ListenTogetherPrefs.AUTO_APPROVE_JOINS).collect {
                repository.autoApproveJoins = it == ListenTogetherPrefs.TRUE
            }
        }
        viewModelScope.launch {
            dataStore.getString(ListenTogetherPrefs.AUTO_APPROVE_SUGGESTIONS).collect {
                repository.autoApproveSuggestions = it == ListenTogetherPrefs.TRUE
            }
        }
    }

    val state: StateFlow<ListenTogetherRoom> =
        repository.room.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListenTogetherRoom())

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    init {
        // The name is remembered: typing it before every room was the first chore of the feature.
        // First time round it starts from the profile badge name, when one is set.
        viewModelScope.launch {
            val saved = dataStore.getString(KEY_DISPLAY_NAME).first()
            val badge = dataStore.getString(PersonalizationKeys.BADGE_NAME).first()
            if (_displayName.value.isEmpty()) {
                _displayName.value = (saved?.takeIf { it.isNotBlank() } ?: badge.orEmpty()).take(MAX_USERNAME_LENGTH)
            }
        }
        // Connected the moment the screen is opened, rather than behind a Connect button.
        if (repository.room.value.connection !is RoomConnection.Connected &&
            repository.room.value.connection !is RoomConnection.Connecting
        ) {
            repository.connect()
        }
    }

    private var pendingInvite: String? = null

    /**
     * Opened from an invite link: fill the code in and join as soon as there is a connection and a
     * name. Without a name the code just waits in the field for the user to press Join.
     */
    fun handleInvite(code: String?) {
        val clean = code?.let(::extractRoomCode) ?: return
        if (repository.room.value.inRoom) return
        _roomCodeInput.value = clean
        pendingInvite = clean
        viewModelScope.launch {
            repository.room.first { it.isConnected }
            // Wait for the remembered name to load (it is read asynchronously in init).
            kotlinx.coroutines.delay(300)
            if (pendingInvite == clean && _displayName.value.isNotBlank() && !repository.room.value.inRoom) {
                pendingInvite = null
                joinRoom()
            }
        }
    }

    private val _roomCodeInput = MutableStateFlow("")
    val roomCodeInput: StateFlow<String> = _roomCodeInput.asStateFlow()

    fun onDisplayNameChange(value: String) {
        // metroserver caps usernames at 50; trimming here means the server never has to reject it.
        _displayName.value = value.take(MAX_USERNAME_LENGTH)
        viewModelScope.launch { dataStore.putString(KEY_DISPLAY_NAME, _displayName.value) }
    }

    fun onRoomCodeChange(value: String) {
        // A pasted invite message is accepted whole: the code is picked out of it, instead of the
        // message's first eight letters ("JOINMYRO…") ending up in the field.
        _roomCodeInput.value = extractRoomCode(value) ?: value.uppercase().filter { it.isLetterOrDigit() }.take(ROOM_CODE_LENGTH)
    }

    fun connect() = repository.connect()

    fun disconnect() = repository.disconnect()

    fun createRoom() {
        repository.createRoom(_displayName.value)
    }

    fun joinRoom() {
        repository.joinRoom(_roomCodeInput.value, _displayName.value)
    }

    fun leaveRoom() {
        repository.leaveRoom()
        _roomCodeInput.value = ""
    }

    fun approveJoin(userId: String) {
        repository.approveJoin(userId)
    }

    fun rejectJoin(userId: String) {
        repository.rejectJoin(userId)
    }

    fun approveSuggestion(id: String) {
        repository.approveSuggestion(id)
    }

    fun rejectSuggestion(id: String) {
        repository.rejectSuggestion(id)
    }

    fun kickUser(userId: String) {
        repository.kickUser(userId)
    }

    /**
     * Blocks by NAME and then kicks.
     *
     * The protocol has no ban and the server mints a new user id per connection, so the id cannot
     * be blocked on — the name is the only thing that survives a reconnect, and it is changeable.
     * A convenience, not a security control.
     */
    fun blockAndKick(
        userId: String,
        username: String,
    ) {
        viewModelScope.launch {
            val current =
                dataStore
                    .getString(ListenTogetherPrefs.BLOCKLIST)
                    .first()
                    .orEmpty()
                    .split(ListenTogetherPrefs.BLOCKLIST_SEPARATOR)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            if (current.none { it.equals(username, ignoreCase = true) }) {
                dataStore.putString(
                    ListenTogetherPrefs.BLOCKLIST,
                    (current + username).joinToString(ListenTogetherPrefs.BLOCKLIST_SEPARATOR),
                )
            }
            repository.kickUser(userId)
        }
    }

    fun transferHost(userId: String) {
        repository.transferHost(userId)
    }

    /** Host: lets a member control playback (or takes it back) without handing over the room. */
    fun setControl(
        userId: String,
        allowed: Boolean,
    ) = repository.setControl(userId, allowed)

    /** Guest: a transport button, carried out by the host's app when it has given us control. */
    fun control(command: String) = repository.sendControl(command)

    fun cancelJoin() = repository.cancelJoin()

    fun clearError() = repository.clearError()

    companion object {
        private const val KEY_DISPLAY_NAME = "listen_together_display_name"

        /**
         * The room code inside [text]: after `code=` in an invite link, else the last standalone
         * 8-character run of letters and digits (the code at the end of a shared message), else
         * null when [text] is not longer than a code and should be read as typed.
         */
        fun extractRoomCode(text: String): String? {
            Regex("""code=([A-Za-z0-9]{$ROOM_CODE_LENGTH})""").find(text)?.let { return it.groupValues[1].uppercase() }
            if (text.length <= ROOM_CODE_LENGTH) return null
            return Regex("""(?<![A-Za-z0-9])([A-Za-z0-9]{$ROOM_CODE_LENGTH})(?![A-Za-z0-9])""")
                .findAll(text)
                .lastOrNull()
                ?.groupValues
                ?.get(1)
                ?.uppercase()
        }

        const val ROOM_CODE_LENGTH = 8
        private const val MAX_USERNAME_LENGTH = 50
    }
}
