package com.impala.campala.ui_sections.edit_profile

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the edit profile form
 *
 * Stores the interim values for the Text Fields, and also eventually handles saving if save is pressed
 *
 * @param entrantDB the EntrantProfileRepository to load and save the user's entrant profile with
 * @param user the user editing their profile
 * @param scope the CoroutineScope used for Firebase operations
 *
 * @property profile The entrant profile being edited, null until it has loaded
 * @property name The name to go on the profile
 * @property email The email to go on the profile
 * @property phoneNumber The phone number to go on the profile
 */
class EditProfileModel(
    val entrantDB: EntrantProfileRepository,
    val user: User,
    val scope: CoroutineScope
) : ObservableModel<EditProfileModel>() {
    var profile: EntrantProfileModel? = null
    var name = ""
    var email = ""
    var phoneNumber = ""

    /**
     * Fills in the form's fields with the user's current entrant profile
     */
    suspend fun loadProfile() {
        profile =entrantDB.getEntrantProfileById(user.getUID())
        name = profile?.name ?: ""
        email = profile?.email ?: ""
        phoneNumber = profile?.phoneNumber ?: ""
        notifyObservers(this)
    }

    /**
     * Updates the name on the profile
     *
     * @param newName the new name
     */
    fun updateName(newName: String) {
        name = newName
        notifyObservers(this)
    }

    /**
     * Updates the email on the profile
     *
     * @param newEmail the new email
     */
    fun updateEmail(newEmail: String) {
        email = newEmail
        notifyObservers(this)
    }

    /**
     * Updates the phone number on the profile
     *
     * @param newPhoneNumber the new phone number
     */
    fun updatePhoneNumber(newPhoneNumber: String) {
        phoneNumber = newPhoneNumber
        notifyObservers(this)
    }

    /**
     * Saves the profile with the form's values
     */
    fun saveProfile() {
        // If the profile somehow couldn't be loaded, a new one is made under the user's ID so the edit isn't lost
        val updatedProfile = (profile ?: EntrantProfileModel(userId = user.getUID())).copy(
            name = name.trim(),
            email = email.trim(),
            phoneNumber = phoneNumber.trim()
        )

        scope.launch {
            entrantDB.saveEntrantProfile(updatedProfile)
            profile = updatedProfile
        }
    }
}
