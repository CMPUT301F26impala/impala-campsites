package com.impala.campala.ui_sections.see_reservation;

import androidx.navigation.NavController
import com.impala.campala.Controller
import com.impala.campala.model.ReservationModel

/**
 * Controller for the 'reservations' tab, passes the View's actions on to the model
 *
 * @param model the SeeReservationsModel this controller updates
 */
public class SeeReservationsController(val model: SeeReservationsModel) : Controller {
    public fun initialize() {
        model.initialize()
    }

    /**
     * Marks a reservation as rated, posts the review, and also adjusts the rating values in the campsite itself
     *
     * @param res the past reservation being rated
     * @param rating the rating out of 5
     * @param reviewText the written review, can be empty
     */
    public fun rateReservation(res: ReservationModel, rating: Double, reviewText: String) {
        model.rateReservation(res, rating, reviewText)
    }

    /**
     * Called when a campsite owner confirms rejecting an active reservation
     *
     * @param res the active reservation being rejected
     */
    public fun rejectReservation(res: ReservationModel) {
        model.rejectReservation(res)
    }

    /**
     * Called when the View Messages or Message button is pressed, opens the messages for that reservation
     *
     * @param navController the navController object used for navigation
     * @param res the reservation whose messages are opened
     */
    public fun openMessages(navController: NavController, res: ReservationModel) {
        navController.navigate("messages/${res.id}")
    }
}
