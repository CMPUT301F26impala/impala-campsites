package com.impala.campala.ui_sections.campsite_details

import androidx.navigation.NavController
import com.impala.campala.Controller

/**
 * Controller for the Campsite Details screen
 *
 * Largely just acts as a middleman to pass data and click actions from the View to the model
 *
 * @param model the CampsiteDetailsModel this controller calls functions from
 */
class CampsiteDetailsController(val model: CampsiteDetailsModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the campsite whenever the screen is shown
     *
     * @param campsiteId the id of the campsite to show
     */
    suspend fun onOpen(campsiteId: String) {
        model.loadCampsite(campsiteId)
    }

    fun moveToEdit() {
        // TODO: Add functionality to move to the edit campsite screen
    }

    /**
     * Called when the back button is pressed, this will take the user back to the Browse Campsite screen
     *
     * @param navController the navController object used for navigation
     */
    fun moveToBrowse(navController: NavController) {
        navController.popBackStack()
    }

    fun moveToBookings() {
        // TODO: Add functionality to move back to the booked dates screen
    }

    fun generateQRCode() {
        // TODO: Add functionality to generate and display a QR Code.
    }

    /**
     * Called when the join lottery or join waitlist button is pressed
     */
    fun joinLottery() {
        model.joinLottery()
    }

    /**
     * Called when the leave waitlist button is pressed
     */
    fun leaveWaitlist() {
        model.leaveWaitlist()
    }
}