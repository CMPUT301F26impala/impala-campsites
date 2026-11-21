package com.impala.campala.ui_sections.view_profile

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the entrant's 'Profile' tab
 *
 * @param entrantDB the EntrantProfileRepository to fetch the user's entrant profile from
 * @param user the user
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property profile The user's entrant profile or null if it's not loaded
 */
class ViewProfileModel(val entrantDB: EntrantProfileRepository, val user: User, val scope: CoroutineScope) : ObservableModel<ViewProfileModel>() {
    var profile: EntrantProfileModel? = null

    /**
     * Called on opening the page, gets the profile and updates observers
     */
    fun loadProfile() {
        scope.launch {
            profile = entrantDB.getEntrantProfileById(user.getUID())
            notifyObservers(this@ViewProfileModel)
        }
    }
}
