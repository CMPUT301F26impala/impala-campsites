package com.impala.campala.ui_sections.view_lotteries

import com.google.firebase.Timestamp
import com.impala.campala.ObservableModel
import com.impala.campala.User
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
 * Which user type the 'Lotteries' screen is being shown to, to control the UI shown
 */
enum class ViewLotteriesViewMode { ENTRANT, CAMPSITE_OWNER }

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
 * Lotteries the entrant lost, or already made a reservation through, are left out, since there's nothing left for them to do there
 * In campsite owner mode this is the list of active lotteries owned by that campsite owner
 *
 * @param lotteryRepo the LotteryRepository to fetch lotteries from
 * @param campsiteRepo the CampsiteRepository, used to get the name/location of each lottery's campsite
 * @param reservationRepo the ReservationRepository, used in entrant mode to hide lotteries the user already reserved through
 * @param user the signed in User, used to find their lotteries
 * @param scope the CoroutineScope that the suspend fetches run in
 * @param viewMode whether this screen is for an entrant or a campsite owner
 *
 * @property lotteries every lottery for this user (minus ones an entrant lost or already reserved through)
 */
class ViewLotteriesModel(
    val lotteryRepo: LotteryRepository,
    val campsiteRepo: CampsiteRepository,
    val reservationRepo: ReservationRepository,
    val user: User,
    val scope: CoroutineScope,
    val viewMode: ViewLotteriesViewMode
) : ObservableModel<ViewLotteriesModel>() {
    var lotteries: List<LotteryListItem> = emptyList()
        private set

    /**
     * This is effectively the list of rows the view should display, with information for each row
     */
    val visibleLotteries: List<LotteryListItem>
        get() {
            if (viewMode == ViewLotteriesViewMode.ENTRANT) return lotteries
            val now = Timestamp.now()
            return lotteries.filter { it.lottery.lotteryEndTime >= now }
        }

    private var loadJob: Job? = null

    /**
     * Fetches the user's lotteries (and their campsites)
     * For entrants, it fetches all of the lotteries they have entered (or waitlisted or won or lost), and it filters to only the ones that 
     */
    fun loadLotteries() {
        loadJob?.cancel()
        loadJob = scope.launch {
            val uid = user.getUID()
            val fetched = when (viewMode) { // Switch block
                ViewLotteriesViewMode.ENTRANT -> {
                    // Get all the lotteries and all the reservations
                    // The reason the .await() calls are after is so that these two fetches can run in parralel instead of one after the other
                    val entered = async { lotteryRepo.getLotteriesByEntrantId(uid) }
                    val reservedLotteryIds = async {
                        reservationRepo.getReservationsByUser(uid).mapNotNull { it.lotteryId }.toSet()
                    }

                    // Filter out lotteries that have been lost or that were won and already reserved
                    val reserved = reservedLotteryIds.await()
                    entered.await().filter { it.id !in reserved && !checkIfEntrantLost(it, uid) }
                }
                ViewLotteriesViewMode.CAMPSITE_OWNER -> lotteryRepo.getLotteriesByOwnerId(uid)
            }.sortedBy { it.lotteryEndTime }

            // Fetch every campsite at the same time instead of one after the other
            lotteries = fetched.map { lottery ->
                async {
                    val campsite = lottery.campsiteIds.firstOrNull()?.let { campsiteRepo.getCampsiteById(it) }
                    val status = if (viewMode == ViewLotteriesViewMode.ENTRANT) getEntrantStatus(lottery, uid) else null
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
