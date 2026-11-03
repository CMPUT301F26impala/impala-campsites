package com.impala.campala.ui_sections.form_lotteries

import com.impala.campala.ObservableModel
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FormLotteriesModel(
    val repo: LotteryRepository,
    val scope: CoroutineScope,
    val lottery: LotteryModel? = null
): ObservableModel<FormLotteriesModel>() {

    var campsiteIds = if (lottery == null) mutableListOf<String>() else lottery.campsiteIds.toMutableList()

    var maximumParticipants = if (lottery == null) "" else lottery.lotteryCapacity.toString()

    var waitlistSize = if (lottery == null) "" else lottery.waitlistCapacity.toString()

    // lottery model didn't have a variable for this so empty string for now
    var numberOfWinners = ""


    fun updateMaximumParticipants(newParticipants: String) {
        maximumParticipants = newParticipants
        notifyObservers(this)
    }

    fun updateWaitlistSize(newWaitlistSize: String) {
        waitlistSize = newWaitlistSize
        notifyObservers(this)
    }

    fun updateNumberOfWinners(newNumberOfWinners: String) {
        numberOfWinners = newNumberOfWinners
        notifyObservers(this)
    }

    fun addCampsite(campsiteId: String) {
        if (!campsiteIds.contains(campsiteId)) {
            campsiteIds.add(campsiteId)
            notifyObservers(this)
        }
    }

    fun removeCampsite(campsiteId: String) {
        campsiteIds.remove(campsiteId)
        notifyObservers(this)
    }

}

