package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.impala.campala.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Repository for storing the app's campsite owner profiles.
 *
 * Campsite owner profiles are stored under the same ID as the User they belong to
 * So you can search for a campsite owner profile with the User ID
 */
object CampsiteOwnerProfileRepository {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Gets a single campsite owner profile by its user's ID
     *
     * @param userId UID of the User whose campsite owner profile to fetch
     *
     * @return The matching campsite owner profile or null if there was none, or if there was an error
     */
    suspend fun getCampsiteOwnerProfileById(userId: String): CampsiteOwnerProfileModel? {
        return try {
            val snapshot = db.collection("campsiteOwners").document(userId).get().await()
            snapshot.toObject(CampsiteOwnerProfileModel::class.java) // null if the document doesn't exist
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Creates the campsite owner profile for a user, and marks the user as having one
     *
     * @param user The User object the profile is being made for
     * @param profile The new profile model object, its userId gets set to the user's UID so you don't have to do that yourself
     *
     * @throws IllegalStateException if the user already has a campsite owner profile
     * @throws FirebaseFirestoreException if the write fails
     */
    suspend fun createCampsiteOwnerProfile(user: User, profile: CampsiteOwnerProfileModel): Unit {
        check(!user.getHasCreatedCampsiteOwnerProfile()) { "User already has a campsite owner profile" }

        profile.userId = user.getUID()
        profile.save().await()

        user.setHasCreatedCampsiteOwnerProfile(true)
        user.setHasActiveCampsiteOwnerProfile(true)
        user.updateUserProfile(user.getUID())
    }

    /**
     * Saves a campsite owner profile to the database
     *
     * Used for updating an existing profile, use createCampsiteOwnerProfile for new ones
     *
     * @param profile CampsiteOwnerProfileModel to be saved
     *
     * @throws FirebaseFirestoreException if the write fails
     */
    suspend fun saveCampsiteOwnerProfile(profile: CampsiteOwnerProfileModel): Unit {
        profile.save().await()
    }

    /**
     * Records that a campsite owner owns a campsite
     *
     * @param profile CampsiteOwnerProfileModel to be saved
     * @param campsiteId ID of the campsite the owner now owns
     *
     * @throws FirebaseFirestoreException if the write fails
     */
    suspend fun addOwnedCampsite(profile: CampsiteOwnerProfileModel, campsiteId: String) {
        profile.ownedCampsites.add(campsiteId)
        this.saveCampsiteOwnerProfile(profile)
    }

    /**
     * Records that a campsite owner owns a lottery
     *
     * @param profile CampsiteOwnerProfileModel to be saved
     * @param lotteryId ID of the lottery the owner now owns
     *
     * @throws FirebaseFirestoreException if the write fails
     */
    suspend fun addOwnedLottery(profile: CampsiteOwnerProfileModel, lotteryId: String) {
        profile.ownedLotteries.add(lotteryId)
        this.saveCampsiteOwnerProfile(profile)
    }

    /**
     * Records that a campsite owner no longer owns a campsite
     *
     * @param profile CampsiteOwnerProfileModel to be saved
     * @param campsiteId ID of the campsite the owner no longer owns
     *
     * @throws FirebaseFirestoreException if the write fails
     */
    suspend fun removeOwnedCampsite(profile: CampsiteOwnerProfileModel, campsiteId: String) {
        profile.ownedCampsites.remove(campsiteId)
        this.saveCampsiteOwnerProfile(profile)
    }

    /**
     * Bans a campsite owner, deletes their campsites, and marks them as banned
     * The next time they open the app they're sent to the choose role screen, and can't pick campsite owner
     *
     * @param userId UID of the campsite owner to ban
     *
     * @throws FirebaseFirestoreException if any of the reads or writes fail
     */
    suspend fun banCampsiteOwner(userId: String): Unit {
        for (campsite in CampsiteRepository.getCampsitesByOwnerId(userId)) {
            CampsiteRepository.softDeleteCampsite(campsite)
        }
        db.collection("users").document(userId)
            .set(mapOf("hasActiveCampsiteOwnerProfile" to false), SetOptions.merge()).await()
    }
}
