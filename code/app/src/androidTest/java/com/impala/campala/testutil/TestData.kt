// DISCLOSURE:
// I wasn't entirely sure how to do UI tests that integrated with Firestore, so I worked with Claude Opus 5.5 in the Claude Code harness
// After it helped me set up the first UI test (CreateReservationFlowTest), I prompted it with
// "Is there any work we can do to extract the boilerplate stuff to another file so that our test files themselves dont have to duplicate it"
// It generated the below scaffolding code for putting test data into the Firebase emulator, so that the tests I go on to write can be my own code, just using these helper functions

package com.impala.campala.testutil

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.impala.campala.UserViewMode
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.Socket
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Helpers for putting test data into the Firebase emulator before a UI test renders the app
//
// The emulator runs without security rules (see firebase.emulator.json), so these can write
// anything, including data owned by users that don't exist
//
// Typical @Before:
//     uid = signInAsNewEntrant()
//     seedCampsite(campsite)
//     seedLottery(wonLottery(campsite.id, winnerId = uid))

/** Owner ID used on seeded campsites/lotteries when the test doesn't care who owns them */
const val TEST_OWNER_ID = "ui-test-owner"

// --------------------------------------------------------------- database

/**
 * Deletes every document in the Firestore emulator, for tests that need to start from an empty database
 * (eg: checking that the search screen shows nothing)
 *
 * This also wipes anything you added by hand while running the app on the emulator
 *
 * It goes through a plain socket because Android blocks normal http:// requests by default,
 * and the emulator's "delete everything" endpoint isn't part of the Firestore SDK
 *
 * @throws IllegalStateException if the emulator doesn't say it worked
 * @throws java.io.IOException if the emulator can't be reached
 */
suspend fun clearFirestore() = withContext(Dispatchers.IO) {
    val projectId = FirebaseApp.getInstance().options.projectId
    Socket(EMULATOR_HOST, FIRESTORE_EMULATOR_PORT).use { socket ->
        val request = "DELETE /emulator/v1/projects/$projectId/databases/(default)/documents HTTP/1.1\r\n" +
            "Host: $EMULATOR_HOST:$FIRESTORE_EMULATOR_PORT\r\n" +
            "Content-Length: 0\r\n" +
            "Connection: close\r\n\r\n"
        socket.getOutputStream().apply {
            write(request.toByteArray())
            flush()
        }
        val statusLine = socket.getInputStream().bufferedReader().readLine() ?: ""
        check(statusLine.contains(" 200")) { "Clearing the Firestore emulator failed: $statusLine" }
    }
}

// Same address CampalaApplication points Firestore at
private const val EMULATOR_HOST = "127.0.0.1"
private const val FIRESTORE_EMULATOR_PORT = 8080

// ------------------------------------------------------------------ users

/**
 * Signs in as a brand new anonymous user who has already picked the entrant role,
 * so the app opens straight onto the entrant screens
 *
 * Each call makes a new user, so tests can't see each other's lotteries/reservations
 *
 * @param name Name saved on their entrant profile (shows up on the owner's lottery/reservation screens)
 * @return The new user's UID
 */
suspend fun signInAsNewEntrant(name: String = "Test Entrant"): String {
    val uid = signInAsNewUser()
    writeUserDoc(uid, UserViewMode.ENTRANT)
    EntrantProfileModel(userId = uid, name = name).save().await()
    return uid
}

/**
 * Signs in as a brand new anonymous user who has already picked the campsite owner role,
 * so the app opens straight onto the campsite owner screens
 *
 * Campsites and lotteries seeded with ownerId set to this UID show up as theirs
 *
 * @return The new user's UID
 */
suspend fun signInAsNewOwner(): String {
    val uid = signInAsNewUser()
    writeUserDoc(uid, UserViewMode.CAMPSITE_OWNER)
    Firebase.firestore.collection("campsiteOwners").document(uid)
        .set(mapOf("userId" to uid), SetOptions.merge()).await()
    return uid
}

/**
 * Makes an entrant profile for someone other than the signed in user, without signing in as them
 *
 * Useful for filling lotteries/waitlists/reservations with other people, eg: when testing the owner's screens
 *
 * @param name Name saved on their entrant profile
 * @return The made up user's UID
 */
@OptIn(ExperimentalUuidApi::class)
suspend fun createOtherEntrant(name: String): String {
    val uid = "other-entrant-" + Uuid.random()
    EntrantProfileModel(userId = uid, name = name).save().await()
    return uid
}

// Fresh anonymous user, the app will pick this user up when it starts since they're already signed in
private suspend fun signInAsNewUser(): String {
    Firebase.auth.signOut()
    return Firebase.auth.signInAnonymously().await().user!!.uid
}

