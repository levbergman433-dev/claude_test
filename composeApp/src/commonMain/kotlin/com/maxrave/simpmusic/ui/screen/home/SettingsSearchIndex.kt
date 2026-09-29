package com.maxrave.simpmusic.ui.screen.home

import org.jetbrains.compose.resources.StringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.adaptive_quality_weak_network
import simpmusic.composeapp.generated.resources.adaptive_quality_weak_network_description
import simpmusic.composeapp.generated.resources.ai
import simpmusic.composeapp.generated.resources.ai_api_key
import simpmusic.composeapp.generated.resources.ai_provider
import simpmusic.composeapp.generated.resources.animated_artwork_info
import simpmusic.composeapp.generated.resources.aod_auto_dim
import simpmusic.composeapp.generated.resources.aod_burn_in
import simpmusic.composeapp.generated.resources.aod_burn_in_description
import simpmusic.composeapp.generated.resources.aod_clock
import simpmusic.composeapp.generated.resources.aod_clock_description
import simpmusic.composeapp.generated.resources.aod_clock_style
import simpmusic.composeapp.generated.resources.aod_lock_screen
import simpmusic.composeapp.generated.resources.aod_lock_screen_description
import simpmusic.composeapp.generated.resources.app_font
import simpmusic.composeapp.generated.resources.apple_layout
import simpmusic.composeapp.generated.resources.apple_layout_description
import simpmusic.composeapp.generated.resources.audio
import simpmusic.composeapp.generated.resources.audio_delay
import simpmusic.composeapp.generated.resources.audio_delay_description
import simpmusic.composeapp.generated.resources.audio_reverb
import simpmusic.composeapp.generated.resources.audio_reverb_description
import simpmusic.composeapp.generated.resources.auto_backup
import simpmusic.composeapp.generated.resources.auto_backup_description
import simpmusic.composeapp.generated.resources.auto_download_liked_songs
import simpmusic.composeapp.generated.resources.auto_download_liked_songs_description
import simpmusic.composeapp.generated.resources.backup
import simpmusic.composeapp.generated.resources.backup_downloaded
import simpmusic.composeapp.generated.resources.backup_downloaded_description
import simpmusic.composeapp.generated.resources.backup_frequency
import simpmusic.composeapp.generated.resources.balance_media_loudness
import simpmusic.composeapp.generated.resources.battery_saver
import simpmusic.composeapp.generated.resources.battery_saver_description
import simpmusic.composeapp.generated.resources.canvas_info
import simpmusic.composeapp.generated.resources.categories_sponsor_block
import simpmusic.composeapp.generated.resources.clear_listening_history
import simpmusic.composeapp.generated.resources.clear_listening_history_description
import simpmusic.composeapp.generated.resources.combine_local_and_youtube_liked_songs
import simpmusic.composeapp.generated.resources.combine_local_and_youtube_liked_songs_description
import simpmusic.composeapp.generated.resources.content
import simpmusic.composeapp.generated.resources.content_country
import simpmusic.composeapp.generated.resources.contributor_email
import simpmusic.composeapp.generated.resources.contributor_name
import simpmusic.composeapp.generated.resources.credits_maiker_subtitle
import simpmusic.composeapp.generated.resources.credits_maiker_title
import simpmusic.composeapp.generated.resources.credits_simpmusic_subtitle
import simpmusic.composeapp.generated.resources.credits_simpmusic_title
import simpmusic.composeapp.generated.resources.crossfade
import simpmusic.composeapp.generated.resources.crossfade_dj_mode
import simpmusic.composeapp.generated.resources.crossfade_duration
import simpmusic.composeapp.generated.resources.crossfade_skip_album
import simpmusic.composeapp.generated.resources.custom_ai_model_id
import simpmusic.composeapp.generated.resources.custom_color
import simpmusic.composeapp.generated.resources.discord_integration
import simpmusic.composeapp.generated.resources.download_quality
import simpmusic.composeapp.generated.resources.downloaded_cache
import simpmusic.composeapp.generated.resources.enable_animated_artwork
import simpmusic.composeapp.generated.resources.enable_canvas
import simpmusic.composeapp.generated.resources.enable_liquid_glass_effect
import simpmusic.composeapp.generated.resources.enable_liquid_glass_effect_description
import simpmusic.composeapp.generated.resources.enable_rich_presence
import simpmusic.composeapp.generated.resources.enable_scrobbling
import simpmusic.composeapp.generated.resources.enable_sponsor_block
import simpmusic.composeapp.generated.resources.enable_spotify_lyrics
import simpmusic.composeapp.generated.resources.equalizer
import simpmusic.composeapp.generated.resources.equalizer_description
import simpmusic.composeapp.generated.resources.fast_song_loading
import simpmusic.composeapp.generated.resources.fast_song_loading_description
import simpmusic.composeapp.generated.resources.glass_style
import simpmusic.composeapp.generated.resources.help_build_lyrics_database
import simpmusic.composeapp.generated.resources.help_build_lyrics_database_description
import simpmusic.composeapp.generated.resources.import_data
import simpmusic.composeapp.generated.resources.import_playlists_from_other_apps
import simpmusic.composeapp.generated.resources.keep_backups
import simpmusic.composeapp.generated.resources.keep_service_alive
import simpmusic.composeapp.generated.resources.keep_service_alive_description
import simpmusic.composeapp.generated.resources.keep_your_youtube_playlist_offline
import simpmusic.composeapp.generated.resources.keep_your_youtube_playlist_offline_description
import simpmusic.composeapp.generated.resources.kill_service_on_exit
import simpmusic.composeapp.generated.resources.kill_service_on_exit_description
import simpmusic.composeapp.generated.resources.language
import simpmusic.composeapp.generated.resources.large_titles
import simpmusic.composeapp.generated.resources.large_titles_description
import simpmusic.composeapp.generated.resources.last_backup
import simpmusic.composeapp.generated.resources.lastfm_integration
import simpmusic.composeapp.generated.resources.limit_player_cache
import simpmusic.composeapp.generated.resources.listening_history
import simpmusic.composeapp.generated.resources.local_tracking_description
import simpmusic.composeapp.generated.resources.local_tracking_title
import simpmusic.composeapp.generated.resources.lyrics
import simpmusic.composeapp.generated.resources.lyrics_romanization
import simpmusic.composeapp.generated.resources.lyrics_style
import simpmusic.composeapp.generated.resources.main_lyrics_provider
import simpmusic.composeapp.generated.resources.manage_your_youtube_accounts
import simpmusic.composeapp.generated.resources.menu_button_size
import simpmusic.composeapp.generated.resources.menu_style
import simpmusic.composeapp.generated.resources.normalize_volume
import simpmusic.composeapp.generated.resources.now_playing_style
import simpmusic.composeapp.generated.resources.open_links_header
import simpmusic.composeapp.generated.resources.open_youtube_links
import simpmusic.composeapp.generated.resources.open_youtube_links_description
import simpmusic.composeapp.generated.resources.page_image
import simpmusic.composeapp.generated.resources.page_image_dim
import simpmusic.composeapp.generated.resources.page_image_set
import simpmusic.composeapp.generated.resources.page_image_text
import simpmusic.composeapp.generated.resources.play_explicit_content
import simpmusic.composeapp.generated.resources.play_explicit_content_description
import simpmusic.composeapp.generated.resources.play_video_for_video_track_instead_of_audio_only
import simpmusic.composeapp.generated.resources.playback
import simpmusic.composeapp.generated.resources.player_cache
import simpmusic.composeapp.generated.resources.profile_badge
import simpmusic.composeapp.generated.resources.profile_badge_description
import simpmusic.composeapp.generated.resources.profile_badge_font
import simpmusic.composeapp.generated.resources.profile_badge_icon
import simpmusic.composeapp.generated.resources.profile_badge_icon_color
import simpmusic.composeapp.generated.resources.profile_badge_name
import simpmusic.composeapp.generated.resources.profile_badge_name_color
import simpmusic.composeapp.generated.resources.profile_badge_picture
import simpmusic.composeapp.generated.resources.profile_badge_picture_reset
import simpmusic.composeapp.generated.resources.profile_badge_picture_youtube
import simpmusic.composeapp.generated.resources.proxy
import simpmusic.composeapp.generated.resources.proxy_description
import simpmusic.composeapp.generated.resources.proxy_host
import simpmusic.composeapp.generated.resources.proxy_password
import simpmusic.composeapp.generated.resources.proxy_port
import simpmusic.composeapp.generated.resources.proxy_type
import simpmusic.composeapp.generated.resources.proxy_username
import simpmusic.composeapp.generated.resources.quality
import simpmusic.composeapp.generated.resources.radio_audio_only
import simpmusic.composeapp.generated.resources.radio_audio_only_description
import simpmusic.composeapp.generated.resources.restore_your_data
import simpmusic.composeapp.generated.resources.restore_your_saved_data
import simpmusic.composeapp.generated.resources.rich_presence_info
import simpmusic.composeapp.generated.resources.save_all_your_playlist_data
import simpmusic.composeapp.generated.resources.save_last_played
import simpmusic.composeapp.generated.resources.save_last_played_track_and_queue
import simpmusic.composeapp.generated.resources.save_playback_state
import simpmusic.composeapp.generated.resources.save_shuffle_and_repeat_mode
import simpmusic.composeapp.generated.resources.scrobbling_info
import simpmusic.composeapp.generated.resources.send_back_listening_data_to_google
import simpmusic.composeapp.generated.resources.settings_credits_header
import simpmusic.composeapp.generated.resources.settings_lyrics_display_header
import simpmusic.composeapp.generated.resources.settings_performance_header
import simpmusic.composeapp.generated.resources.settings_quality_header
import simpmusic.composeapp.generated.resources.settings_section_layout
import simpmusic.composeapp.generated.resources.settings_section_night_mode
import simpmusic.composeapp.generated.resources.settings_section_player
import simpmusic.composeapp.generated.resources.settings_section_theme
import simpmusic.composeapp.generated.resources.show_mix_tab
import simpmusic.composeapp.generated.resources.show_mix_tab_description
import simpmusic.composeapp.generated.resources.skip_no_music_part
import simpmusic.composeapp.generated.resources.skip_silent
import simpmusic.composeapp.generated.resources.skip_sponsor_part_of_video
import simpmusic.composeapp.generated.resources.sponsorBlock
import simpmusic.composeapp.generated.resources.spotify
import simpmusic.composeapp.generated.resources.spotify_canvas_cache
import simpmusic.composeapp.generated.resources.spotify_lyrícs_info
import simpmusic.composeapp.generated.resources.storage
import simpmusic.composeapp.generated.resources.such_as_music_video_lyrics_video_podcasts_and_more
import simpmusic.composeapp.generated.resources.sync_follow_to_youtube
import simpmusic.composeapp.generated.resources.sync_follow_to_youtube_description
import simpmusic.composeapp.generated.resources.theme
import simpmusic.composeapp.generated.resources.theme_color
import simpmusic.composeapp.generated.resources.thumbnail_cache
import simpmusic.composeapp.generated.resources.top_bar_style
import simpmusic.composeapp.generated.resources.translation_language
import simpmusic.composeapp.generated.resources.translucent_bottom_navigation_bar
import simpmusic.composeapp.generated.resources.use_ai_translation
import simpmusic.composeapp.generated.resources.use_ai_translation_description
import simpmusic.composeapp.generated.resources.video_download_quality
import simpmusic.composeapp.generated.resources.video_quality
import simpmusic.composeapp.generated.resources.what_segments_will_be_skipped
import simpmusic.composeapp.generated.resources.you_can_see_the_content_below_the_bottom_bar
import simpmusic.composeapp.generated.resources.youtube_account
import simpmusic.composeapp.generated.resources.youtube_subtitle_language

