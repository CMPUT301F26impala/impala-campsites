package com.impala.campala.ui_sections.see_reservation

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.collections.List

/**
 * Model for the reservations tab screen
 *
 * In entrant mode this is the user's own reservations, in campsite owner mode it's the reservations made at the user's campsites
 *
 * @param resDB the ReservationRepository to fetch reservations from
 * @param campsiteDB the CampsiteRepository to fetch each reservation's campsite from
 * @param entrantDB the EntrantProfileRepository, used in campsite owner mode to get the name of whoever made each reservation
 * @param user the signed in User
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property viewMode Indicates whether the user viewing the screen is in Entrant or Campsite Owner mode
 * @property campsites A list of the campsites that appear in the displayed reservations
 * @property entrants A list of the entrants that appear in the displayed reservations
 * @property reservations A list of all reservations to be displayed
 * @property oldReservations A sublist of the reservations with those uncancelled ones that have finished
 * @property newReservations A sublist of the reservations with those uncancelled ones that haven't finished yet
 * @property cancelledReservations A sublist of the reservations with those that have been cancelled
 */
public class SeeReservationsModel(val resDB: ReservationRepository, val campsiteDB: CampsiteRepository, val entrantDB: EntrantProfileRepository, val user: User, val scope: CoroutineScope) : ObservableModel<SeeReservationsModel>() {
    var viewMode: UserViewMode = UserViewMode.ENTRANT
    var campsites: List<CampsiteModel> = emptyList()
    var entrants: List<EntrantProfileModel> = emptyList()
    var reservations: List<ReservationModel> = emptyList()
    var oldReservations: List<ReservationModel> = emptyList()
    var newReservations: List<ReservationModel> = emptyList()
    var cancelledReservations: List<ReservationModel> = emptyList()

    /**
     * Clear old data and initialize new data
     */
    public fun initialize() {
        val role = if (user.getCurrentRole() == UserViewMode.CAMPSITE_OWNER) UserViewMode.CAMPSITE_OWNER else UserViewMode.ENTRANT

        // Clear old stale data while we load the new data
        reservations = emptyList()
        oldReservations = emptyList()
        newReservations = emptyList()
        cancelledReservations = emptyList()
        viewMode = role
        notifyObservers(this)

        scope.launch {
            val fetched = if (viewMode == UserViewMode.CAMPSITE_OWNER) resDB.getReservationsByOwner(user.getUID()) else resDB.getReservationsByUser(user.getUID())
            val loaded = fetched.sortedBy { it.startDate }
            // Load each campsite once, all at the same time, before showing the list so it never shows ids
            // Any that can't be found (eg: deleted) are just left out
            campsites = coroutineScope {
                loaded.map { it.campsiteId }.distinct().map { campsiteId ->
                    async { campsiteDB.getCampsiteById(campsiteId) }
                }.awaitAll().filterNotNull()
            }
            // Same thing but for the entrant who made each reservation, only the campsite owner screen shows these
            if (viewMode == UserViewMode.CAMPSITE_OWNER) {
                entrants = coroutineScope {
                    loaded.map { it.entrantId }.distinct().map { entrantId ->
                        async { entrantDB.getEntrantProfileById(entrantId) }
                    }.awaitAll().filterNotNull()
                }
            }
            reservations = loaded
            // isPast only counts a stay as past once its last day is over, comparing endDate to now directly would move it a day early
            oldReservations = reservations.filter {it.isPast && !it.hasBeenCancelled}
            newReservations = reservations.filter {!it.isPast && !it.hasBeenCancelled}
            cancelledReservations = reservations.filter {it.hasBeenCancelled}
            notifyObservers(this@SeeReservationsModel)
        }
    }

    /**
     * Marks a reservation as cancelled, then reloads so it moves to the cancelled list
     *
     * @param res the active reservation being rejected
     */
    public fun rejectReservation(res: ReservationModel) {
        scope.launch {
            resDB.flagAsCancelled(res.id)
            initialize()
        }
    }

    /**
     * Marks a reservation as rated and also adjusts the rating values in the campsite itself
     *
     * @param res the past reservation being rated
     * @param rating the rating out of 5
     */
    public fun rateReservation(res: ReservationModel, rating: Double) {
        scope.launch {
            resDB.flagAsRated(res.id)
            campsiteDB.addRating(res.campsiteId, rating)
        }
    }
}
