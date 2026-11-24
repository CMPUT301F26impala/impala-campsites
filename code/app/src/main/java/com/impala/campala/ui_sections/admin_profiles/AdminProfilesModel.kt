package com.impala.campala.ui_sections.admin_profiles

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.model.AdminProfileModel
import com.impala.campala.model.EntrantProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    }
}