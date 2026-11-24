package com.impala.campala.ui_sections.admin_profiles

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.model.AdminProfileModel
import com.impala.campala.model.EntrantProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.impala.campala.model.ImageRepository

/**
 * Model for the profile branch of admin UI
 *
 * Has a list of people with active entrant or campsite owner profiles
 *
 * @param scope the CoroutineScope used to get the active entrants and campsite owners
 * @property allActiveUserProfiles list containing all active entrants and campsite owners
 * @property isFetchingUserProfiles to check if the fetch for profiles is in process
 * @property wasProfileFetchUnsuccessful to check if the fetch for profiles failed
 */
class AdminProfilesModel(val scope: CoroutineScope) : ObservableModel<AdminProfilesModel>() {
    var allActiveUserProfiles: List<AdminProfileModel> = emptyList()
    var isFetchingUserProfiles = false
    var wasProfileFetchUnsuccessful = false
    var profileSearchInput = ""
    var filteredUserProfiles: List<AdminProfileModel> = emptyList()
    var profilePictureBytesById: Map<String, ByteArray> = emptyMap()

    /**
     * gets all the active profiles of entrants and campsite owners and runs everytime someone opens the profile tab from admin view
     */
    fun loadAllActiveProfiles() {
        if (isFetchingUserProfiles) { return }
        isFetchingUserProfiles = true
        wasProfileFetchUnsuccessful = false
        notifyObservers(this)

        scope.launch {
            try {
                allActiveUserProfiles = EntrantProfileRepository.getAdminProfiles()
                filterDisplayedProfiles()
                loadUserProfilePictures()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                wasProfileFetchUnsuccessful = true
                Log.e("AdminProfilesModel", "Failed to fetch user profiles", e)
            } finally {
                isFetchingUserProfiles = false
                notifyObservers(this@AdminProfilesModel)
            }
        }
    } // end of load all active

    /**
     * updates the search text for filtering
     *
     * @param newSearchInput search text
     */
    fun updateProfileSearchInput(newSearchInput: String) {
        profileSearchInput = newSearchInput
        filterDisplayedProfiles()
        notifyObservers(this)
    }

    /**
     * filters for name, email or phone number entered by user
     */
    fun filterDisplayedProfiles() {
        val matchingUserProfiles = mutableListOf<AdminProfileModel>()
        val searchInputWithoutSpaces = profileSearchInput.trim()
        for (currentUserProfile in allActiveUserProfiles) {
            if (
                currentUserProfile.name.contains(searchInputWithoutSpaces, ignoreCase = true) ||
                currentUserProfile.email.contains(searchInputWithoutSpaces, ignoreCase = true) ||
                currentUserProfile.phoneNumber.contains(searchInputWithoutSpaces, ignoreCase = true)
            ) { matchingUserProfiles.add(currentUserProfile) }
        }
        filteredUserProfiles = matchingUserProfiles
    }

    /**
     * gets profile pictures accessed by image ID
     */
    suspend fun loadUserProfilePictures() {
        val fetchedProfilePictureBytes = mutableMapOf<String, ByteArray>()
        for (currentUserProfile in allActiveUserProfiles) {
            val profilePictureImageId = currentUserProfile.imageId
            if (profilePictureImageId.isBlank()) { continue }
            if (fetchedProfilePictureBytes.containsKey(profilePictureImageId)) { continue }

            val fetchedProfileImage = ImageRepository.getImageById(profilePictureImageId)
            if (fetchedProfileImage != null) {
                val fetchedImageData = fetchedProfileImage.data
                if (fetchedImageData != null) { fetchedProfilePictureBytes[profilePictureImageId] = fetchedImageData.toBytes() }
            }
        }
        profilePictureBytesById = fetchedProfilePictureBytes
    }
}