package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.impala.campala.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Repository for storing the app's entrant profiles.
 *
 * Entrant profiles are stored under the same ID as the User they belong to
 * So you can search for an entrant profile with the User ID
 *
 * Deletion/Banning behaviour is TBD, but what I'm thinking is that we never DELETE the EntrantProfileRecord,
 * we just set hasActiveEntrantProfile to false on the user and then maybe add a flag to prevent it from being displayed in results
 */
object EntrantProfileRepository {
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
     * finds user who have either an active entrant or campsite owner profile, and then gets their details
     *
     * @return A list containing active profiles which admin can see later
     */
    suspend fun getAdminProfiles(): List<AdminProfileModel> {
        val allUserDocuments = db.collection("users").get().await()
        val allEntrantProfiles = db.collection("entrants").get().await().toObjects(EntrantProfileModel::class.java)

        val entrantProfilesByUserId = allEntrantProfiles.associateBy { it.userId }

        val activeAdminDisplayProfiles = mutableListOf<AdminProfileModel>()

        for (currentUserDocument in allUserDocuments.documents) {
            val hasActiveEntrantProfile = currentUserDocument.getBoolean("hasActiveEntrantProfile") == true
            val hasActiveCampsiteOwnerProfile = currentUserDocument.getBoolean("hasActiveCampsiteOwnerProfile") == true
            // dont have entrant and campsite owner profile
            if (!hasActiveEntrantProfile && !hasActiveCampsiteOwnerProfile) { continue }

            val matchingEntrantProfile = entrantProfilesByUserId[currentUserDocument.id]
            var profileDisplayName = ""
            var profileEmailAddress = ""
            var profilePhoneNumber = ""
            var profilePictureImageId = ""

            // if the matching profile exists then update with that data or and if it doesn;t we use the saved display name
            if (matchingEntrantProfile != null) {
                profileDisplayName = matchingEntrantProfile.name
                profileEmailAddress = matchingEntrantProfile.email
                profilePhoneNumber = matchingEntrantProfile.phoneNumber
                profilePictureImageId = matchingEntrantProfile.imageId
            } else {
                val savedUserDisplayName = currentUserDocument.getString("userName")
                if (savedUserDisplayName != null) { profileDisplayName = savedUserDisplayName }
            }

            val currentAdminDisplayProfile = AdminProfileModel(
                userId = currentUserDocument.id,
                name = profileDisplayName,
                email = profileEmailAddress,
                phoneNumber = profilePhoneNumber,
                imageId = profilePictureImageId,
                hasAnActiveEntrantProfile = hasActiveEntrantProfile,
                hasAnActiveCampsiteOwnerProfile = hasActiveCampsiteOwnerProfile
            )
            activeAdminDisplayProfiles.add(currentAdminDisplayProfile)
        }
        return activeAdminDisplayProfiles
    } // end of get admin profiles function for personal reference

    /**
     * Deletes an entrant profile by marking it as inactive and removes the user from unfinished lotteries and cancelling their pending reservations
     *
     * @param userId ID of the user being deleted
     */
    suspend fun deleteProfile(userId: String) {
        require(userId.isNotBlank())
        val currentlyAuthenticatedUserId = FirebaseAuth.getInstance().currentUser?.uid
        check(currentlyAuthenticatedUserId != null) { "No signed in user" }

        // check if they are actually an admin
        if (currentlyAuthenticatedUserId != userId) {
            val authenticatedUserAdminDocument = db.collection("admins").document(currentlyAuthenticatedUserId).get().await()
            check(authenticatedUserAdminDocument.exists()) { "Admin permissions required" }
        }

        val userDocumentToDeactivate = db.collection("users").document(userId)
        val userDetailsBeforeDeletion = userDocumentToDeactivate.get().await()
        check(userDetailsBeforeDeletion.exists()) { "User does not exist" }

        if (userDetailsBeforeDeletion.getBoolean("hasActiveEntrantProfile") != true) { return }

        val allReservationsOfSelectedUser = ReservationRepository.getReservationsByUser(userId)
        val allRetrievedLotteryRecords = LotteryRepository.getLotteries()

        // handing pending reservations
        for (selectedUserReservation in allReservationsOfSelectedUser) {
            if (!selectedUserReservation.hasBeenCancelled && !selectedUserReservation.isPast) { ReservationRepository.flagAsCancelled(selectedUserReservation.id) }
        }

        val currentCalendarDate = LocalDate.now()
        val currentDateAndTimeStamp = Timestamp.now()

        // remove unfinished lotteries
        for (lotteryRecordBeingChecked in allRetrievedLotteryRecords) {
            val lastAvailableLotteryBookingDate = lotteryRecordBeingChecked.registrationEndTime.toInstant().atZone(ZoneOffset.UTC).toLocalDate()
            if (currentCalendarDate.isAfter(lastAvailableLotteryBookingDate)) { continue }

            if (currentDateAndTimeStamp < lotteryRecordBeingChecked.lotteryEndTime ||
                lotteryRecordBeingChecked.winnerIds.isEmpty()) {
                if (userId in lotteryRecordBeingChecked.entrantIds || userId in lotteryRecordBeingChecked.waitlistIds) {
                    LotteryRepository.leaveLottery(lotteryRecordBeingChecked.id, userId)
                }
            } else if (userId in lotteryRecordBeingChecked.winnerIds && userId !in lotteryRecordBeingChecked.declinedIds) {
                val hasUserPreviouslyReservedThroughLottery = allReservationsOfSelectedUser.any { it.lotteryId == lotteryRecordBeingChecked.id }
                if (!hasUserPreviouslyReservedThroughLottery) { LotteryRepository.declineWin(lotteryRecordBeingChecked.id, userId) }
            }
        }

        // deactivate the entrant profile after the cleanup is completed
        userDocumentToDeactivate.update("hasActiveEntrantProfile", false).await()
    } // end of delete profiles function

    /**
     * Gets every entrant profile whose profile picture is an image, used when an admin deletes that image
     *
     * @param imageId ID of the image
     *
     * @return List of entrant profiles whose imageId matches
     *
     * @throws FirebaseFirestoreException if the fetch fails
     */
    suspend fun getEntrantProfilesByImageId(imageId: String): List<EntrantProfileModel> {
        val snapshot = db.collection("entrants").whereEqualTo("imageId", imageId).get().await()
        return snapshot.toObjects(EntrantProfileModel::class.java)
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
}
