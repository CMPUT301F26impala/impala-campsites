package com.impala.campala.ui_sections.admin_profiles

import com.impala.campala.Controller
import android.net.Uri
import androidx.navigation.NavController

/**
 * Controller for the profile branch of admin UI
 * Receives notifications from the view and runs the functions in the model.
 *
 * @param model the AdminProfilesModel it runs the functions from
 */
class AdminProfilesController(
    val model: AdminProfilesModel
) : Controller {
    /**
     * Loads all active user profiles when the profiles tab is opened first time in launched effect.
     */
    fun onFirstRender() {
        model.loadAllActiveProfiles()
    }

    /**
     * Used when text in search bar is changed for filtering
     *
     * @param newSearchInput the search bar text
     */
    fun onSearchBarInputChange(newSearchInput: String) { model.updateProfileSearchInput(newSearchInput) }

    /**
     * Makes user profile clickable
     *
     * @param navController the navigation controller
     * @param selectedUserProfileId ID of the clicked on user
     */
    fun onUserProfilePressed(
        navController: NavController,
        selectedUserProfileId: String
    ) {
        navController.navigate("profile/${Uri.encode(selectedUserProfileId)}")
    }
}