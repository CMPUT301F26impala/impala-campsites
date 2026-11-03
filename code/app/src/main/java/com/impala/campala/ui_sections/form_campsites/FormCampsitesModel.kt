package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// TODO: Needs KDOC!!!!
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

    fun updateTitle(newTitle: String) {
        title = newTitle
        notifyObservers(this)
    }
    fun updateLocation(newLocation: String) {
        location = newLocation
        notifyObservers(this)
    }

    fun updateDescription(newDescription: String) {
        description = newDescription
        notifyObservers(this)
    }

    fun updateMaximumOccupancy(newOccupancy: String) {
        maximumOccupancy = newOccupancy
        notifyObservers(this)
    }

    fun addTag(newTag: String) {
        val trimmedTag = newTag.trim()

        if (trimmedTag.isNotEmpty()) {
            tags.add(trimmedTag)
            notifyObservers(this)
        }
    }

    fun removeTag(tag: String) {
        tags.remove(tag)
        notifyObservers(this)
    }

    fun startNewCampsite() {
        setFields(null)
    }

    suspend fun loadCampsite(campsiteId: String) {
        setFields(repo.getCampsiteById(campsiteId))
    }

    private fun setFields(newCampsite: CampsiteModel?) {
        campsite = newCampsite
        title = newCampsite?.name ?: ""
        location = newCampsite?.location ?: ""
        description = newCampsite?.description ?: ""
        maximumOccupancy = newCampsite?.maxOccupancy?.toString() ?: ""
        tags = newCampsite?.tags?.toMutableList() ?: mutableListOf()
        notifyObservers(this)
    }

    fun saveCampsite() {
        // convert maximum occupancy to integer (its a string right now in the input box)
        val occupancy = if (maximumOccupancy == "") 0 else maximumOccupancy.toInt()

        // editing an existing campsite, updatedCampsite stores the datatype we will send after create/edit
        // campsite is a var now, so it's copied into a val first so kotlin knows it can't change underneath us
        val existingCampsite = campsite
        val updatedCampsite: CampsiteModel
        if (existingCampsite != null) {
            updatedCampsite = existingCampsite.copy(
                name = title,
                location = location,
                description = description,
                maxOccupancy = occupancy,
                tags = tags.toMutableList()
            )
        } else {
            // creating a new campsite
            updatedCampsite = CampsiteModel(
                name = title,
                location = location,
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

    // set the "saved succesful flag" to false
    // after user is popped back in the stack (the popbackstack thing in nav)
    // to prevent the flag from staying true across multiple form usage
    fun changeSaveFLagFalse() {
        wasCampsiteSavedSuccessfully = false
        notifyObservers(this)
    }
}
