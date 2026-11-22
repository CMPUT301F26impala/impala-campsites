package com.impala.campala.ui_sections.view_profile

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the entrant's 'Profile' tab
 *
 * @param entrantDB the EntrantProfileRepository to fetch the user's entrant profile from
 * @param imageRepo the repo to fetch the profile picture from
 * @param user the user
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property profile The user's entrant profile or null if it's not loaded
 * @property imageBytes The profile's compressed picture, null if it has no picture
 * @property showingDeleteConfirmation Whether the 'Confirm Deletion' dialog is open
 */
class ViewProfileModel(val entrantDB: EntrantProfileRepository, val imageRepo: ImageRepository, val user: User, val scope: CoroutineScope) : ObservableModel<ViewProfileModel>() {
    var profile: EntrantProfileModel? = null
    var imageBytes: ByteArray? = null
    var showingDeleteConfirmation = false

    /**
     * Called on opening the page, gets the profile and updates observers
     */
    fun loadProfile() {
        scope.launch {
            profile = entrantDB.getEntrantProfileById(user.getUID())
            val imageId = profile?.imageId ?: ""
            imageBytes = if (imageId.isNotEmpty()) imageRepo.getImageById(imageId)?.data?.toBytes() else null
            notifyObservers(this@ViewProfileModel)
        }
    }

    /**
     * Opens the 'Confirm Deletion' dialog
     */
    fun openDeleteConfirmation() {
        showingDeleteConfirmation = true
        notifyObservers(this)
    }

    /**
     * Closes the 'Confirm Deletion' dialog
     */
    fun closeDeleteConfirmation() {
        showingDeleteConfirmation = false
        notifyObservers(this)
    }
}
