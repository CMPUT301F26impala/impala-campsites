package com.impala.campala.ui_sections.view_profile

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.impala.campala.model.AdminProfileModel
import android.util.Log
import kotlinx.coroutines.CancellationException

/**
 * Model for the entrant's 'Profile' tab
 *
 * @param scope the CoroutineScope that the suspend fetches run in
 * @param selectedAdminProfile profile clicked on by the admin, when looking at their own profile its null
 *
 * @property profile The user's entrant profile or null if it's not loaded
 * @property imageBytes The profile's compressed picture, null if it has no picture
 * @property showingDeleteConfirmation Whether the 'Confirm Deletion' dialog is open
 */
class ViewProfileModel(val scope: CoroutineScope, val selectedAdminProfile: AdminProfileModel? = null) : ObservableModel<ViewProfileModel>() {
    var profile: EntrantProfileModel? = null
    var imageBytes: ByteArray? = null
    var showingDeleteConfirmation = false
    var isCurrentlyDeletingProfile = false
    var wasProfileDeletedSuccessfully = false
    var wasProfileDeletionUnsuccessful = false

    /**
     * Called on opening the page, gets the profile and updates observers. If the admin selected a profile show that.
     */
    fun loadProfile() {
        scope.launch {
            if (selectedAdminProfile != null) {
                // load the selected user's profile picture
                val selectedProfilePictureId = selectedAdminProfile.imageId
                imageBytes = null
                if (selectedProfilePictureId.isNotEmpty()) {
                    val selectedProfileImage = ImageRepository.getImageById(selectedProfilePictureId)
                    if (selectedProfileImage != null) {
                        val selectedProfileImageData = selectedProfileImage.data
                        if (selectedProfileImageData != null) { imageBytes = selectedProfileImageData.toBytes() }
                    }
                }
            } else {
                profile = EntrantProfileRepository.getEntrantProfileById(User.getUID())
                val imageId = profile?.imageId ?: ""
                imageBytes = if (imageId.isNotEmpty()) ImageRepository.getImageById(imageId)?.data?.toBytes() else null
            }
            notifyObservers(this@ViewProfileModel)
        }
    }

    /**
     * Opens the 'Confirm Deletion' dialog
     */
    fun openDeleteConfirmation() {
        if (isCurrentlyDeletingProfile) { return }
        wasProfileDeletionUnsuccessful = false
        showingDeleteConfirmation = true
        notifyObservers(this)
    }

    /**
     * Closes the 'Confirm Deletion' dialog
     */
    fun closeDeleteConfirmation() {
        if (isCurrentlyDeletingProfile) { return }
        wasProfileDeletionUnsuccessful = false
        showingDeleteConfirmation = false
        notifyObservers(this)
    }

    /**
     * Deletes the selected profile and then updates the view with the change
     */
    fun deleteSelectedUserProfile() {
        if (isCurrentlyDeletingProfile) { return }
        var userProfileIdToDelete = User.getUID()
        if (selectedAdminProfile != null) { userProfileIdToDelete = selectedAdminProfile.userId }

        isCurrentlyDeletingProfile = true
        wasProfileDeletedSuccessfully = false
        wasProfileDeletionUnsuccessful = false
        notifyObservers(this)

        scope.launch {
            try {
                EntrantProfileRepository.deleteProfile(userProfileIdToDelete)
                if (userProfileIdToDelete == User.getUID()) { User.setHasActiveEntrantProfile(false) }
                wasProfileDeletedSuccessfully = true
                showingDeleteConfirmation = false
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                wasProfileDeletionUnsuccessful = true
                Log.e("ViewProfileModel", "Failed to delete user profile", e)
            } finally {
                isCurrentlyDeletingProfile = false
                notifyObservers(this@ViewProfileModel)
            }
        }
    } // end of profile deletion function
}
