package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.Controller

class FormCampsitesController(
    val model: FormCampsitesModel
) : Controller {

    /**
     * Called in a LaunchedEffect in the View whenever the form is opened to set up the state of the screen
     *
     * @param campsiteId the id of the campsite to edit, or null if it's 'add campsite' mode
     */
    suspend fun onOpen(campsiteId: String?) {
        if (campsiteId == null) {
            model.startNewCampsite()
        } else {
            model.loadCampsite(campsiteId)
        }
    }

    fun onTitleChange(newTitle: String) {
        model.updateTitle(newTitle)
    }

    fun onLocationChange(newLocation: String) {
        model.updateLocation(newLocation)
    }

    fun onDescriptionChange(newDescription: String) {
        model.updateDescription(newDescription)
    }

    fun onMaximumOccupancyChange(newOccupancy: String) {
        model.updateMaximumOccupancy(newOccupancy)
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