package com.impala.campala.ui_sections.campsite_details

import androidx.navigation.NavController
import com.impala.campala.Controller

class CampsiteDetailsController(val model: CampsiteDetailsModel) : Controller {
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