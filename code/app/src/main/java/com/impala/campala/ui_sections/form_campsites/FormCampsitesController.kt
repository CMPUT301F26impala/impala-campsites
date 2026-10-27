package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.Controller

class FormCampsitesController(
    val model: FormCampsitesModel
) : Controller {

    fun onTitleChange(newTitle: String) {
        model.setTitle(newTitle)
    }

    fun onLocationChange(newLocation: String) {
        model.setLocation(newLocation)
    }

    fun onDescriptionChange(newDescription: String) {
        model.setDescription(newDescription)
    }

    fun onMaximumOccupancyChange(newOccupancy: String) {
        model.setMaximumOccupancy(newOccupancy)
    }

    fun onAddTag(newTag: String) {
        model.addTag(newTag)
    }

    fun onRemoveTag(tag: String) {
        model.removeTag(tag)
    }

    fun onSaveClick() {
        model.saveCampsite()
    }

    // for setting flag to false after user pressed save, otherwise the flag stays true
    fun onSaveNavigationBecomesFalse() {
        model.changeSaveFLagFalse()
    }
}