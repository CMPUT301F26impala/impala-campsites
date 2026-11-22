package com.impala.campala.ui_sections.edit_profile

import android.net.Uri
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
     * Called by the image picker dialog after an image is chosen, stores the image Uri until upload (doesn't upload immediately)
     *
     * @param imageUri content Uri of the image the user picked
     */
    fun onUploadImage(imageUri: Uri) {
        model.updateUnsavedImage(imageUri)
    }

    /**
     * Called when the user presses save, tells the model to save the profile
     * The view goes back once the model says the save has finished
     */
    fun onSaveClick() {
        model.saveProfile()
    }

    /**
     * Called after navigating back after a save to reset the state of the model for next time
     */
    fun onSaveNavigationBecomesFalse() {
        model.changeSaveFlagFalse()
    }
}
