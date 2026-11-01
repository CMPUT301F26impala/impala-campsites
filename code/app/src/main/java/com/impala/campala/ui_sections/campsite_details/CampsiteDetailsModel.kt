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

    fun joinLottery() {
        this.scope.launch {
            user?.let {
                lotteryRepo.joinLottery(lottery!!.id, user.getUID())
                entrantRepo.putEntrantInLottery(entrantProfile!!, lottery!!.id)
            }
        }
    }

    fun leaveWaitlist() {
        this.scope.launch {
            user?.let {
                lotteryRepo.leaveLottery(lottery!!.id, user.getUID())
            }
        }
    }
}