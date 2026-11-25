package com.impala.campala.model

//import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Stores a single message between owner and entrant
 *
 * Used only by MessagesModel as every message is always part of a thread
 */
data class MessageModel(
    val body: String = "",
    val fromOwner: Boolean = false,
    val date: Instant // Uses LocalDateTime for simplicity Stores UTC. Be careful: Instant type should be used when grabbing time, as instant always defaults to UTC.
) {
    public fun localDate(): LocalDateTime {
        return this.date.toLocalDateTime(TimeZone.currentSystemDefault())
    }
}