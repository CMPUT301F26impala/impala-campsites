package com.impala.campala.ui_sections.admin_profiles

import com.impala.campala.Controller

/**
 * Controller for the profile branch of admin UI
 * Receives notifications from the view and runs the functions in the model.
 *
 * @param model the AdminProfilesModel it runs the functions from
 */
class AdminProfilesController(
    val model: AdminProfilesModel
) : Controller {
    /**
     * Loads all active user profiles when the profiles tab is opened first time in launched effect.
     */
    fun onFirstRender() {
        model.loadAllActiveProfiles()
    }
}