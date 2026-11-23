package com.impala.campala.model

/**
 * Stores a single message between owner and entrant
 *
 * Used only by MessagesModel as every message is always part of a thread
 */
data class MessageModel(
    val body: String = "",
    val isFromOwner: Boolean = false
) {
}