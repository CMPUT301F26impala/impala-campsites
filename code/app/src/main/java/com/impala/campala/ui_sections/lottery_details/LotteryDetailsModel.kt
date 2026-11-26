package com.impala.campala.ui_sections.lottery_details

import com.google.firebase.Timestamp
import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ImageRepository
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

enum class LotteryStatus { UPCOMING, ACTIVE, DRAWN }

/**
 * Model for the Lottery Details screen a campsite owner sees when they tap one of their lotteries
 *
 * @property lottery the lottery being shown, null while it's loading or if it couldn't be found
 * @property campsites the campsites in the lottery, in the order the lottery stores them
 * @property name every campsite's name, comma separated
 * @property location every distinct campsite location, comma separated
 * @property description the description of each campsite, separated by a blank line
 * @property tags every distinct tag across the lottery's campsites
 * @property spotsRemaining how many spots are left in the lottery itself, excluding the waitlist
 * @property status whether the lottery hasn't opened yet, is open, or has been drawn
 * @property waitlist the entrant profiles of everyone on the waitlist, in the order they joined
 * @property selected IDs of the waitlisted people who are checked, they're who a notification gets sent to
 * @property showNotifModal whether the Create notification dialog is open
 * @property notifTitle the title typed into the notification dialog
 * @property notifBody the content typed into the notification dialog
 */
class LotteryDetailsModel : ObservableModel<LotteryDetailsModel>() {
    var lottery: LotteryModel? = null
        private set

    var campsites: List<CampsiteModel> = emptyList()
        private set

    val name: String
        get() = campsites.joinToString(", ") { it.name }

    val location: String
        get() = campsites.map { it.location }.filter { it.isNotBlank() }.distinct()
            .joinToString(", ")

    val description: String
        get() = campsites.map { it.description }.filter { it.isNotBlank() }.joinToString("\n\n")

    val tags: List<String>
        get() = campsites.flatMap { it.tags }.distinct()

    val spotsRemaining: Int
        get() = lottery?.let { (it.lotteryCapacity - it.entrantIds.size).coerceAtLeast(0) } ?: 0

    val status: LotteryStatus
        get() {
            val now = Timestamp.now()
            val current = lottery ?: return LotteryStatus.UPCOMING
            return when {
                now < current.lotteryStartTime -> LotteryStatus.UPCOMING
                now < current.lotteryEndTime -> LotteryStatus.ACTIVE
                else -> LotteryStatus.DRAWN
            }
        }

    var waitlist: List<EntrantProfileModel> = emptyList()
    var waitlistImages: List<ByteArray?> = emptyList()
    var selected = mutableSetOf<String>()
    var showNotifModal = false
        set(value) {
            field = value
            notifyObservers(this)
        }
    var notifTitle: String = ""
        set(value) {
            field = value
            notifyObservers(this)
        }
    var notifBody: String = ""
        set(value) {
            field = value
            notifyObservers(this)
        }

    /**
     * Fetches the lottery, its campsites and the profiles of everyone on its waitlist, then notifies the view
     *
     * @param lotteryId the ID of the lottery to show
     */
    suspend fun loadLottery(lotteryId: String) {
        lottery = null
        campsites = emptyList()
        waitlist = emptyList()
        selected.clear()
        notifyObservers(this)

        val loaded = LotteryRepository.getLotteryById(lotteryId)
        // Fetch every campsite at the same time instead of one after the other
        campsites = coroutineScope {
            loaded?.campsiteIds.orEmpty()
                .map { id -> async { CampsiteRepository.getCampsiteById(id) } }
                .awaitAll()
                .filterNotNull()
        }
        waitlist = loaded?.waitlistIds.orEmpty()
            .map { EntrantProfileRepository.getEntrantProfileById(it) }
            .filterNotNull()
        waitlistImages = waitlist.map { ImageRepository.getImageById(it.imageId)?.data?.toBytes() }
        lottery = loaded
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