// Generated from the rows in SettingScreen.kt: every titled setting, and the named sections of each
// category. Search reads this instead of the rows themselves, so a new row only shows up in search
// once it is added here too.

internal class SettingsSearchEntry(
    val title: StringResource,
    val subtitle: StringResource?,
    val category: SettingsCategory,
    /** The key of the section the row sits in, which is what a result scrolls to. */
    val section: String,
)

internal class SettingsSectionInfo(
    val category: SettingsCategory,
    val key: String,
    val title: StringResource,
)

internal val settingsSearchIndex: List<SettingsSearchEntry> =
    listOf(
        SettingsSearchEntry(Res.string.theme, null, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.page_image, Res.string.page_image_set, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.page_image_dim, null, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.page_image_text, null, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.app_font, null, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.theme_color, null, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.custom_color, null, SettingsCategory.APPEARANCE, "user_interface"),
        SettingsSearchEntry(Res.string.now_playing_style, null, SettingsCategory.APPEARANCE, "player_look"),
        SettingsSearchEntry(Res.string.aod_clock, Res.string.aod_clock_description, SettingsCategory.APPEARANCE, "night_mode"),
        SettingsSearchEntry(Res.string.aod_clock_style, null, SettingsCategory.APPEARANCE, "night_mode"),
        SettingsSearchEntry(Res.string.aod_lock_screen, Res.string.aod_lock_screen_description, SettingsCategory.APPEARANCE, "night_mode"),
        SettingsSearchEntry(Res.string.aod_burn_in, Res.string.aod_burn_in_description, SettingsCategory.APPEARANCE, "night_mode"),
        SettingsSearchEntry(Res.string.aod_auto_dim, null, SettingsCategory.APPEARANCE, "night_mode"),
        SettingsSearchEntry(Res.string.translucent_bottom_navigation_bar, Res.string.you_can_see_the_content_below_the_bottom_bar, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.enable_liquid_glass_effect, Res.string.enable_liquid_glass_effect_description, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.glass_style, null, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.apple_layout, Res.string.apple_layout_description, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.large_titles, Res.string.large_titles_description, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.menu_button_size, null, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.top_bar_style, null, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.menu_style, null, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.show_mix_tab, Res.string.show_mix_tab_description, SettingsCategory.APPEARANCE, "layout"),
        SettingsSearchEntry(Res.string.profile_badge, Res.string.profile_badge_description, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_name, null, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_picture, null, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_picture_reset, Res.string.profile_badge_picture_youtube, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_font, null, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_name_color, null, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_icon, null, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.profile_badge_icon_color, null, SettingsCategory.APPEARANCE, "profile_badge"),
        SettingsSearchEntry(Res.string.youtube_account, Res.string.manage_your_youtube_accounts, SettingsCategory.ACCOUNT, "content"),
        SettingsSearchEntry(Res.string.language, null, SettingsCategory.ACCOUNT, "content"),
        SettingsSearchEntry(Res.string.content_country, null, SettingsCategory.ACCOUNT, "content"),
        SettingsSearchEntry(Res.string.quality, null, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.download_quality, null, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.video_quality, null, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.video_download_quality, null, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.auto_download_liked_songs, Res.string.auto_download_liked_songs_description, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.play_video_for_video_track_instead_of_audio_only, Res.string.such_as_music_video_lyrics_video_podcasts_and_more, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.radio_audio_only, Res.string.radio_audio_only_description, SettingsCategory.PLAYBACK, "quality"),
        SettingsSearchEntry(Res.string.sync_follow_to_youtube, Res.string.sync_follow_to_youtube_description, SettingsCategory.ACCOUNT, "content_more"),
        SettingsSearchEntry(Res.string.send_back_listening_data_to_google, null, SettingsCategory.ACCOUNT, "content_more"),
        SettingsSearchEntry(Res.string.play_explicit_content, Res.string.play_explicit_content_description, SettingsCategory.ACCOUNT, "content_more"),
        SettingsSearchEntry(Res.string.keep_your_youtube_playlist_offline, Res.string.keep_your_youtube_playlist_offline_description, SettingsCategory.ACCOUNT, "content_more"),
        SettingsSearchEntry(Res.string.combine_local_and_youtube_liked_songs, Res.string.combine_local_and_youtube_liked_songs_description, SettingsCategory.ACCOUNT, "content_more"),
        SettingsSearchEntry(Res.string.proxy, Res.string.proxy_description, SettingsCategory.ACCOUNT, "content_more"),
        SettingsSearchEntry(Res.string.proxy_type, null, SettingsCategory.ACCOUNT, "proxy"),
        SettingsSearchEntry(Res.string.proxy_host, null, SettingsCategory.ACCOUNT, "proxy"),
        SettingsSearchEntry(Res.string.proxy_port, null, SettingsCategory.ACCOUNT, "proxy"),
        SettingsSearchEntry(Res.string.proxy_username, null, SettingsCategory.ACCOUNT, "proxy"),
        SettingsSearchEntry(Res.string.proxy_password, null, SettingsCategory.ACCOUNT, "proxy"),
        SettingsSearchEntry(Res.string.normalize_volume, Res.string.balance_media_loudness, SettingsCategory.PLAYBACK, "audio"),
        SettingsSearchEntry(Res.string.skip_silent, Res.string.skip_no_music_part, SettingsCategory.PLAYBACK, "audio"),
        SettingsSearchEntry(Res.string.equalizer, Res.string.equalizer_description, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.audio_delay, Res.string.audio_delay_description, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.audio_reverb, Res.string.audio_reverb_description, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.save_playback_state, Res.string.save_shuffle_and_repeat_mode, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.save_last_played, Res.string.save_last_played_track_and_queue, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.kill_service_on_exit, Res.string.kill_service_on_exit_description, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.keep_service_alive, Res.string.keep_service_alive_description, SettingsCategory.PLAYBACK, "playback"),
        SettingsSearchEntry(Res.string.crossfade, null, SettingsCategory.PLAYBACK, "crossfade_settings"),
        SettingsSearchEntry(Res.string.crossfade_duration, null, SettingsCategory.PLAYBACK, "crossfade_settings"),
        SettingsSearchEntry(Res.string.crossfade_dj_mode, null, SettingsCategory.PLAYBACK, "crossfade_settings"),
        SettingsSearchEntry(Res.string.crossfade_skip_album, null, SettingsCategory.PLAYBACK, "crossfade_settings"),
        SettingsSearchEntry(Res.string.fast_song_loading, Res.string.fast_song_loading_description, SettingsCategory.PLAYBACK, "performance"),
        SettingsSearchEntry(Res.string.adaptive_quality_weak_network, Res.string.adaptive_quality_weak_network_description, SettingsCategory.PLAYBACK, "performance"),
        SettingsSearchEntry(Res.string.battery_saver, Res.string.battery_saver_description, SettingsCategory.PLAYBACK, "performance"),
        SettingsSearchEntry(Res.string.local_tracking_title, Res.string.local_tracking_description, SettingsCategory.STORAGE, "listening_history"),
        SettingsSearchEntry(Res.string.clear_listening_history, Res.string.clear_listening_history_description, SettingsCategory.STORAGE, "listening_history"),
        SettingsSearchEntry(Res.string.lyrics_style, null, SettingsCategory.LYRICS, "lyrics_display"),
        SettingsSearchEntry(Res.string.lyrics_romanization, null, SettingsCategory.LYRICS, "lyrics_display"),
        SettingsSearchEntry(Res.string.main_lyrics_provider, null, SettingsCategory.LYRICS, "lyrics"),
        SettingsSearchEntry(Res.string.translation_language, null, SettingsCategory.LYRICS, "lyrics"),
        SettingsSearchEntry(Res.string.youtube_subtitle_language, null, SettingsCategory.LYRICS, "lyrics"),
        SettingsSearchEntry(Res.string.help_build_lyrics_database, Res.string.help_build_lyrics_database_description, SettingsCategory.LYRICS, "lyrics"),
        SettingsSearchEntry(Res.string.contributor_name, null, SettingsCategory.LYRICS, "lyrics"),
        SettingsSearchEntry(Res.string.contributor_email, null, SettingsCategory.LYRICS, "lyrics"),
        SettingsSearchEntry(Res.string.ai_provider, null, SettingsCategory.LYRICS, "AI"),
        SettingsSearchEntry(Res.string.ai_api_key, null, SettingsCategory.LYRICS, "AI"),
        SettingsSearchEntry(Res.string.custom_ai_model_id, null, SettingsCategory.LYRICS, "AI"),
        SettingsSearchEntry(Res.string.use_ai_translation, Res.string.use_ai_translation_description, SettingsCategory.LYRICS, "AI"),
        SettingsSearchEntry(Res.string.open_youtube_links, Res.string.open_youtube_links_description, SettingsCategory.SERVICES, "links"),
        SettingsSearchEntry(Res.string.enable_spotify_lyrics, Res.string.spotify_lyrícs_info, SettingsCategory.SERVICES, "spotify"),
        SettingsSearchEntry(Res.string.enable_canvas, Res.string.canvas_info, SettingsCategory.SERVICES, "spotify"),
        SettingsSearchEntry(Res.string.enable_animated_artwork, Res.string.animated_artwork_info, SettingsCategory.SERVICES, "spotify"),
        SettingsSearchEntry(Res.string.enable_rich_presence, Res.string.rich_presence_info, SettingsCategory.SERVICES, "discord"),
        SettingsSearchEntry(Res.string.enable_scrobbling, Res.string.scrobbling_info, SettingsCategory.SERVICES, "lastfm"),
        SettingsSearchEntry(Res.string.enable_sponsor_block, Res.string.skip_sponsor_part_of_video, SettingsCategory.SERVICES, "sponsor_block"),
        SettingsSearchEntry(Res.string.categories_sponsor_block, Res.string.what_segments_will_be_skipped, SettingsCategory.SERVICES, "sponsor_block"),
        SettingsSearchEntry(Res.string.player_cache, null, SettingsCategory.STORAGE, "storage"),
        SettingsSearchEntry(Res.string.downloaded_cache, null, SettingsCategory.STORAGE, "storage"),
        SettingsSearchEntry(Res.string.thumbnail_cache, null, SettingsCategory.STORAGE, "storage"),
        SettingsSearchEntry(Res.string.spotify_canvas_cache, null, SettingsCategory.STORAGE, "storage"),
        SettingsSearchEntry(Res.string.limit_player_cache, null, SettingsCategory.STORAGE, "storage"),
        SettingsSearchEntry(Res.string.backup_downloaded, Res.string.backup_downloaded_description, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.auto_backup, Res.string.auto_backup_description, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.backup_frequency, null, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.keep_backups, null, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.last_backup, null, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.backup, Res.string.save_all_your_playlist_data, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.restore_your_data, Res.string.restore_your_saved_data, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.import_data, Res.string.import_playlists_from_other_apps, SettingsCategory.STORAGE, "backup"),
        SettingsSearchEntry(Res.string.credits_simpmusic_title, Res.string.credits_simpmusic_subtitle, SettingsCategory.ABOUT, "about_us"),
        SettingsSearchEntry(Res.string.credits_maiker_title, Res.string.credits_maiker_subtitle, SettingsCategory.ABOUT, "about_us"),
    )

