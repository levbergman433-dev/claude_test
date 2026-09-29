package com.maxrave.kotlinytmusicscraper.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ThumbnailRenderer(
    @SerialName("musicThumbnailRenderer")
    @JsonNames("croppedSquareThumbnailRenderer")
    private val stillThumbnailRenderer: MusicThumbnailRenderer?,
    val musicAnimatedThumbnailRenderer: MusicAnimatedThumbnailRenderer?,
    val croppedSquareThumbnailRenderer: MusicThumbnailRenderer?,
) {
    /**
     * The still cover. Items with an animated cover (YouTube Music's Supermix, Discover and Archive
     * mixes among them) send only `musicAnimatedThumbnailRenderer`, whose `backupRenderer` holds the
     * still image; without this fallback they arrived with no artwork at all. Every caller reads
     * this property, so all of them get the fallback.
     */
    val musicThumbnailRenderer: MusicThumbnailRenderer?
        get() = stillThumbnailRenderer ?: croppedSquareThumbnailRenderer ?: musicAnimatedThumbnailRenderer?.backupRenderer

    @Serializable
    data class MusicThumbnailRenderer(
        val thumbnail: Thumbnails,
        val thumbnailCrop: String?,
        val thumbnailScale: String?,
    ) {
        fun getThumbnailUrl() = thumbnail.thumbnails.lastOrNull()?.url
    }

    @Serializable
    data class MusicAnimatedThumbnailRenderer(
        val animatedThumbnail: Thumbnails,
        val backupRenderer: MusicThumbnailRenderer,
    )
}