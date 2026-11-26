package com.impala.campala.ui_sections.create_reservation

import android.content.Context
import androidx.navigation.NavController
import com.impala.campala.User
import com.impala.campala.model.NotificationModel
import com.impala.campala.model.NotificationService
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
     * Sets the reservation date range to user's choice
     */
    fun onDateRangeChange(start: LocalDate?, end: LocalDate?) {
        model.updateSelectedDateRange(start, end)
    }

    /**
     * Comes from the confirm modal
     */
    fun onConfirmPress(context: Context) {
        model.completeReservation()
        // technically, the notification should be sent from the server when it detects a new reservation document
        // is created. but because of our constraints, that would have latency, so we'll call this "optimistic update"
        val notif = NotificationModel(
            title = "Campsite reserved",
            body = "${model.selectedCampsite!!.name} reserved from ${model.dateRangeStart} to ${model.dateRangeEnd}",
            User.getUID(),
            mutableListOf(User.getUID()),
            true
        )
        NotificationService.show(context, notif.title, notif.body)
        notif.save()
    }

    /**
     * Comes from the decline modal, it declines the reservation and then goes back to the previous screen
     *
     * @param navController the NavController used to go back
     */
    fun onDeclinePress(navController: NavController, context: Context) {
        model.declineReservation()
        val notif = NotificationModel(
            title = "Campsite cancelled",
            body = "${model.selectedCampsite!!.name} cancelled",
            User.getUID(),
            mutableListOf(User.getUID()),
            true
        )
        NotificationService.show(context, notif.title, notif.body)
        notif.save()
        navController.popBackStack()
    }

    /**
     * Triggered in the final confirmation screen as a way to go home from there
     *
     * @param navController the NavController used to go home
     */
    fun goHome(navController: NavController) {
        // Pops everything off the stack until it's back at the home tabs
        navController.popBackStack("home", false)
    }
}
