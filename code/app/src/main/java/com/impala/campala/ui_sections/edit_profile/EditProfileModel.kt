package com.impala.campala.ui_sections.edit_profile

import android.net.Uri
import com.google.firebase.firestore.Blob
import com.impala.campala.ImageCompressor
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ImageModel
import com.impala.campala.model.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the edit profile form
 *
 * Stores the interim values for the Text Fields, and also eventually handles saving if save is pressed
 *
 * @param entrantDB the EntrantProfileRepository to load and save the user's entrant profile with
 * @param imageRepo Repository of images. Used for loading the profile's current picture, and saving a new one when it's picked
 * @param imageCompressor Used to shrink the picked image before it's saved
 * @param user the user editing their profile
 * @param scope the CoroutineScope used for Firebase operations
 *
 * @property profile The entrant profile being edited, null until it has loaded
 * @property name The name to go on the profile
 * @property email The email to go on the profile
 * @property phoneNumber The phone number to go on the profile
 * @property unsavedImage The image picked by the form, not uploaded yet
 * @property existingImageBytes The profile's current saved picture, null if it has no picture
 * @property wasProfileSavedSuccessfully True once the profile has finished saving to the database
 */
class EditProfileModel(
    val entrantDB: EntrantProfileRepository,
    val imageRepo: ImageRepository,
    val imageCompressor: ImageCompressor,
    val user: User,
    val scope: CoroutineScope
) : ObservableModel<EditProfileModel>() {
    var profile: EntrantProfileModel? = null
    var name = ""
    var email = ""
    var phoneNumber = ""
    var unsavedImage: Uri? = null
    var existingImageBytes: ByteArray? = null
    var wasProfileSavedSuccessfully = false

    /**
     * Fills in the form's fields with the user's current entrant profile
     */
    suspend fun loadProfile() {
        profile =entrantDB.getEntrantProfileById(user.getUID())
        name = profile?.name ?: ""
        email = profile?.email ?: ""
        phoneNumber = profile?.phoneNumber ?: ""
        val imageId = profile?.imageId ?: ""
        existingImageBytes = if (imageId.isNotEmpty()) imageRepo.getImageById(imageId)?.data?.toBytes() else null
        unsavedImage = null
        notifyObservers(this)
    }

    /**
     * Holds the image picked in the form until the profile is saved
     *
     * @param imageUri content Uri of the image the user picked
     */
    fun updateUnsavedImage(imageUri: Uri) {
        unsavedImage = imageUri
        notifyObservers(this)
    }

    /**
     * Resets the saved flag once the view has navigated away, otherwise it stays true the next time the form opens
     */
    fun changeSaveFlagFalse() {
        wasProfileSavedSuccessfully = false
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
     * Saves the profile with the form's values, then notifies the view
     * Also uploads a new image if needed
     */
    fun saveProfile() {
        // If the profile somehow couldn't be loaded, a new one is made under the user's ID so the edit isn't lost
        val updatedProfile = (profile ?: EntrantProfileModel(userId = user.getUID())).copy(
            name = name.trim(),
            email = email.trim(),
            phoneNumber = phoneNumber.trim()
        )

        scope.launch {
            val imageUri = unsavedImage
            val oldImageId = profile?.imageId ?: ""
            if (imageUri != null) {
                val image = ImageModel(data = Blob.fromBytes(imageCompressor.compress(imageUri)))
                imageRepo.saveImage(image)
                updatedProfile.imageId = image.id
            }

            entrantDB.saveEntrantProfile(updatedProfile)
            profile = updatedProfile

            // Delete the old image
            if (imageUri != null && oldImageId.isNotEmpty()) {
                imageRepo.deleteImage(oldImageId)
            }

            wasProfileSavedSuccessfully = true
            notifyObservers(this@EditProfileModel)
        }
    }
}
