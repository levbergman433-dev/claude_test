package com.maxrave.kotlinytmusicscraper.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
     *
     * A stored value worked out once, not a getter: callers smart-cast it after a null check, which
     * the compiler refuses for a property with a custom getter. @Transient keeps it out of the
     * serialized form; its initializer still runs when an instance is decoded.
     */
    @Transient
    val musicThumbnailRenderer: MusicThumbnailRenderer? =
        stillThumbnailRenderer ?: croppedSquareThumbnailRenderer ?: musicAnimatedThumbnailRenderer?.backupRenderer

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