package com.maxrave.simpmusic.ui.navigation.destination.home

import kotlinx.serialization.Serializable

/** [code] is set when the screen is opened from an invite link: the room to join. */
@Serializable
data class ListenTogetherDestination(
    val code: String? = null,
)
