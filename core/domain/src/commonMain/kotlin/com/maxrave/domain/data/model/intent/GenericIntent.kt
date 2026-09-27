package com.maxrave.domain.data.model.intent

import com.eygraber.uri.Uri

data class GenericIntent(
    val action: String? = null,
    val data: Uri? = null,
    val type: String? = null,
    /**
     * Makes every delivery distinct. The intent travels through a StateFlow, which drops a value
     * equal to the current one — so opening the same link twice while the app was running did
     * nothing the second time.
     */
    val nonce: Long = nextNonce(),
) {
    private companion object {
        private var counter = 0L

        fun nextNonce(): Long = ++counter
    }
}