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
    fun onOpen(lotteryId: String) {
        model.loadLotteryDetails(lotteryId)
    }

    /**
     * Advances the screen from the terms step to the next step
     */
    fun onAcceptTerms() {
        model.acceptTerms()
    }

    /**
     * Only called in multi-campsite lotteries, picks a specific campsite to let you see the available dates for that one
     *
     * @param campsiteId the id of the campsite that was picked
     */
    fun onCampsiteSelect(campsiteId: String) {
        model.setSelectedCampsite(campsiteId)
    }

    /**
     * Sets the start date to user's choice, doesn't validate if it's an available date just yet
     *
     * @param date the start date the user picked, null if the field is empty or isn't a full date yet
     */
    fun onStartDateChange(date: LocalDate?) {
        model.updateSelectedStartDate(date)
    }

    /**
     * Sets the end date to user's choice, doesn't validate if it's an available date just yet
     *
     * @param date the end date the user picked, null if the field is empty or isn't a full date yet
     */
    fun onEndDateChange(date: LocalDate?) {
        model.updateSelectedEndDate(date)
    }

    /**
     * Comes from the confirm modal
     */
    fun onConfirmPress() {}

    /**
     * Comes from the decline modal, it declines the reservation and then goes back to the previous screen
     *
     * @param navController the NavController used to go back
     */
    fun onDeclinePress(navController: NavController) {
        model.declineReservation()
        navController.popBackStack()
    }

    /**
     * Triggered in the final confirmation screen as a way to go home from there
     *
     * @param navController the NavController used to go home
     */
    fun goHome(navController: NavController) {}
}
