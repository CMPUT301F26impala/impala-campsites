package com.impala.campala.ui_sections.admin_images

import com.impala.campala.Controller

/**
 * Controller for the admin's Images tab
 *
 * Largely just acts as a middleman to pass data and click actions from the View to the model
 *
 * @param model the AdminImagesModel this controller calls functions from
 */
class AdminImagesController(val model: AdminImagesModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the images whenever the tab is shown
     */
    fun onFirstRender() {
        model.loadImages()
    }

    /**
     * Called when an admin confirms deleting an image
     *
     * @param imageId ID of the image to delete
     */
    fun deleteImage(imageId: String) {
        model.deleteImage(imageId)
    }
}
