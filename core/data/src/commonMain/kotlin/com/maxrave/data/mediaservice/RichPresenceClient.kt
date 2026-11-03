package com.maxrave.data.mediaservice

import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.repository.DiscordRepository
import com.my.kizzy.DiscordRPC
import org.simpmusic.discord.DiscordRpc

/**
 * The Rich Presence connection a playback handler holds. A Full build talks to Discord through
 * [DiscordRpc] with an OAuth token; a FOSS build keeps kizzy's [DiscordRPC] and the user's token.
 */
internal interface RichPresenceClient {
    /**
     * [activeSession] is the fork's Rich presence priority setting: true marks the session as
     * active (not afk), so Discord shows it even while the Discord app is open on the phone.
     */
    suspend fun updateSong(
        progressMs: Long,
        durationMs: Long,
        speed: Float,
        song: SongEntity,
        activeSession: Boolean,
    ): Result<Unit>

    fun isRpcRunning(): Boolean

    fun closeRPC()
}

/** Null when there is no token to connect with. */
internal suspend fun createRichPresenceClient(discordRepository: DiscordRepository): RichPresenceClient? {
    if (discordRepository.isOAuth) {
        val rpc = DiscordRpc { forceRefresh -> discordRepository.accessToken(forceRefresh) }
        return object : RichPresenceClient {
            override suspend fun updateSong(
                progressMs: Long,
                durationMs: Long,
                speed: Float,
                song: SongEntity,
                activeSession: Boolean,
            ) = rpc.updateSong(progressMs, durationMs, speed, song, activeSession)

            override fun isRpcRunning() = rpc.isRpcRunning()

            override fun closeRPC() = rpc.closeRPC()
        }
    }
    val token = discordRepository.accessToken() ?: return null
    val rpc = DiscordRPC(token)
    return object : RichPresenceClient {
        override suspend fun updateSong(
            progressMs: Long,
            durationMs: Long,
            speed: Float,
            song: SongEntity,
            activeSession: Boolean,
        ) = rpc.updateSong(progressMs, durationMs, speed, song, activeSession)

        override fun isRpcRunning() = rpc.isRpcRunning()

        override fun closeRPC() = rpc.closeRPC()
    }
}
