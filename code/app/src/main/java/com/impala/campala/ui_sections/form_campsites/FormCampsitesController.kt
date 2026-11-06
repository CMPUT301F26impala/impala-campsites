package com.impala.campala.ui_sections.form_campsites

import com.impala.campala.Controller


/**
 * Controller for the create and edit version campsite forms
 *
 * Receives actions from the View and calls the corresponding functions in the Model.
 * Also decides whether the form should create a new campsite or load an existing one.
 *
 * @param model the FormCampsitesModel whose functions are called
 */
class FormCampsitesController(
    val model: FormCampsitesModel
) : Controller {

    /**
     * Used in a LaunchedEffect in the View, handles data setup of the screen when the form is opened
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

    /**
     * Changes title of the campsite
     *
     * @param newTitle New title of the campsite
     */
    fun onTitleChange(newTitle: String) {
        model.updateTitle(newTitle)
    }

    /**
     * Changes location of the campsite
     *
     * @param newLocation New location of the campsite
     */
    fun onLocationChange(newLocation: String) {
        model.updateLocation(newLocation)
    }

    /**
     * Changes description of the campsite
     *
     * @param newDescription New description to be added
     */
    fun onDescriptionChange(newDescription: String) {
        model.updateDescription(newDescription)
    }

    /**
     * Controls change of maximum occupacy
     *
     * @param newOccupancy New maximum occupancy value
     */
    fun onMaximumOccupancyChange(newOccupancy: String) {
        model.updateMaximumOccupancy(newOccupancy)
    }

    /**
     * Adds a tag to the list of tags
     *
     * @param newTag Tag to be added
     */
    fun onAddTag(newTag: String) {
        model.addTag(newTag)
    }

    /**
     * Removes tag form the list of tags
     *
     * @param tag String of the tag to be removed
     */
    fun onRemoveTag(tag: String) {
        model.removeTag(tag)
    }

    /**
     * Called when the user presses create or save and tells the model to save the campsite.
     */
    fun onSaveClick() {
        model.saveCampsite()
    }

    /**
     * Called after the campsite has been saved and then tells the model to reset the flag for navigation
     */
    fun onSaveNavigationBecomesFalse() {
        // for setting flag to false after user pressed save, otherwise the flag stays true
        model.changeSaveFLagFalse()
    }
}