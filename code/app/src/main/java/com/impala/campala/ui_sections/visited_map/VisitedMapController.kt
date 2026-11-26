package com.impala.campala.ui_sections.visited_map

import androidx.navigation.NavController
import com.impala.campala.Controller

/**
 * Controller for the 'Places you've been' screen
 *
 * @param model the VisitedMapModel
 */
class VisitedMapController(val model: VisitedMapModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the data
     */
    fun onFirstRender() {
        model.loadVisitedCampsites()
    }

    /**
     * Goes back to the previous screen
     *
     * @param navController the root NavController
     */
    fun onBackPress(navController: NavController) {
        navController.popBackStack()
    }
}
