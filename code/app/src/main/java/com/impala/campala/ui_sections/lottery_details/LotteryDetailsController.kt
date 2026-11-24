package com.impala.campala.ui_sections.lottery_details

import androidx.navigation.NavController
import com.impala.campala.Controller
import com.impala.campala.User
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.NotificationModel

/**
 * Controller for the Lottery Details screen
 *
 * @param model the LotteryDetailsModel this controller updates
 */
class LotteryDetailsController(val model: LotteryDetailsModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the lottery whenever the screen is shown
     *
     * @param lotteryId the ID of the lottery to show
     */
    suspend fun onOpen(lotteryId: String) {
        model.loadLottery(lotteryId)
    }

    /**
     * Called when the edit button is pressed, takes the owner to the edit lottery screen
     *
     * @param navController the root NavController, assumes 'editLottery/{id}' is on this nav stack
     * @param lotteryId the ID of the lottery to edit
     */
    fun onEditPress(navController: NavController, lotteryId: String) {
        navController.navigate("editLottery/$lotteryId")
    }

    fun onCheckedChange(camper: EntrantProfileModel){
        if (camper.userId in model.selected) model.removeSelected(camper.userId)
        else model.addSelected(camper.userId)
    }

    fun notifyBtn(){
        model.showNotifModal=true
    }

    fun modalDismiss(){
        model.showNotifModal=false
    }

    fun onNotifTitleInput(title: String){
        model.notifTitle=title
    }
    fun onNotifBodyInput(body: String){
        model.notifBody=body
    }

    fun sendNotif(){
        NotificationModel(
            model.notifTitle,
            model.notifBody,
            User.getUID(),
            model.selected.toMutableList()
        ).save()
        modalDismiss()
        // TODO: show some visual feedback of notif sent
    }
}
