package com.impala.campala.ui_sections.reservation_related

import com.google.firebase.Timestamp
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
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
 * @param resDB the ReservationRepository to fetch reservations from
 * @param campsiteDB the CampsiteRepository to fetch each reservation's campsite from
 * @param user the signed in User
 * @param scope the CoroutineScope that the suspend fetches run in
 */
public class SeeReservationsModel(val resDB: ReservationRepository, val campsiteDB: CampsiteRepository, val user: User, val scope: CoroutineScope) : ObservableModel<SeeReservationsModel>() {
    var campsites: List<CampsiteModel> = emptyList()
    var reservations: List<ReservationModel> = emptyList()
    var oldReservations: List<ReservationModel> = emptyList()
    var newReservations: List<ReservationModel> = emptyList()

    public fun initialize() {
        scope.launch {
            val currentDate: com.google.firebase.Timestamp = Timestamp.now()
            val loaded = resDB.getReservationsByUser(user.getUID()).sortedBy { it.startDate }
            // Load each campsite once, all at the same time, before showing the list so it never shows ids
            // Any that can't be found (eg: deleted) are just left out
            campsites = coroutineScope {
                loaded.map { it.campsiteId }.distinct().map { campsiteId ->
                    async { campsiteDB.getCampsiteById(campsiteId) }
                }.awaitAll().filterNotNull()
            }
            reservations = loaded
            oldReservations = reservations.filter {it.endDate < currentDate}
            newReservations = reservations.filter {it.endDate >= currentDate}
            notifyObservers(this@SeeReservationsModel)
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
