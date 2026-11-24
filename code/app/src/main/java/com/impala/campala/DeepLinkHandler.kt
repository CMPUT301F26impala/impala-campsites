package com.impala.campala

import android.net.Uri

/**
 * Builds and reads the campala://CAMPSITE-ID deep links
 */
object DeepLinkHandler {
    const val SCHEME = "campala"

    /**
     * Builds the deep link for a campsite, this is what should go in the campsite's QR code
     *
     * @param campsiteId The ID of the campsite the link should open
     * @return The link, e.g. campala://abc123
     */
    fun createCampsiteLinkUrl(campsiteId: String): String = "$SCHEME://${Uri.encode(campsiteId)}"

    /**
     * Gets the campsite ID out of a campsite deep link
     *
     * @param uri The link the app was opened with, usually intent.data
     * @return The campsite ID, or null if the link is missing or isn't a campala:// link
     */
    fun parseCampsiteId(uri: Uri?): String? {
        if (uri == null || uri.scheme != SCHEME) return null
        // The campsite ID is the part right after campala://
        return uri.authority?.takeIf { it.isNotBlank() }
    }
}
