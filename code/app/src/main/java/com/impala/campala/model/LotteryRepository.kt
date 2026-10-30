package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Repository for querying lotteries by useful parameters (eg: by ID, or by campsiteId)
 *
 * Anything that needs to find a lottery or modify it can do so through this
 *
 * Everything here is a suspend fun, see the comment in CampsiteRepository for how to call these
 */
class LotteryRepository {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Gets every lottery
     *
     * @return List of all lotteries
     */
    suspend fun getLotteries(): List<LotteryModel> {
        val snapshot = db.collection("lotteries").get().await()
        return snapshot.toObjects(LotteryModel::class.java)
    }

    /**
     * Gets the lottery for a campsite if there is one
     *
     * There should only ever be one lottery on a campsite at a time, so this doesn't really handle the case of there being multiple, it's assumed there is logic to prevent that from happening
     *
     * @param campsiteId ID of the campsite
     *
     * @return The campsite's lottery, or null if it doesn't have one or the fetch fails
     */
    suspend fun getLotteryByCampsiteId(campsiteId: String): LotteryModel? {
        return try {
            val snapshot = db.collection("lotteries").whereArrayContains("campsiteIds", campsiteId).limit(1).get().await()
            snapshot.toObjects(LotteryModel::class.java).firstOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Gets every lottery created by an owner, used so campsite owners can see only their own lotteries
     *
     * @param ownerId ID of the campsite owner
     *
     * @return List of that owner's lotteries
     */
    suspend fun getLotteriesByOwnerId(ownerId: String): List<LotteryModel> {
        val snapshot = db.collection("lotteries").whereEqualTo("ownerId", ownerId).get().await()
        return snapshot.toObjects(LotteryModel::class.java)
    }

    /**
     * Gets a single lottery by its ID
     *
     * @param id the LOTTERY id of the lottery to fetch
     *
     * @return The matching lottery, or null if it doesn't exist or the fetch fails
     */
    suspend fun getLotteryById(id: String): LotteryModel? {
        return try {
            val snapshot = db.collection("lotteries").document(id).get().await()
            snapshot.toObject(LotteryModel::class.java) // null if the document doesn't exist
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Saves a lottery to the database, creating it if it doesn't exist yet
     *
     * Don't use this for joining/leaving, use joinLottery/leaveLottery
     *
     * @param lottery LotteryModel to be saved
     * @throws IllegalArgumentException if any of the parameters are not good
     */
    suspend fun saveLottery(lottery: LotteryModel): Unit {
        lottery.save().await()
    }

    /**
     * Deletes lottery from database
     *
     * @param lottery LotteryModel to delete
     */
    suspend fun deleteLottery(lottery: LotteryModel): Unit {
        db.collection("lotteries").document(lottery.id).delete().await()
    }

    /**
     * Adds someone to a lottery, or its waitlist if the lottery is full, and returns the status
     *
     * @param lotteryId ID of the lottery to join
     * @param entrantId ID of the person joining
     *
     * @return Where they ended up
     * @throws IllegalArgumentException If the lottery ID points to nothing
     */
    suspend fun joinLottery(lotteryId: String, entrantId: String): LotteryModel.JoinResult {
        val lottery = getLotteryById(lotteryId)
            ?: throw IllegalArgumentException("Lottery $lotteryId doesn't exist")

        val result = lottery.addEntrant(entrantId)
        saveLottery(lottery)
        return result
    }

    /**
     * Removes someone from a lottery or its waitlist
     * And also handles bumping the next person on the waitlist down
     *
     * @param lotteryId ID of the lottery to leave
     * @param entrantId ID of the person leaving
     *
     * @return False if they weren't in the lottery or the waitlist
     * @throws IllegalArgumentException if the lottery doesn't exist or isn't valid
     */
    suspend fun leaveLottery(lotteryId: String, entrantId: String): Boolean {
        val lottery = getLotteryById(lotteryId)
            ?: throw IllegalArgumentException("Lottery $lotteryId doesn't exist")

        val removed = lottery.removeEntrant(entrantId)
        saveLottery(lottery)
        return removed
    }
}
