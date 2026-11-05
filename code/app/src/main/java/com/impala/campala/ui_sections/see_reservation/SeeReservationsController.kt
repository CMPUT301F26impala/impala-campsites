package com.impala.campala.ui_sections.see_reservation;

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
     * Marks a reservation as rated and also adjusts the rating values in the campsite itself
     *
     * @param res the past reservation being rated
     * @param rating the rating out of 5
     */
    public fun rateReservation(res: ReservationModel, rating: Double) {
        model.rateReservation(res, rating)
    }

    /**
     * Called when a campsite owner confirms rejecting an active reservation
     *
     * @param res the active reservation being rejected
     */
    public fun rejectReservation(res: ReservationModel) {
        model.rejectReservation(res)
    }
}
