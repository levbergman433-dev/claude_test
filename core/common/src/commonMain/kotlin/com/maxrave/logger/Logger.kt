package com.maxrave.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

object Logger {
    private val logger = Logger

    /**
     * Debug-level logging and full HTTP request/response logging. On by default so Desktop and
     * tests keep today's output; Android release builds switch it off at startup (before Koin
     * builds any HTTP client), because formatting and printing every response body — YouTube
     * Music pages are hundreds of KB of JSON — measurably slows down every page load.
     */
    var isVerbose: Boolean = true
        set(value) {
            field = value
            logger.setMinSeverity(if (value) Severity.Verbose else Severity.Warn)
        }

    // Tags suppressed at all log levels. Add a tag here to silence its logs globally.
    private val mutedTags =
        setOf(
            "DiscordWebSocket",
        )

    private fun isMuted(tag: String): Boolean = tag in mutedTags

    fun d(
        tag: String,
        message: String,
    ) {
        if (!isVerbose || isMuted(tag)) return
        logger.d(
            tag = tag,
            message = {
                message
            },
        )
    }

    fun i(
        tag: String,
        message: String,
    ) {
        if (!isVerbose || isMuted(tag)) return
        logger.i(tag = tag, message = { message })
    }

    fun w(
        tag: String,
        message: String,
    ) {
        if (isMuted(tag)) return
        logger.w(tag = tag, message = { message })
    }

    fun e(
        tag: String,
        message: String,
        e: Throwable? = null,
    ) {
        if (isMuted(tag)) return
        logger.e(throwable = e, tag = tag, message = { message })
    }
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
}