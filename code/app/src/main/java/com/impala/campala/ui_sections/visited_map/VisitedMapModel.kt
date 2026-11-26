package com.impala.campala.ui_sections.visited_map

import com.google.firebase.firestore.GeoPoint
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * Model for the map of campsites the entrant has been to
 *
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property points Map locations of the visited campsites, filters out the ones without a location
 */
class VisitedMapModel(val scope: CoroutineScope) : ObservableModel<VisitedMapModel>() {
    var points: List<GeoPoint> = emptyList()

    /**
     * Fetches the entrant's past reservations, then the campsites they were for, and updates observers
     */
    fun loadVisitedCampsites() {
        scope.launch {
            val visitedCampsiteIds = ReservationRepository.getReservationsByUser(User.getUID())
                .filter { it.isPast && !it.hasBeenCancelled }
                .map { it.campsiteId }
                .distinct() // staying at the same campsite twice only needs one pin

            val visitedCampsites = visitedCampsiteIds
                .map { id -> async { CampsiteRepository.getCampsiteById(id) } }
                .awaitAll()
                .filterNotNull()

            points = visitedCampsites.mapNotNull { it.coordinates }
            notifyObservers(this@VisitedMapModel)
        }
    }
}
