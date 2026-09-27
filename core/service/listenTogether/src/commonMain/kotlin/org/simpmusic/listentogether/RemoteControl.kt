package org.simpmusic.listentogether

/**
 * Letting a guest control playback without handing over the room.
 *
 * The server obeys transport commands from the host alone, and the wire format is Metrolist's, so
 * nothing new can be added to it. What does exist is a private channel each way between a guest
 * and the host:
 *  - guest → host: `suggest_track`, which the server forwards to the host only;
 *  - host → guest: `reject_suggestion`, whose `reason` the server hands back to that guest.
 *
 * A command is therefore a suggestion whose track id carries a marker, and the host's app — not
 * the server — decides whether to carry it out: it plays the command on its own player if the
 * sender is one of its controllers, which the room then follows like any host action. Either way
 * the suggestion is rejected with a reason telling the guest whether it had control, so nothing
 * lingers in the server's pending list.
 *
 * The human-readable title is deliberate: a host on a client that does not know this scheme sees
 * an understandable suggestion ("⏭ Skip") rather than garbage.
 */
object RemoteControl {
    private const val MARKER = "sm-ctl:"

    const val PLAY = "play"
    const val PAUSE = "pause"
    const val NEXT = "next"
    const val PREVIOUS = "previous"
    const val SEEK = "seek"

    /** Play this track now, for the room. The track rides along in the payload. */
    const val PLAY_NOW = "playnow"

    const val REPLY_OK = "sm-ctl-ok"
    const val REPLY_NOT_ALLOWED = "sm-ctl-not-allowed"

    data class Command(
        val fromUserId: String,
        val command: String,
        val arg: String,
        /** For [PLAY_NOW]: the track to play, with its real id. */
        val track: TrackInfo?,
    )

    /** A transport command: the marker is the whole id, since there is no real track. */
    fun isControl(info: TrackInfo): Boolean = info.id.startsWith(MARKER)

    /**
     * A "play this now" request. It carries the REAL track id and puts the marker in `album`, so a
     * host that does not know this scheme sees, and can approve, an ordinary suggestion.
     */
    fun isPlayNow(info: TrackInfo): Boolean = info.album == MARKER + PLAY_NOW

    fun encode(
        command: String,
        arg: String,
        track: TrackInfo?,
    ): TrackInfo =
        if (command == PLAY_NOW && track != null) {
            track.copy(album = MARKER + PLAY_NOW)
        } else {
            TrackInfo(id = MARKER + command, title = labelFor(command), album = arg)
        }

    fun decode(
        fromUserId: String,
        info: TrackInfo,
    ): Command? =
        when {
            isPlayNow(info) -> Command(fromUserId, PLAY_NOW, "", playableTrack(info))
            isControl(info) -> Command(fromUserId, info.id.removePrefix(MARKER), info.album, null)
            else -> null
        }

    /** The track of a play-now request, with the marker taken back out. */
    fun playableTrack(info: TrackInfo): TrackInfo = info.copy(album = "")

    private fun labelFor(command: String): String =
        when (command) {
            PLAY -> "▶ Play"
            PAUSE -> "⏸ Pause"
            NEXT -> "⏭ Skip"
            PREVIOUS -> "⏮ Previous"
            SEEK -> "⏩ Seek"
            else -> "Control: $command"
        }
}
