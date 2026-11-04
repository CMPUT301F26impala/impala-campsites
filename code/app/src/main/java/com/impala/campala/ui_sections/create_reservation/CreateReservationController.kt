package com.impala.campala.ui_sections.create_reservation

import androidx.navigation.NavController
import com.impala.campala.model.CampsiteModel
import java.time.LocalDate

class CreateReservationController(val model: CreateReservationModel) {
    // Will preload data
    fun onOpen(lotteryId: String) {}

    // Advances the screen from the terms step to the next step
    fun onAcceptTerms() {}

    // Only called in multi-campsite lotteries, picks a specific campsite to let you see the available dates for that one
    fun onCampsiteSelect(campsiteId: String) {}

    // Sets the start date to user's choice, doesn't validate if it's an available date just yet
    fun onStartDateChange(date: LocalDate) {}

    // Sets the end date to user's choice, doesn't validate if it's an available date just yet
    fun onEndDateChange(date: LocalDate) {}

    // Triggered up to two times
    // Once on the press of the confirm FAB in the bottom right, and a second time (with doublyConfirmed set to true) inside of the "Are you sure" modal
    fun onConfirmPress(doublyConfirmed: Boolean?) {}

    // Triggered up to two times
    // Once on the press of the decline FAB in the bottom right, and a second time (with doublyConfirmed set to true) inside of the "Are you sure" modal
    fun onDeclinePress(doublyConfirmed: Boolean?) {}

    // Triggered in the final confirmation screen as a way to go home from there
    fun goHome(navController: NavController) {}
}
