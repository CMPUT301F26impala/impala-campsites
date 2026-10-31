package com.impala.campala.ui_sections.campsite_details

import androidx.navigation.NavController
import com.impala.campala.Controller

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

    fun moveToBrowse(navController: NavController) {
        navController.popBackStack()
    }

    fun moveToBookings() {
        // TODO: Add functionality to move back to the booked dates screen
    }

    fun generateQRCode() {
        // TODO: Add functionality to generate and display a QR Code.
    }

    fun joinLottery() {
        model.joinLottery()
    }
}