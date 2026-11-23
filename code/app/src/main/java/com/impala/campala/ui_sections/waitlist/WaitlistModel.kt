package com.impala.campala.ui_sections.waitlist

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.LotteryRepository

class WaitlistModel : ObservableModel<WaitlistModel>() {
    var waitlist: List<EntrantProfileModel> = emptyList()
    var selected = mutableSetOf<String>()
    var showNotifModal=false
        set(value) {
            field=value
            notifyObservers(this)
        }
    var notifTitle: String=""
        set(value) {
            field=value
            notifyObservers(this)
        }
    var notifBody: String=""
        set(value) {
            field=value
            notifyObservers(this)
        }
    suspend fun loadLottery(lotteryId: String) {
        val lottery = LotteryRepository.getLotteryById(lotteryId)!!
        waitlist = lottery.waitlistIds
            .map { EntrantProfileRepository.getEntrantProfileById(it) }
            .filterNotNull()
        notifyObservers(this)
    }

    fun addSelected(id: String) {
        selected.add(id)
        notifyObservers(this)
    }

    fun removeSelected(id: String) {
        selected.remove(id)
        notifyObservers(this)
    }
}