package com.impala.campala.ui_sections.view_lotteries

import androidx.navigation.NavController
import com.impala.campala.Controller
import com.impala.campala.UserViewMode

/**
 * Controller for the 'Lotteries' screen
 *
 * Largely just acts as a middleman to pass data and click actions from the View to the model
 *
 * @param model the ViewLotteriesModel this controller updates
 */
class ViewLotteriesController(val model: ViewLotteriesModel) : Controller {
    /**
     * Called in a LaunchedEffect in the View, loads the lotteries whenever the screen is shown
     */
    fun onFirstRender() {
        model.loadLotteries()
    }

    /**
     * Called when a campsite owner taps a lottery row, takes them to the edit lottery screen
     *
     * For entrants this does nothing
     *
     * @param item the lottery row that was tapped
     * @param navController the root NavController, assumes 'editLottery/{id}' is on this nav stack
     */
    fun onLotteryPress(item: LotteryListItem, navController: NavController) {
        if (model.viewMode == UserViewMode.CAMPSITE_OWNER) {
            navController.navigate("editLottery/${item.lottery.id}")
        }
    }

    /**
     * Called when an entrant who won a lottery taps Reserve
     *
     * @param item the lottery row the Reserve button belongs to
     * @param navController the root NavController, assumes 'campsite/{id}' is on this nav stack
     */
    fun onReservePress(item: LotteryListItem, navController: NavController) {
        // TODO!!! NAVIGATE TO RESERVE SCREEN
    }

    /**
     * Called when a campsite owner taps the Add Lottery button, takes them to the lottery form screen
     *
     * @param navController the navController, assumes that a screen called 'addLottery' is on this nav stack
     */
    fun onAddLotteryPress(navController: NavController) {
        navController.navigate("addLottery")
    }
}
