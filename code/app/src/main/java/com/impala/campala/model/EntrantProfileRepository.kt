package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.impala.campala.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Repository for storing the app's entrant profiles.
 *
 * Entrant profiles are stored under the same ID as the User they belong to
 * So you can search for an entrant profile with the User ID
 *
 * Deletion/Banning behaviour is TBD, but what I'm thinking is that we never DELETE the EntrantProfileRecord,
 * we just set hasActiveEntrantProfile to false on the user and then maybe add a flag to prevent it from being displayed in results
 */
class EntrantProfileRepository {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Gets a single entrant profile by its user's ID
     *
     * @param userId UID of the User whose entrant profile to fetch
     *
     * @return The matching entrant profile or null if nothing or if error
     */
    suspend fun getEntrantProfileById(userId: String): EntrantProfileModel? {
        return try {
            val snapshot = db.collection("entrants").document(userId).get().await()
            snapshot.toObject(EntrantProfileModel::class.java) // null if the document doesn't exist
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Creates the entrant profile for a user, and marks the user as having one
     *
     * @param user The User object the profile is being made for
     * @param profile The new profile model object, its userId gets set to the user's UID so you don't have to do that yourself
     */
    suspend fun createEntrantProfile(user: User, profile: EntrantProfileModel): Unit {
        check(!user.getHasCreatedEntrantProfile()) { "User already has an entrant profile" }

        profile.userId = user.getUID()
        profile.save().await()

        user.setHasCreatedEntrantProfile(true)
        user.setHasActiveEntrantProfile(true)
        user.updateUserProfile(user.getUID())
    }

    /**
     * Saves an entrant profile to the database
     *
     * Used for updating an existing profile, use createEntrantProfile for new ones
     *
     * @param profile EntrantProfileModel to be saved
     */
    suspend fun saveEntrantProfile(profile: EntrantProfileModel): Unit {
        profile.save().await()
    }

    /**
     * Records that a entrant is in a lottery
     *
     * Whether an entrant has joined the waitlist OR the lottery, this should be called
     * To know which one an entrant is in, you can get the list of lotteryIDs and then check the lottery objects themselves
     *
     * @param profile EntrantProfileModel to be saved
     */
    suspend fun putEntrantInLottery(profile: EntrantProfileModel, lotteryId: String) {
        profile.enteredLotteries.add(lotteryId)
        this.saveEntrantProfile(profile)
    }
}