// Saying they already have a profile skips the choose role screen
private suspend fun writeUserDoc(uid: String, role: UserViewMode) {
    val isOwner = role == UserViewMode.CAMPSITE_OWNER
    Firebase.firestore.collection("users").document(uid).set(
        mapOf(
            "isEntrant" to !isOwner,
            "isOwner" to isOwner,
            "hasCreatedEntrantProfile" to !isOwner,
            "hasActiveEntrantProfile" to !isOwner,
            "hasCreatedCampsiteOwnerProfile" to isOwner,
            "hasActiveCampsiteOwnerProfile" to isOwner,
            "currentRole" to role.name
        )
    ).await()
}

// -------------------------------------------------------- campsites / lotteries

/**
 * Saves a campsite, and if it has an ownerId, adds it to that owner's profile like the app does
 * (the owner's campsites screen only lists campsites in their profile)
 *
 * Leave the campsite's id as the random default, otherwise reservations left over from an
 * earlier run would already be booking the dates the test picks
 *
 * @param campsite The campsite to save
 * @return The same campsite, so it can be saved and assigned in one line
 */
suspend fun seedCampsite(campsite: CampsiteModel): CampsiteModel {
    CampsiteRepository().saveCampsite(campsite)
    if (campsite.ownerId.isNotBlank()) {
        addToOwnerProfile(campsite.ownerId, "ownedCampsites", campsite.id)
    }
    return campsite
}

/**
 * Saves a lottery, and adds it to its owner's profile like the app does
 *
 * Start from [openLottery] or [wonLottery] and use copy() to change what the test needs
 *
 * @param lottery The lottery to save
 * @return The same lottery, so it can be saved and assigned in one line
 */
suspend fun seedLottery(lottery: LotteryModel): LotteryModel {
    LotteryRepository().saveLottery(lottery)
    addToOwnerProfile(lottery.ownerId, "ownedLotteries", lottery.id)
    return lottery
}

/**
 * A lottery that is open for joining right now, with nobody in it yet
 *
 * Joining is open from yesterday until 10 days from now, then winners reserve over the next 10 days,
 * and the stays are in the month after next
 *
 * @param campsiteIds Campsites the lottery is for
 * @param ownerId Who owns the lottery
 */
fun openLottery(campsiteIds: List<String>, ownerId: String = TEST_OWNER_ID): LotteryModel {
    val today = todayUtc()
    val stayStart = today.plusMonths(2).withDayOfMonth(1)
    return LotteryModel(
        campsiteIds = campsiteIds.toMutableList(),
        lotteryStartTime = toTimestamp(today.minusDays(1)),
        lotteryEndTime = toTimestamp(today.plusDays(10)),
        registrationStartTime = toTimestamp(today.plusDays(10)),
        registrationEndTime = toTimestamp(today.plusDays(20)),
        reservationStartDate = toTimestamp(stayStart),
        reservationEndDate = toTimestamp(stayStart.plusDays(20)),
        lotteryCapacity = 10,
        waitlistCapacity = 5,
        ownerId = ownerId
    )
}

/**
 * A lottery that already drew with [winnerId] as the winner, and registration is open right now
 * so the winner can reserve
 *
 * @param campsiteId Campsite the lottery is for
 * @param winnerId Who won
 * @param stayStart First day that can be reserved, defaults to the 1st of next month so the date picker opens on a month we know
 * @param ownerId Who owns the lottery
 */
fun wonLottery(
    campsiteId: String,
    winnerId: String,
    stayStart: LocalDate = todayUtc().plusMonths(1).withDayOfMonth(1),
    ownerId: String = TEST_OWNER_ID
): LotteryModel {
    val today = todayUtc()
    return LotteryModel(
        campsiteIds = mutableListOf(campsiteId),
        lotteryStartTime = toTimestamp(today.minusDays(10)),
        lotteryEndTime = toTimestamp(today.minusDays(1)),
        registrationStartTime = toTimestamp(today.minusDays(1)),
        registrationEndTime = toTimestamp(today.plusDays(10)),
        reservationStartDate = toTimestamp(stayStart),
        reservationEndDate = toTimestamp(stayStart.plusDays(20)),
        lotteryCapacity = 1,
        // The user has to be in entrantIds too, since that's how the Lotteries tab finds their lotteries
        entrantIds = mutableListOf(winnerId),
        winnerIds = mutableListOf(winnerId),
        ownerId = ownerId
    )
}

// Merge so this works whether or not the owner has a profile yet
private suspend fun addToOwnerProfile(ownerId: String, field: String, id: String) {
    Firebase.firestore.collection("campsiteOwners").document(ownerId)
        .set(mapOf("userId" to ownerId, field to FieldValue.arrayUnion(id)), SetOptions.merge())
        .await()
}

// ------------------------------------------------------------------ dates

/** Today's date in UTC, the same day the app uses when it saves dates */
fun todayUtc(): LocalDate = LocalDate.now(ZoneOffset.UTC)

/** Same conversion the app uses, dates are stored as midnight UTC */
fun toTimestamp(date: LocalDate): Timestamp {
    return Timestamp(date.atStartOfDay(ZoneOffset.UTC).toInstant())
}
