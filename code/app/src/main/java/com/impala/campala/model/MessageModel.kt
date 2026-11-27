package com.impala.campala.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
//import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Stores a single message between owner and entrant
 *
 * Used only by MessagesModel as every message is always part of a thread
 */
data class MessageModel(
    val body: String = "",
    val fromOwner: Boolean = false,
    // Disclosure: I (Logan) was reviewing this PR, and I found a bug where saving dates wasn't working
    // I traced this back to the fact that this type uses a kotlinx.datetime, but Firestore expected a firebase.Timestamp
    // I didn't entirely know how to fix this, and I didn't want to rewrite the code to use Timestamp everywhere because I felt like that was stepping on Liam's toes
    // So I prompted Claude Opus 5.5 explaining what I just said above with "What's the simplest way to preserve all of the existing code and just do the translation between the two date types in the Model"
    // It suggested the follow :Exclude syntax and the below @get and @set syntax
    // Which I then used to create the var firestoreData variable myself.
    @get:Exclude @set:Exclude
    var date: Instant = Clock.System.now() // Uses LocalDateTime for simplicity Stores UTC. Be careful: Instant type should be used when grabbing time, as instant always defaults to UTC.
) {
    public fun localDate(): LocalDateTime {
        return this.date.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    /**
     * A helper to convert so that Firestore stores times as Firestore Timestamp, but client reads it as kotlinx.datetime
     */
    @get:PropertyName("date") @set:PropertyName("date")
    var firestoreDate: Timestamp
        get() = Timestamp(date.epochSeconds, date.nanosecondsOfSecond)
        set(value) { date = Instant.fromEpochSeconds(value.seconds, value.nanoseconds) }
}