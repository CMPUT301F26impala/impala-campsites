package com.impala.campala.ui_sections.view_profile

import com.impala.campala.Controller

/**
 * Controller for the entrant's 'Profile' tab
 *
 * @param model the ViewProfileModel this controller updates
 */
class ViewProfileController(val model: ViewProfileModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the profile whenever the tab is shown
     */
    fun onFirstRender() {
        model.loadProfile()
    }
}
