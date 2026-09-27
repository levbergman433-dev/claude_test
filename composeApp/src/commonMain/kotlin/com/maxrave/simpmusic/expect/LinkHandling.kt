package com.maxrave.simpmusic.expect

/**
 * Whether this platform has a system page where the user decides which app opens web links —
 * the only way to make YouTube / YouTube Music links open here, since those domains can only be
 * verified by Google.
 */
expect fun supportsLinkHandlingSettings(): Boolean

/** Opens that system page for this app. */
expect fun openLinkHandlingSettings()
