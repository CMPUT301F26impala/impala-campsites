package com.impala.campala.ui_sections.create_reservation

import androidx.navigation.NavController
import com.impala.campala.model.CampsiteModel
import java.time.LocalDate

/**
 * Controller for the Create Reservation screen
 *
 * @param model the CreateReservationModel this controller updates
 */
class CreateReservationController(val model: CreateReservationModel) {
    /**
     * Will preload data
     *
     * @param lotteryId the id of the lottery being reserved through
     */
    fun onOpen(lotteryId: String) {}

    /**
     * Advances the screen from the terms step to the next step
     */
    fun onAcceptTerms() {}

    /**
     * Only called in multi-campsite lotteries, picks a specific campsite to let you see the available dates for that one
     *
     * @param campsiteId the id of the campsite that was picked
     */
    fun onCampsiteSelect(campsiteId: String) {}

    /**
     * Sets the start date to user's choice, doesn't validate if it's an available date just yet
     *
     * @param date the start date the user picked
     */
    fun onStartDateChange(date: LocalDate) {}

    /**
     * Sets the end date to user's choice, doesn't validate if it's an available date just yet
     *
     * @param date the end date the user picked
     */
    fun onEndDateChange(date: LocalDate) {}

    /**
     * Triggered up to two times
     * Once on the press of the confirm FAB in the bottom right, and a second time (with doublyConfirmed set to true) inside of the "Are you sure" modal
     *
     * @param doublyConfirmed true when pressed inside of the "Are you sure" modal
     */
    fun onConfirmPress(doublyConfirmed: Boolean?) {}

    /**
     * Triggered up to two times
     * Once on the press of the decline FAB in the bottom right, and a second time (with doublyConfirmed set to true) inside of the "Are you sure" modal
     *
     * @param doublyConfirmed true when pressed inside of the "Are you sure" modal
     */
    fun onDeclinePress(doublyConfirmed: Boolean?) {}

    /**
     * Triggered in the final confirmation screen as a way to go home from there
     *
     * @param navController the NavController used to go home
     */
    fun goHome(navController: NavController) {}
}