internal val settingsSections: List<SettingsSectionInfo> =
    listOf(
        SettingsSectionInfo(SettingsCategory.APPEARANCE, "user_interface", Res.string.settings_section_theme),
        SettingsSectionInfo(SettingsCategory.APPEARANCE, "player_look", Res.string.settings_section_player),
        SettingsSectionInfo(SettingsCategory.APPEARANCE, "night_mode", Res.string.settings_section_night_mode),
        SettingsSectionInfo(SettingsCategory.APPEARANCE, "layout", Res.string.settings_section_layout),
        SettingsSectionInfo(SettingsCategory.APPEARANCE, "profile_badge", Res.string.profile_badge),
        SettingsSectionInfo(SettingsCategory.ACCOUNT, "content", Res.string.content),
        SettingsSectionInfo(SettingsCategory.PLAYBACK, "quality", Res.string.settings_quality_header),
        SettingsSectionInfo(SettingsCategory.PLAYBACK, "audio", Res.string.audio),
        SettingsSectionInfo(SettingsCategory.PLAYBACK, "playback", Res.string.playback),
        SettingsSectionInfo(SettingsCategory.PLAYBACK, "performance", Res.string.settings_performance_header),
        SettingsSectionInfo(SettingsCategory.STORAGE, "listening_history", Res.string.listening_history),
        SettingsSectionInfo(SettingsCategory.LYRICS, "lyrics_display", Res.string.settings_lyrics_display_header),
        SettingsSectionInfo(SettingsCategory.LYRICS, "lyrics", Res.string.lyrics),
        SettingsSectionInfo(SettingsCategory.LYRICS, "AI", Res.string.ai),
        SettingsSectionInfo(SettingsCategory.SERVICES, "links", Res.string.open_links_header),
        SettingsSectionInfo(SettingsCategory.SERVICES, "spotify", Res.string.spotify),
        SettingsSectionInfo(SettingsCategory.SERVICES, "discord", Res.string.discord_integration),
        SettingsSectionInfo(SettingsCategory.SERVICES, "lastfm", Res.string.lastfm_integration),
        SettingsSectionInfo(SettingsCategory.SERVICES, "sponsor_block", Res.string.sponsorBlock),
        SettingsSectionInfo(SettingsCategory.STORAGE, "storage", Res.string.storage),
        SettingsSectionInfo(SettingsCategory.STORAGE, "backup", Res.string.backup),
        SettingsSectionInfo(SettingsCategory.ABOUT, "about_us", Res.string.settings_credits_header),
    )
