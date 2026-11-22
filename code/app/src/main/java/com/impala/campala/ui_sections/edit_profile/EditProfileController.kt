package com.impala.campala.ui_sections.edit_profile

import androidx.navigation.NavController
import com.impala.campala.Controller

/**
 * Controller for the edit profile form screen
 *
 * @param model the EditProfileModel whose functions are called
 */
class EditProfileController(val model: EditProfileModel) : Controller {
    /**
     * Used in a LaunchedEffect in the View, fills in the form when it's opened
     */
    suspend fun onOpen() {
        model.loadProfile()
    }

    /**
     * Changes the name on the profile
     *
     * @param newName the new name
     */
    fun onNameChange(newName: String) {
        model.updateName(newName)
    }

    /**
     * Changes the email on the profile
     *
     * @param newEmail the new email
     */
    fun onEmailChange(newEmail: String) {
        model.updateEmail(newEmail)
    }

    /**
     * Changes the phone number on the profile
     *
     * @param newPhoneNumber the new phone number
     */
    fun onPhoneNumberChange(newPhoneNumber: String) {
        model.updatePhoneNumber(newPhoneNumber)
    }

    /**
     * Called when the user presses save, tells the model to save the profile and goes back
     *
     * @param navController the root NavController, used to go back to the profile tab
     */
    fun onSaveClick(navController: NavController) {
        model.saveProfile()
        navController.popBackStack()
    }
}
