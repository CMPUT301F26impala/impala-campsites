package com.impala.campala.ui_sections.campsite_details

import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileModel
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Model for the Campsite Details screen
 *
 * There will be one of these objects, not one for each campsite
 * The attributes will be updated when a different campsite is displayed
 *
 * @param campsiteRepo the repo to fetch relevant campsite from
 * @param lotteryRepo the repo to fetch the campsite lottery from
 * @param entrantRepo the repo to fetch the user's entrant profile from
 * @param user the object which contains the current user's information
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property name the Campsite name
 * @property location the Campsite location
 * @property description the Campsite description
 * @property maxOccupancy the Campsite occupancy limit
 * @property tags the set of tags the Campsite has
 * @property campsite the Campsite object whose details are displayed
 * @property lottery the Lottery for the Campsite, if it exists
 * @property entrantProfile the user's entrant profile, used to enter a lottery
 * @property joinStatus indicates whether a user can join a lottery or the reason they cannot
 * @property userIsOwner flag for if the current user owns the campsite
 * @property lotteryExists flag indicating if there is a lottery for the selected campsite
 */
class CampsiteDetailsModel(
    val campsiteRepo: CampsiteRepository,
    val lotteryRepo: LotteryRepository,
    val entrantRepo: EntrantProfileRepository,
    val user: User?,
    val scope: CoroutineScope
) : ObservableModel<CampsiteDetailsModel>() {
    var name: String = ""
    var location: String = ""
    var description: String = ""
    var maxOccupancy: Int = 0
    var tags: MutableList<String> = mutableListOf()
    var campsite: CampsiteModel? = null
    var lottery: LotteryModel? = null
    var entrantProfile: EntrantProfileModel? = null
    var joinStatus: LotteryModel.ProspectiveJoinStatus? = null
    var userIsOwner: Boolean = false
    var lotteryExists: Boolean = false


    /**
     * Called every time the screen is rendered.
     * Gets a campsite object from the repo by ID and uses it to update the properites of this class.
     * Finds a lottery for the campsite if it exists.
     * Also loads the current user's entrant profile.
     * Will notify the view that a change has been made
     *
     * @param campsiteId the ID of the selected campsite, used to get the campsite from the repo
     */
    suspend fun loadCampsite(campsiteId: String) {
        campsite = campsiteRepo.getCampsiteById(campsiteId)
        this.name = campsite?.name ?: ""
        this.location = campsite?.location ?: ""
        this.description = campsite?.description ?: ""
        this.maxOccupancy = campsite?.maxOccupancy ?: 0
        this.tags = campsite?.tags ?: mutableListOf()

        lottery = lotteryRepo.getLotteryByCampsiteId(campsiteId)

        user?.let {
            entrantProfile = entrantRepo.getEntrantProfileById(user.getUID())
            lottery?.let {
                joinStatus = it.evaluateUserAction(user.getUID())
                lotteryExists = true
            }
        }

        notifyObservers(this)
    }

    /**
     * Called when the entrant tries to join the lottery or waitlist
     */
    fun joinLottery() {
        this.scope.launch {
            user?.let {
                lotteryRepo.joinLottery(lottery!!.id, user.getUID())
                entrantRepo.putEntrantInLottery(entrantProfile!!, lottery!!.id)
            }
        }
    }

    /**
     * Called when the entrant tries to leave the waitlist
     */
    fun leaveWaitlist() {
        this.scope.launch {
            user?.let {
                lotteryRepo.leaveLottery(lottery!!.id, user.getUID())
            }
        }
    }
}