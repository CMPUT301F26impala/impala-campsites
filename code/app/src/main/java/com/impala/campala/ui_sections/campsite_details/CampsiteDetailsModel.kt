package com.impala.campala.ui_sections.campsite_details

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlin.uuid.Uuid

class CampsiteDetailsModel(
    val repo: CampsiteRepository,
    val scope: CoroutineScope
) : ObservableModel<CampsiteDetailsModel>() {
    var name: String = ""
    var location: String = ""
    var description: String = ""
    var maxOccupancy: Int = 0
    var tags: MutableList<String> = mutableListOf()
    var campsite: CampsiteModel? = null

    suspend fun loadCampsite(campsiteId: String) {
        campsite = repo.getCampsiteById(campsiteId)
        this.name = campsite?.name ?: ""
        this.location = campsite?.location ?: ""
        this.description = campsite?.description ?: ""
        this.maxOccupancy = campsite?.maxOccupancy ?: 0
        this.tags = campsite?.tags ?: mutableListOf()
        notifyObservers(this)
    }

    fun joinLottery() {
        // TODO: Call a function on lottery object
    }
}