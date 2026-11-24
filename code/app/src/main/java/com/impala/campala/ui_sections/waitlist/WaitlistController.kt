package com.impala.campala.ui_sections.waitlist

import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.NotificationModel

class WaitlistController(val waitlistModel: WaitlistModel) {
    suspend fun onOpen(lotteryId: String){
        waitlistModel.loadLottery(lotteryId)
    }
    fun onCheckedChange(camper: EntrantProfileModel){
        if (camper.userId in waitlistModel.selected) waitlistModel.removeSelected(camper.userId)
        else waitlistModel.addSelected(camper.userId)
    }

    fun notifyBtn(){
        waitlistModel.showNotifModal=true
    }

    fun modalDismiss(){
        waitlistModel.showNotifModal=false
    }

    fun onNotifTitleInput(title: String){
        waitlistModel.notifTitle=title
    }
    fun onNotifBodyInput(body: String){
        waitlistModel.notifBody=body
    }

    fun sendNotif(){
        NotificationModel(
            waitlistModel.notifTitle,
            waitlistModel.notifBody,
            User.getUID(),
            waitlistModel.selected.toMutableList()
        ).save()
        modalDismiss()
        // TODO: show some visual feedback of notif sent
    }
}