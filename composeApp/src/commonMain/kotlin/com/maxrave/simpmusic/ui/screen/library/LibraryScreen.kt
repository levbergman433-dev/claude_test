package com.maxrave.simpmusic.ui.screen.library

import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.wrapContentSize
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.AppleShelfHeader
import com.maxrave.simpmusic.ui.component.LibraryShortcutGrid
import com.maxrave.simpmusic.ui.component.LibraryCollectionPills
import com.maxrave.simpmusic.ui.component.RecentlyRow
import com.maxrave.simpmusic.ui.component.recentlyKey
import simpmusic.composeapp.generated.resources.show_all
import simpmusic.composeapp.generated.resources.show_less
import simpmusic.composeapp.generated.resources.recently_added
import com.maxrave.simpmusic.ui.component.topBarSurface
import com.maxrave.simpmusic.ui.component.pageBrushSlice
import com.maxrave.simpmusic.ui.theme.LocalPageBrush
import com.maxrave.simpmusic.ui.component.AppleTopBarSeparator
import com.maxrave.simpmusic.ui.component.applePageTitleStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.LibraryChipType
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.taste.TasteProfile
import com.maxrave.domain.utils.LocalResource
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.extension.copy
import com.maxrave.simpmusic.extension.isScrollingUp
import com.maxrave.simpmusic.extension.ultraThinBarStyle
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.AppleBackRow
import com.maxrave.simpmusic.ui.component.AppleLibraryRow
import com.maxrave.simpmusic.ui.component.Chip
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.GridLibraryPlaylist
import com.maxrave.simpmusic.ui.component.LibraryItem
import com.maxrave.simpmusic.ui.component.LibraryItemState
import com.maxrave.simpmusic.ui.component.LibraryItemType
import com.maxrave.simpmusic.ui.component.LibraryTilingBox
import com.maxrave.simpmusic.ui.component.taste.ShareTasteSheet
import com.maxrave.simpmusic.ui.component.taste.TasteCard
import com.maxrave.simpmusic.ui.component.ListenTogetherIconButton
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.largeTitleStyle
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.AutoGraph
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.DownloadForOffline
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.Groups
import com.maxrave.simpmusic.ui.icon.Insights
import com.maxrave.simpmusic.ui.icon.LibraryMusic
import com.maxrave.simpmusic.ui.icon.PeopleAlt
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.RssFeed
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.Star
import com.maxrave.simpmusic.ui.icon.TrendingUp
import com.maxrave.simpmusic.ui.navigation.destination.home.ListenTogetherDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.theme.LocalAppleLayout
import com.maxrave.simpmusic.ui.theme.LocalLargeTitles
import com.maxrave.simpmusic.ui.navigation.destination.home.SettingsDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.LibraryViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import com.maxrave.simpmusic.viewModel.TasteUiState
import com.maxrave.simpmusic.viewModel.TasteViewModel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.chart
import simpmusic.composeapp.generated.resources.create
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloaded_playlists
import simpmusic.composeapp.generated.resources.favorite
import simpmusic.composeapp.generated.resources.favorite_playlists
import simpmusic.composeapp.generated.resources.favorite_podcasts
import simpmusic.composeapp.generated.resources.followed
import simpmusic.composeapp.generated.resources.library
import simpmusic.composeapp.generated.resources.mix_for_you
import simpmusic.composeapp.generated.resources.most_played
import simpmusic.composeapp.generated.resources.no_YouTube_playlists
import simpmusic.composeapp.generated.resources.no_charts_found
import simpmusic.composeapp.generated.resources.no_favorite_playlists
import simpmusic.composeapp.generated.resources.no_favorite_podcasts
import simpmusic.composeapp.generated.resources.no_playlists_added
import simpmusic.composeapp.generated.resources.no_playlists_downloaded
import simpmusic.composeapp.generated.resources.playlist_name
import simpmusic.composeapp.generated.resources.playlist_name_cannot_be_empty
import simpmusic.composeapp.generated.resources.simpmusic_charts
import simpmusic.composeapp.generated.resources.wrapped
import simpmusic.composeapp.generated.resources.your_library
import simpmusic.composeapp.generated.resources.your_playlists
import simpmusic.composeapp.generated.resources.your_youtube_playlists

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    innerPadding: PaddingValues,
    viewModel: LibraryViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
    onScrolling: (onTop: Boolean) -> Unit = {},
) {
    val density = LocalDensity.current

    val loggedIn by viewModel.youtubeLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    // Wrapped and its recaps are built entirely from playback_event, so the chip follows the same
    // setting the Analytics tab does.
    val localTrackingEnabled by viewModel.localTrackingEnabled.collectAsStateWithLifecycle(initialValue = false)
    val monthlyRecaps by viewModel.monthlyRecaps.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlayingVideoId.collectAsStateWithLifecycle()
    val youTubePlaylist by viewModel.youTubePlaylist.collectAsStateWithLifecycle()
    val listCanvasSong by viewModel.listCanvasSong.collectAsStateWithLifecycle()
    val yourLocalPlaylist by viewModel.yourLocalPlaylist.collectAsStateWithLifecycle()
    val favoritePlaylist by viewModel.favoritePlaylist.collectAsStateWithLifecycle()
    val downloadedPlaylist by viewModel.downloadedPlaylist.collectAsStateWithLifecycle()
    val favoritePodcasts by viewModel.favoritePodcasts.collectAsStateWithLifecycle()
    val chartPlaylists by viewModel.chartPlaylists.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val libraryOverview by viewModel.libraryOverview.collectAsStateWithLifecycle()
    // A list rebuilt on every track change can repeat an entry; a lazy list must not see a key twice.
    val history =
        remember(recentlyAdded) {
            recentlyAdded.data.orEmpty().distinctBy { it.recentlyKey() }
        }

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    val tasteViewModel: TasteViewModel = koinViewModel()
    // Held here rather than inside the list item, so the sheet does not close if its card scrolls
    // out of composition behind it.
    var sharingTaste by remember { mutableStateOf<TasteProfile?>(null) }
    // The history's song sheet, hosted here so it stays open when its row scrolls out of the list.
    var historySheetSong by remember { mutableStateOf<SongEntity?>(null) }
    var showAllHistory by rememberSaveable { mutableStateOf(false) }
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    val accountThumbnail by viewModel.accountThumbnail.collectAsStateWithLifecycle()
    val hazeState =
        rememberHazeState()

    var topAppBarHeight by remember {
        mutableStateOf(0.dp)
    }
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(nowPlaying) {
        Logger.w("LibraryScreen", "Check nowPlaying: $nowPlaying")
        viewModel.getRecentlyAdded()
    }

    val chipRowState = rememberScrollState()
    val currentFilter by viewModel.currentScreen.collectAsStateWithLifecycle()
    // A chip asked for by a deep link (simpmusic://library?tab=…), applied once and cleared.
    val libraryTabRequest by sharedViewModel.libraryTabRequest.collectAsStateWithLifecycle()
    LaunchedEffect(libraryTabRequest) {
        libraryTabRequest?.let {
            viewModel.setCurrentScreen(it)
            sharedViewModel.requestLibraryTab(null)
        }
    }

    LaunchedEffect(currentFilter) {
        when (currentFilter) {
            LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> {
                if (youTubePlaylist.data.isNullOrEmpty()) {
                    viewModel.getYouTubePlaylist()
                }
            }

            // Mix for you has its own nav tab now. The filter is persisted, so a build upgraded
            // while it was selected would land here with no chip to match — send it back to the
            // default. The enum value itself stays so older persisted values still parse.
            LibraryChipType.YOUTUBE_MIX_FOR_YOU -> {
                viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
            }

            LibraryChipType.YOUR_LIBRARY -> {
                viewModel.getCanvasSong()
                viewModel.getRecentlyAdded()
            }

            LibraryChipType.LOCAL_PLAYLIST -> {
                viewModel.getLocalPlaylist()
            }

            LibraryChipType.FAVORITE_PLAYLIST -> {
                viewModel.getPlaylistFavorite()
            }

            LibraryChipType.DOWNLOADED_PLAYLIST -> {
                viewModel.getDownloadedPlaylist()
            }

            LibraryChipType.FAVORITE_PODCAST -> {
                viewModel.getFavoritePodcasts()
            }

            LibraryChipType.CHART -> {
                if (chartPlaylists.data.isNullOrEmpty()) {
                    viewModel.getChartPlaylists()
                }
            }

            LibraryChipType.WRAPPED -> {
                viewModel.getMonthlyRecaps()
            }
        }
    }

    Crossfade(
        modifier = Modifier.hazeSource(hazeState),
        targetState = currentFilter,
    ) { filter ->
        when (filter) {
            LibraryChipType.YOUR_LIBRARY -> {
                val state = rememberLazyListState()
                val isScrollingUp by state.isScrollingUp()
                LaunchedEffect(state) {
                    snapshotFlow { state.firstVisibleItemIndex }
                        .collect {
                            if (it <= 1) {
                                onScrolling.invoke(true)
                            } else {
                                onScrolling.invoke(isScrollingUp)
                            }
                        }
                }
                // Desktop windows run wide; this tab keeps to the floating capsule player's width
                // so its cards line up with the capsule instead of stretching edge to edge. Done
                // as content padding rather than by narrowing the list, so the side gutters stay
                // part of it and the wheel still scrolls there.
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val layoutDirection = LocalLayoutDirection.current
                    val innerStart = innerPadding.calculateStartPadding(layoutDirection)
                    val innerEnd = innerPadding.calculateEndPadding(layoutDirection)
                    val sideInset =
                        if (getPlatform() == Platform.Desktop) {
                            ((maxWidth - innerStart - innerEnd - YOUR_LIBRARY_MAX_WIDTH) / 2).coerceAtLeast(0.dp)
                        } else {
                            0.dp
                        }
                    val appleLayout = LocalAppleLayout.current
                    LazyColumn(
                        contentPadding =
                            innerPadding.copy(
                                start = innerStart + sideInset,
                                top = topAppBarHeight,
                                end = innerEnd + sideInset,
                            ),
                        state = state,
                    ) {
                        // Everything that opens part of the library fits above the fold, so the
                        // history starts on the first screen instead of two screens down.
                        if (appleLayout) {
                            item(key = "shortcuts") {
                                LibraryShortcutGrid(
                                    navController = navController,
                                    overview = libraryOverview,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                            item(key = "collections") {
                                LibraryCollectionPills(
                                    showYouTube = loggedIn,
                                    showWrapped = localTrackingEnabled,
                                    onOpenCategory = { viewModel.setCurrentScreen(it) },
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                                )
                            }
                        } else {
                            item(key = "tiles") {
                                LibraryTilingBox(navController, libraryOverview)
                            }
                        }

                        // The history, one lazy row per entry. It used to be a single item holding
                        // every entry (up to 150) in a plain Column, so all of them were composed
                        // and laid out at once, and again on every track change.
                        item(key = "history_header") {
                            LibraryHistoryHeader(appleLayout = appleLayout)
                        }
                        if (history.isEmpty() && recentlyAdded is LocalResource.Loading) {
                            item(key = "history_loading") {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(130.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CenterLoadingBox(Modifier.wrapContentSize())
                                }
                            }
                        }
                        items(
                            items = if (showAllHistory) history else history.take(HISTORY_PREVIEW),
                            key = { "history:${it.recentlyKey()}" },
                            contentType = { it.objectType() },
                        ) { entry ->
                            RecentlyRow(
                                item = entry,
                                playingVideoId = nowPlaying,
                                navController = navController,
                                viewModel = viewModel,
                                sharedViewModel = sharedViewModel,
                                selectionState = selectionState,
                                onSongMore = { historySheetSong = it },
                            )
                        }
                        if (history.size > HISTORY_PREVIEW) {
                            item(key = "history_toggle") {
                                TextButton(
                                    onClick = { showAllHistory = !showAllHistory },
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                ) {
                                    Text(
                                        text = stringResource(if (showAllHistory) Res.string.show_less else Res.string.show_all),
                                        style = typo().titleSmall.copy(fontSize = 15.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }

                        if (!listCanvasSong.data.isNullOrEmpty()) {
                            item(key = "canvas") {
                                LibraryItem(
                                    state =
                                        LibraryItemState(
                                            type = LibraryItemType.CanvasSong,
                                            data = listCanvasSong.data ?: emptyList(),
                                            isLoading = listCanvasSong is LocalResource.Loading,
                                        ),
                                    navController = navController,
                                )
                            }
                        }

                        // The reading is drawn from playback_event, so the card follows the same
                        // setting the Wrapped chip and the Analytics tab do.
                        if (localTrackingEnabled) {
                            item(key = "taste") {
                                val tasteState by tasteViewModel.uiState.collectAsStateWithLifecycle()
                                if (tasteState != TasteUiState.Hidden) {
                                    TasteCard(
                                        state = tasteState,
                                        onGenerate = tasteViewModel::generate,
                                        onOpenSettings = { navController.navigate(SettingsDestination) },
                                        onShare = { sharingTaste = it },
                                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 16.dp, bottom = 8.dp),
                                    )
                                }
                            }
                        }
                        item(key = "end") {
                            EndOfPage()
                        }
                    }
                }
            }

            LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    innerPadding.copy(top = topAppBarHeight),
                    youTubePlaylist,
                    emptyText = Res.string.no_YouTube_playlists,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getYouTubePlaylist()
                }
            }

            // Nothing to draw: MixForYouScreen owns this content now, and the effect above bounces
            // the filter back to YOUR_LIBRARY the moment it lands here.
            LibraryChipType.YOUTUBE_MIX_FOR_YOU -> Unit

            LibraryChipType.LOCAL_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    innerPadding.copy(top = topAppBarHeight),
                    yourLocalPlaylist,
                    onScrolling = onScrolling,
                    emptyText = Res.string.no_playlists_added,
                    createNewPlaylist = {
                        showAddSheet = true
                    },
                ) {
                    viewModel.getLocalPlaylist()
                }
            }

            LibraryChipType.FAVORITE_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    innerPadding.copy(top = topAppBarHeight),
                    favoritePlaylist,
                    emptyText = Res.string.no_favorite_playlists,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getPlaylistFavorite()
                }
            }

            LibraryChipType.DOWNLOADED_PLAYLIST -> {
                GridLibraryPlaylist(
                    navController,
                    innerPadding.copy(top = topAppBarHeight),
                    downloadedPlaylist,
                    emptyText = Res.string.no_playlists_downloaded,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getDownloadedPlaylist()
                }
            }

            LibraryChipType.FAVORITE_PODCAST -> {
                GridLibraryPlaylist(
                    navController,
                    innerPadding.copy(top = topAppBarHeight),
                    favoritePodcasts,
                    emptyText = Res.string.no_favorite_podcasts,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getFavoritePodcasts()
                }
            }

            LibraryChipType.CHART -> {
                GridLibraryPlaylist(
                    navController,
                    innerPadding.copy(top = topAppBarHeight),
                    chartPlaylists,
                    emptyText = Res.string.no_charts_found,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getChartPlaylists()
                }
            }

            LibraryChipType.WRAPPED -> {
                LibraryWrappedTab(
                    navController = navController,
                    contentPadding = innerPadding.copy(top = topAppBarHeight),
                    recaps = monthlyRecaps,
                    onScrolling = onScrolling,
                ) {
                    viewModel.getMonthlyRecaps()
                }
            }
        }
    }
    historySheetSong?.let { song ->
        NowPlayingBottomSheet(
            onDismiss = { historySheetSong = null },
            navController = navController,
            song = song,
            onLibraryDelete = { viewModel.deleteSong(song.videoId) },
        )
    }
    sharingTaste?.let { profile ->
        ShareTasteSheet(profile = profile, onDismiss = { sharingTaste = null })
    }
    val coroutineScope = rememberCoroutineScope()
    if (showAddSheet) {
        var newTitle by remember { mutableStateOf("") }
        val showAddSheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            )
        val hideEditTitleBottomSheet: () -> Unit =
            {
                coroutineScope.launch {
                    showAddSheetState.hide()
                    showAddSheet = false
                }
            }
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = showAddSheetState,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Card(
                        modifier =
                            Modifier
                                .width(60.dp)
                                .height(4.dp),
                        colors =
                            CardDefaults.cardColors().copy(
                                containerColor = MaterialTheme.colorScheme.outline,
                            ),
                        shape = RoundedCornerShape(50),
                    ) {}
                    Spacer(modifier = Modifier.height(5.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { s -> newTitle = s },
                        label = {
                            Text(text = stringResource(Res.string.playlist_name))
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    TextButton(
                        onClick = {
                            if (newTitle.isBlank()) {
                                viewModel.makeToast(runBlocking { getString(Res.string.playlist_name_cannot_be_empty) })
                            } else {
                                viewModel.createPlaylist(newTitle)
                                hideEditTitleBottomSheet()
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(Res.string.create))
                    }
                }
            }
        }
    }
    val appleLayoutBar = LocalAppleLayout.current
    Column(
        Modifier
            .topBarSurface(
                atTop = false,
                hazeState = hazeState,
                default =
                if (appleLayoutBar && LocalPageBrush.current == null) {
                    // Apple Music's bar is solid page colour, with a hairline under it.
                    Modifier.background(MaterialTheme.colorScheme.background)
                } else if (LocalPageBrush.current != null) {
                    // Gradient page: the slice of the gradient behind the bar, not a tinted blur
                    // that reads as a flat band over the gradient.
                    Modifier.pageBrushSlice(LocalPageBrush.current!!)
                } else {
                    Modifier
                        .background(Color.Transparent)
                        .hazeBlur(HazeInput.Sources(hazeState), ultraThinBarStyle())
                },
            ).onGloballyPositioned { coordinates ->
                topAppBarHeight = with(density) { coordinates.size.height.toDp() }
            },
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.library),
                    style =
                        when {
                            appleLayoutBar -> applePageTitleStyle()
                            LocalLargeTitles.current -> largeTitleStyle()
                            else -> typo().titleMedium
                        },
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            navigationIcon = {
                AnimatedVisibility(
                    !accountThumbnail.isNullOrEmpty(),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalPlatformContext.current)
                                .data(accountThumbnail)
                                .crossfade(550)
                                .build(),
                        placeholder = rememberVectorPainter(SimpIcons.PeopleAlt),
                        error = rememberVectorPainter(SimpIcons.PeopleAlt),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape),
                    )
                }
            },
            // The Library bar had no actions slot at all — added for the Listen Together entry,
            // which the design canvas puts on Home AND Library.
            actions = {
                ListenTogetherIconButton { navController.navigate(ListenTogetherDestination()) }
            },
        )
        if (appleLayoutBar) AppleTopBarSeparator()
        AnimatedVisibility(visible = selectionState.isActive) {
            SongSelectionTopAppBar(
                state = selectionState,
                // Stacked BELOW the Library TopAppBar in the same Column, which already consumed
                // the status-bar inset — leaving the default here reserved it twice and opened a
                // status-bar-sized band of dead blur between the two bars. Same fix as Search;
                // the overlay-style call sites (Album, Artist, Recently…) keep the default because
                // they COVER their normal bar instead of standing under it.
                windowInsets = WindowInsets(0),
                onSelectAll = {
                    selectionState.toggleSelectAll(
                        (recentlyAdded.data ?: emptyList())
                            .filterIsInstance<SongEntity>()
                            .map { it.videoId },
                    )
                },
                onOpenActions = { showSelectionSheet = true },
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (LocalAppleLayout.current) {
            // Apple Music Library: categories are rows on the landing page, so a sub-page shows a
            // way back instead of the chip row.
            if (currentFilter != LibraryChipType.YOUR_LIBRARY) {
                AppleBackRow(label = stringResource(Res.string.library)) {
                    viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
                }
            }
        } else {
            Row(
                modifier =
                    Modifier
                        .horizontalScroll(chipRowState)
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 8.dp)
                        .background(Color.Transparent),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                LibraryChipType.entries.forEach { type ->
                    // Mix for you left this row for a tab of its own.
                    if (type == LibraryChipType.YOUTUBE_MIX_FOR_YOU) {
                        return@forEach
                    }
                    if (type == LibraryChipType.YOUTUBE_MUSIC_PLAYLIST && !loggedIn) {
                        return@forEach
                    }
                    // Nothing to recap without the plays — gated exactly as the YouTube chip above
                    // is gated on being logged in.
                    if (type == LibraryChipType.WRAPPED && !localTrackingEnabled) {
                        return@forEach
                    }
                    Chip(
                        isAnimated = false,
                        isSelected = type == currentFilter,
                        text =
                            when (type) {
                                LibraryChipType.YOUR_LIBRARY -> stringResource(Res.string.your_library)
                                LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> stringResource(Res.string.your_youtube_playlists)
                                LibraryChipType.YOUTUBE_MIX_FOR_YOU -> stringResource(Res.string.mix_for_you)
                                LibraryChipType.LOCAL_PLAYLIST -> stringResource(Res.string.your_playlists)
                                LibraryChipType.FAVORITE_PLAYLIST -> stringResource(Res.string.favorite_playlists)
                                LibraryChipType.DOWNLOADED_PLAYLIST -> stringResource(Res.string.downloaded_playlists)
                                LibraryChipType.FAVORITE_PODCAST -> stringResource(Res.string.favorite_podcasts)
                                LibraryChipType.CHART -> stringResource(Res.string.simpmusic_charts)
                                LibraryChipType.WRAPPED -> stringResource(Res.string.wrapped)
                            },
                    ) {
                        viewModel.setCurrentScreen(type)
                    }
                }
            }
        }
        if (showSelectionSheet) {
            val selectedIds = selectionState.selected.toList()
            SelectedSongsBottomSheet(
                count = selectedIds.size,
                onDismiss = { showSelectionSheet = false },
                onPlayNext = {
                    selectionViewModel.playNext(selectedIds)
                    selectionState.exit()
                },
                onAddToQueue = {
                    selectionViewModel.addToQueue(selectedIds)
                    selectionState.exit()
                },
                onAddToPlaylist = { showSelectionAddToPlaylist = true },
                onDownload = {
                    selectionViewModel.download(selectedIds)
                    selectionState.exit()
                },
                onAddToFavorite = {
                    selectionViewModel.addToFavorite(selectedIds)
                    selectionState.exit()
                },
            )
        }
        if (showSelectionAddToPlaylist) {
            val selectedIds = selectionState.selected.toList()
            val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
            AddToPlaylistModalBottomSheet(
                isBottomSheetVisible = true,
                listLocalPlaylist = localPlaylists,
                listYouTubePlaylist = emptyList(),
                onDismiss = { showSelectionAddToPlaylist = false },
                onClick = { playlist ->
                    selectionViewModel.addToPlaylist(playlist.id, selectedIds)
                    selectionState.exit()
                },
                onYTPlaylistClick = {},
            )
        }
    }
}

/**
 * How wide the Your Library tab may get on Desktop: the floating capsule player's own width plus
 * the 10dp gutter every row of this list already carries, so the cards' edges land on the
 * capsule's. The capsule has no width constant of its own — `MiniPlayer`'s Desktop row sums to
 * 16 + 200 (controls) + 29 (divider) + 300 (track) + 29 (divider) + 232 (heart cell 40 + four
 * 48dp icon buttons) + 16 = 822dp — so this must follow if that row changes.
 */
private val YOUR_LIBRARY_MAX_WIDTH = 842.dp

/** "Recently Added" over the Your library history, in the Apple layout's shelf style or the classic one. */
@Composable
private fun LibraryHistoryHeader(appleLayout: Boolean) {
    if (appleLayout) {
        AppleShelfHeader(
            title = stringResource(Res.string.recently_added),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
    } else {
        Text(
            text = stringResource(Res.string.recently_added),
            style = typo().headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp).height(35.dp).wrapContentHeight(align = Alignment.CenterVertically),
        )
    }
}

/** How much of the history shows before "Show all": about two screens' worth. */
private const val HISTORY_PREVIEW = 25
