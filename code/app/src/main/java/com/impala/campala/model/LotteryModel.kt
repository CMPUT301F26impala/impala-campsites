package com.impala.campala.model

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.firestore
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A lottery for reserving stays at one or more campsites
 *
 * How the dates work is outlined below
 *  1. Entrants join between the time of creation and up until [lotteryEndTime]. At that time winners are drawn
 *  // TODO: AT THIS POINT THERE IS NO AUTO-DRAW, JUST A MANUAL OPTION FOR THAT
 *  2. Winners have from the time of drawing until [registrationEndTime] to reserve their campsite dates
 *  3. The dates they can reserve fall between [reservationStartDate] and [reservationEndDate]
 *
 * If the lottery is full, people go on the waitlist instead. When an entrant leaves,
 * the earliest person on the waitlist gets moved into the lottery.
 *
 * @property campsiteIds IDs of the campsites this lottery is for, must have at least one
 * @property lotteryEndTime When the lottery closes and winners are drawn
 * @property registrationEndTime When winners must have reserved their dates by
 * @property reservationStartDate First day that can be reserved
 * @property reservationEndDate Last day that can be reserved
 * @property lotteryCapacity Max number of entrants in the lottery
 * @property entrantIds IDs of people entered in the lottery
 * @property waitlistCapacity Max number of people on the waitlist
 * @property waitlistIds IDs of people on the waitlist, in the order they joined (earliest first)
 * @property ownerId ID of the campsite owner who created the lottery
 */
data class LotteryModel @OptIn(ExperimentalUuidApi::class) constructor(
    var campsiteIds: MutableList<String> = mutableListOf(),
    var lotteryEndTime: Timestamp = Timestamp.now(),
    var registrationEndTime: Timestamp = Timestamp.now(),
    var reservationStartDate: Timestamp = Timestamp.now(),
    var reservationEndDate: Timestamp = Timestamp.now(),
    var lotteryCapacity: Int = 0,
    var entrantIds: MutableList<String> = mutableListOf(),
    var waitlistCapacity: Int = 0,
    var waitlistIds: MutableList<String> = mutableListOf(),
    var ownerId: String = "",
    var id: String = Uuid.random().toString()
) {

    // This is just a helper so that we don't need a boolean flag for each of these states
    // A user's state in the
    enum class JoinResult {
        JOINED_LOTTERY,
        JOINED_WAITLIST,
        ALREADY_JOINED,
        FULL
    }

    // This is the return value of evaluateUserAction
    // It basically returns what hitting the "join button" WOULD do
    enum class ProspectiveJoinStatus {
        CAN_JOIN_LOTTERY,
        CAN_JOIN_WAITLIST,
        ALREADY_IN_LOTTERY,
        ALREADY_IN_WAITLIST,
        FULL
    }

    companion object {
        fun load(doc: DocumentSnapshot): LotteryModel {
            return doc.toObject(LotteryModel::class.java)!!
        }
    }

    @get:Exclude
    var doc: DocumentReference? = null
    fun save(): Task<Void> {
        validate()
        doc = Firebase.firestore.document("lotteries/" + id)
        return doc!!.set(this)
    }

    // Functions are marked @Exclude so Firestore doesn't try to save them as fields

    /**
     * This can be used in the form screen to return a message to the user as to why their current interim lottery is not good to be saved
     * But it does throw, so be mindful of that
     * @throws IllegalArgumentException if something is wrong, you can try catch this to get the error message
     * @return Returns nothing if there was a successful validation
     */
    @Exclude
    fun validate() {
        require(ownerId.isNotBlank()) { "A lottery must have an owner" }
        require(campsiteIds.isNotEmpty()) { "A lottery must be linked to at least one campsite" }
        require(lotteryEndTime <= registrationEndTime) { "Registration can't end before winners are drawn" }
        require(reservationStartDate <= reservationEndDate) { "Reservation range can't end before it starts" }
        require(lotteryCapacity > 0) { "Lottery capacity must be at least 1" }
        require(waitlistCapacity >= 0) { "Waitlist capacity can't be negative" }
        require(entrantIds.size <= lotteryCapacity) { "More entrants than the lottery capacity" }
        require(waitlistIds.size <= waitlistCapacity) { "More people on the waitlist than its capacity" }
    }

    /**
     * This is basically a function to figure out "What should the 'Join' button do
     *
     * It returns what action that entrant can take, are they already in the lottery or waitlist, can they join either, or is it full
     *
     * @param entrantId ID of the person joining
     * @return A ProspectiveJoinStatus enum, which is one of ALREADY_IN_LOTTERY, ALREADY_IN_WAITLIST, CAN_JOIN_LOTTERY, CAN_JOIN_WAITLIST, or FULL
     */
    @Exclude
    fun evaluateUserAction(entrantId: String): ProspectiveJoinStatus {
        if (entrantId in entrantIds) {
            return ProspectiveJoinStatus.ALREADY_IN_LOTTERY
        }
        if (entrantId in waitlistIds) {
            return ProspectiveJoinStatus.ALREADY_IN_WAITLIST
        }
        if (entrantIds.size < lotteryCapacity) {
            return ProspectiveJoinStatus.CAN_JOIN_LOTTERY
        }
        if (waitlistIds.size < waitlistCapacity) {
            return ProspectiveJoinStatus.CAN_JOIN_WAITLIST
        }
        return ProspectiveJoinStatus.FULL
    }

    /**
     * Either adds to the waitlist, or the lottery
     *
     * This doesn't actually call .save on it, that's because this is intended to be used only by the LotteryRepository
     * The repository will have a method to add an entrant to a campsite, and it will take that campsite, call this method on it, and then call save
     *
     * @param entrantId ID of the person joining
     * @return A JoinResult enum which is where they ended up
     */
    @Exclude
    fun addEntrant(entrantId: String): JoinResult {
        if (entrantId in entrantIds || entrantId in waitlistIds) {
            return JoinResult.ALREADY_JOINED
        }
        if (entrantIds.size < lotteryCapacity) {
            entrantIds.add(entrantId)
            return JoinResult.JOINED_LOTTERY
        }
        if (waitlistIds.size < waitlistCapacity) {
            waitlistIds.add(entrantId)
            return JoinResult.JOINED_WAITLIST
        }
        return JoinResult.FULL
    }

    /**
     * Removes someone from the lottery or the waitlist
     *
     * If they were in the lottery, the earliest person on the waitlist takes their spot
     *
     * This doesn't actually call .save on it, that's because this is intended to be used only by the LotteryRepository
     * The repository will have a method to add an entrant to a campsite, and it will take that campsite, call this method on it, and then call save
     *
     * @param entrantId ID of the person leaving
     * @return False if they weren't in the lottery or the waitlist
     */
    @Exclude
    fun removeEntrant(entrantId: String): Boolean {
        if (entrantIds.remove(entrantId)) {
            if (waitlistIds.isNotEmpty()) {
                entrantIds.add(waitlistIds.removeAt(0))
            }
            return true
        }
        return waitlistIds.remove(entrantId)
    }
}
