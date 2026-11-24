package com.impala.campala.ui_sections.view_profile

import androidx.navigation.NavController
import com.impala.campala.Controller

/**
 * Controller for the entrant's 'Profile' tab
 *
 * @param model the ViewProfileModel this controller updates
 */
class ViewProfileController(val model: ViewProfileModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the profile whenever the tab is shown
     */
    fun onFirstRender() {
        model.loadProfile()
    }

    /**
     * Takes the user to the edit profile form
     *
     * @param navController the root NavController, assumes 'editProfile' is on this nav stack
     */
    fun onEditProfilePress(navController: NavController) {
        navController.navigate("editProfile")
    }

    /**
     * Takes the user to the app policies screen
     *
     * @param navController the root NavController, assumes 'appPolicies' is on this nav stack
     */
    fun onViewAppPoliciesPress(navController: NavController) {
        navController.navigate("appPolicies")
    }

    /**
     * Opens the delete confirm modal
     */
    fun onDeleteProfilePress() {
        model.openDeleteConfirmation()
    }

    /**
     * Closes the delete confirm modal without doing anything
     */
    fun onDeleteCancel() {
        model.closeDeleteConfirmation()
    }

    /**
     * Deletes the user's profile
     */
    fun onDeleteConfirm() {
        model.deleteSelectedUserProfile()
    }
}
