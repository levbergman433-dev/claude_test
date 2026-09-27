package com.maxrave.simpmusic.ui.component

/**
 * How many times a long title in a LIST scrolls before it rests.
 *
 * These used to scroll forever. An endless marquee redraws every frame for as long as its row is
 * on screen, so a page of long titles kept the display at full refresh indefinitely — and with
 * liquid glass on, each of those frames also re-rendered the nav bar and mini-player glass that
 * refract the page. Twice is enough to read the whole title.
 */
const val LIST_MARQUEE_ITERATIONS = 2

/**
 * Pause between loops for the titles that keep scrolling (player, mini player). Nothing is drawn
 * during the pause, so this directly cuts the frames those marquees cost.
 */
const val PLAYER_MARQUEE_REPEAT_DELAY_MS = 3_000
