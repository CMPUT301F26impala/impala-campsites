package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the create and edit version of Campsite form.
 *
 * Stores information entered into the form, and notifies view about the changes.
 * Saves campsites after creation and editing, as well as loading them to display values to the user.
 *
 * @param repo CampsiteRepository consisting of all campsites
 * @param campsiteOwnerProfileRepo Repository of campsite owner profiles. Used for saving new campsites to a user's profile
 * @param user the authorized user editing the form
 * @param scope the CoroutineScope used for Firebase operations
 * @param campsite campsite whose data will be edited, passed as null in creation mode
 *
 * @property title The name of the campsite
 * @property location The location to go on the campsite
 * @property description The description to go on the campsite
 * @property maximumOccupancy The maximum occupancy to go on the campsite
 * @property tags The list of tags to go on the campsite
 * @property wasCampsiteSavedSuccessfully True if the new campsite was saved to the database successfully
 */
class FormCampsitesModel(
    val repo: CampsiteRepository,
    val campsiteOwnerProfileRepo: CampsiteOwnerProfileRepository,
    val user: User,
    val scope: CoroutineScope,
    var campsite: CampsiteModel? = null
): ObservableModel<FormCampsitesModel>() {

    var title = campsite?.name ?: "" // The ?: is equivalent to "campsite.name if exists else empty string"
    var location = campsite?.location ?: ""
    var description = campsite?.description ?: ""
    var maximumOccupancy = campsite?.maxOccupancy?.toString() ?: ""
    var tags = campsite?.tags?.toMutableList() ?: mutableListOf()

    // if campsite was saved successfully
    var wasCampsiteSavedSuccessfully = false

    /**
     * Updates title of the campsite
     *
     * @param newTitle string to be used as the title of the campsite
     */
    fun updateTitle(newTitle: String) {
        title = newTitle
        notifyObservers(this)
    }

    /**
     * Updates location of the campsite
     *
     * @param newLocation Location of the campsite
     */
    fun updateLocation(newLocation: String) {
        location = newLocation
        notifyObservers(this)
    }

    /**
     * Updates the description of a campsite
     *
     * @param newDescription Description to be added for the campsite
     */
    fun updateDescription(newDescription: String) {
        description = newDescription
        notifyObservers(this)
    }

    /**
     * Updates maximum occupancy of the tags
     *
     * @param newOccupancy string data for the maximum occupancy of the campsite
     */
    fun updateMaximumOccupancy(newOccupancy: String) {
        maximumOccupancy = newOccupancy
        notifyObservers(this)
    }

    /**
     * Adds a tag for the campsite to its list of tags
     *
     * @param newTag value of the new tag
     */
    fun addTag(newTag: String) {
        val trimmedTag = newTag.trim()

        if (trimmedTag.isNotEmpty()) {
            tags.add(trimmedTag)
            notifyObservers(this)
        }
    }

    /**
     * Removes a tag for the campsite from its list of tags
     *
     * @param tag the tag to remove from the list
     */
    fun removeTag(tag: String) {
        tags.remove(tag)
        notifyObservers(this)
    }

    /**
     * Prepares the model for creating a new campsite by clearing the form's fields
     */
    fun startNewCampsite() {
        setFields(null)
    }

    /**
     * Prepares the model for editing an existing campsite by updating the form's fields
     *
     * @param campsiteId ID of the campsite being edited
     */
    suspend fun loadCampsite(campsiteId: String) {
        setFields(repo.getCampsiteById(campsiteId))
    }

    /**
     * Updated the fields in the form with the data that was passed, and set to null for clearing them
     * Notifies the views that the fields have been updated.
     *
     * @param newCampsite The campsite data that should be added to the form, clear data when it's set to null
     */
    private fun setFields(newCampsite: CampsiteModel?) {
        campsite = newCampsite
        title = newCampsite?.name ?: ""
        location = newCampsite?.location ?: ""
        description = newCampsite?.description ?: ""
        maximumOccupancy = newCampsite?.maxOccupancy?.toString() ?: ""
        tags = newCampsite?.tags?.toMutableList() ?: mutableListOf()
        notifyObservers(this)
    }

    /**
     * Create or update a campsite using the form's values.
     * Adds the campsite Id to the user's campsite owner profile, then notifies the view
     */
    fun saveCampsite() {
        // convert maximum occupancy to integer (its a string right now in the input box)
        // default value needed
        // check for number pad on android
        val occupancy = if (maximumOccupancy == "") 0 else maximumOccupancy.toInt()

        // editing an existing campsite, updatedCampsite stores the datatype we will send after create/edit
        // campsite is a var now, so it's copied into a val first so kotlin knows it can't change underneath us
        val existingCampsite = campsite
        val updatedCampsite: CampsiteModel
        if (existingCampsite != null) {
            updatedCampsite = existingCampsite.copy(
                name = title,
                location = location.trim(),
                description = description,
                maxOccupancy = occupancy,
                tags = tags.toMutableList()
            )
        } else {
            // creating a new campsite
            updatedCampsite = CampsiteModel(
                name = title,
                location = location.trim(),
                description = description,
                maxOccupancy = occupancy,
                tags = tags.toMutableList()
            )
        }

        //save to firebase
        scope.launch {
            repo.saveCampsite(updatedCampsite)
            // If this is a new campsite, make sure to actually add it to the campsite owner profile too
            if (existingCampsite == null) {
                campsiteOwnerProfileRepo.getCampsiteOwnerProfileById(user.getUID())?.let {
                    campsiteOwnerProfileRepo.addOwnedCampsite(it, updatedCampsite.id)
                }
            }
            wasCampsiteSavedSuccessfully = true
            notifyObservers(this@FormCampsitesModel)
        }
    }

    /**
     * When a Save operation after creating or editing a form is triggered, wasCampsiteSavedSuccessfully is changed to false which is used for navigation.
     * This function changes that flag back to false after it's been used once for reuse.
     */
    fun changeSaveFLagFalse() {
        // set the "saved succesful flag" to false
        // after user is popped back in the stack (the popbackstack thing in nav)
        // to prevent the flag from staying true across multiple form usage
        wasCampsiteSavedSuccessfully = false
        notifyObservers(this)
    }
}
