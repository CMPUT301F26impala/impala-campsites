package com.impala.campala.ui_sections.view_lotteries

import com.google.firebase.Timestamp
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import com.impala.campala.model.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * Where an entrant stands in a lottery, used to pick what text/buttons to show on their row
 */
enum class EntrantLotteryStatus { WAITING_FOR_DRAW, ON_WAITLIST, WON }

/**
 * Since a lottery only stored CampsiteIDs, this wrapper class allows for us to associate the Campsites in a lottery with that lottery
 * That makes rendering easier because a given row in the list can just use one of these objects
 *
 * @property lottery the lottery this row is for
 * @property campsite the first campsite in the lottery, null if it couldn't be found
 * @property entrantStatus where the current user stands in this lottery, only set in entrant mode
 */
data class LotteryListItem(
    val lottery: LotteryModel,
    val campsite: CampsiteModel?,
    val entrantStatus: EntrantLotteryStatus? = null
) {
    /**
     * How many spots are left in the lottery itself, excluding waitlist
     * This is only shown for campsite owners, not entrants
     */
    val spotsRemaining: Int
        get() = (lottery.lotteryCapacity - lottery.entrantIds.size).coerceAtLeast(0)
}

/**
 * In entrant mode this is the list of lotteries the user has joined, with status on if they've won it or not
 * Lotteries the entrant lost, declined, or already made a reservation through, are left out, since there's nothing left for them to do there
 * In campsite owner mode this is the list of active lotteries owned by that campsite owner
 *
 * @param lotteryRepo the LotteryRepository to fetch lotteries from
 * @param campsiteRepo the CampsiteRepository, used to get the name/location of each lottery's campsite
 * @param reservationRepo the ReservationRepository, used in entrant mode to hide lotteries the user already reserved through
 * @param user the signed in User, used to find their lotteries
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property lotteries every lottery for this user (minus ones an entrant lost or already reserved through)
 * @property viewMode the user's role when the lotteries were last loaded, CAMPSITE_OWNER shows the owner UI, anything else shows the entrant UI
 */
class ViewLotteriesModel(
    val lotteryRepo: LotteryRepository,
    val campsiteRepo: CampsiteRepository,
    val reservationRepo: ReservationRepository,
    val user: User,
    val scope: CoroutineScope
) : ObservableModel<ViewLotteriesModel>() {
    var lotteries: List<LotteryListItem> = emptyList()
        private set

    var viewMode: UserViewMode = UserViewMode.ENTRANT
        private set

    /**
     * This is effectively the list of rows the view should display, with information for each row
     */
    val visibleLotteries: List<LotteryListItem>
        get() {
            if (viewMode != UserViewMode.CAMPSITE_OWNER) return lotteries
            val now = Timestamp.now()
            return lotteries.filter { it.lottery.lotteryEndTime >= now }
        }

    private var loadJob: Job? = null

    /**
     * Fetches the user's lotteries (and their campsites)
     * For entrants, it fetches all of the lotteries they have entered (or waitlisted or won or lost), and it filters to only the ones that 
     */
    fun loadLotteries() {
        // Checked on every load since the user can switch roles while the app is open
        val role = if (user.getCurrentRole() == UserViewMode.CAMPSITE_OWNER) UserViewMode.CAMPSITE_OWNER else UserViewMode.ENTRANT
        // Since the below code is async and could take a while, we should clear the previous data first to make sure we're only showing up to date stuff
        viewMode = role
        lotteries = emptyList()
        notifyObservers(this)

        loadJob?.cancel()
        loadJob = scope.launch {
            val uid = user.getUID()
            val fetched = when (role) { // Switch block
                UserViewMode.CAMPSITE_OWNER -> lotteryRepo.getLotteriesByOwnerId(uid)
                else -> {
                    // Get all the lotteries and all the reservations
                    // The reason the .await() calls are after is so that these two fetches can run in parralel instead of one after the other
                    val entered = async { lotteryRepo.getLotteriesByEntrantId(uid) }
                    val reservedLotteryIds = async {
                        reservationRepo.getReservationsByUser(uid).mapNotNull { it.lotteryId }.toSet()
                    }

                    // Filter out lotteries that have been lost, or that were won and then already reserved or declined
                    val reserved = reservedLotteryIds.await()
                    entered.await().filter { it.id !in reserved && uid !in it.declinedIds && !checkIfEntrantLost(it, uid) }
                }
            }.sortedBy { it.lotteryEndTime }

            // Fetch every campsite at the same time instead of one after the other
            lotteries = fetched.map { lottery ->
                async {
                    val campsite = lottery.campsiteIds.firstOrNull()?.let { campsiteRepo.getCampsiteById(it) }
                    val status = if (role != UserViewMode.CAMPSITE_OWNER) getEntrantStatus(lottery, uid) else null
                    LotteryListItem(lottery, campsite, status)
                }
            }.awaitAll()
            notifyObservers(this@ViewLotteriesModel)
        }
    }

    private fun getEntrantStatus(lottery: LotteryModel, uid: String): EntrantLotteryStatus {
        return when (uid) {
            in lottery.winnerIds -> {
                EntrantLotteryStatus.WON
            }
            in lottery.waitlistIds -> {
                EntrantLotteryStatus.ON_WAITLIST
            }
            else -> {
                EntrantLotteryStatus.WAITING_FOR_DRAW
            }
        }
    }

    // A lottery is considered LOST if there are winners and you are not one of them
    // No winners might mean it still hasn't drawn
    // So this checks for that
    private fun checkIfEntrantLost(lottery: LotteryModel, uid: String): Boolean {
        return lottery.winnerIds.isNotEmpty() && uid !in lottery.winnerIds
    }
}
