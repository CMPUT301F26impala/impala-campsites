package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FormCampsitesModel(
    val repo: CampsiteRepository,
    val scope: CoroutineScope,
    val campsite: CampsiteModel? = null
): ObservableModel<FormCampsitesModel>() {

    var title = if (campsite == null) "" else campsite.name
    var location = if (campsite == null) "" else campsite.location
    var description = if (campsite == null) "" else campsite.description
    var maximumOccupancy = if (campsite == null) "" else campsite.maxOccupancy.toString()
    var tags = if (campsite == null) mutableListOf<String>() else campsite.tags.toMutableList()

    // if campiste was saved successfully
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

    fun saveCampsite() {
        // convert maximum occupancy to integer (its a string right now in the input box)
        val occupancy = if (maximumOccupancy == "") 0 else maximumOccupancy.toInt()

        // editing an existing campsite, updatedCampsite stores the datatype we will send after create/edit
        val updatedCampsite: CampsiteModel
        if (campsite != null) {
            updatedCampsite = campsite.copy(
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
