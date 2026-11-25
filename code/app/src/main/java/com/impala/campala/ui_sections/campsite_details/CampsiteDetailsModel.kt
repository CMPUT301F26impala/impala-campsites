package com.impala.campala.ui_sections.campsite_details

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.User
import com.impala.campala.UserViewMode
import com.impala.campala.model.CampsiteModel
import com.impala.campala.model.CampsiteOwnerProfileRepository
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.ImageRepository
import com.impala.campala.model.LotteryModel
import com.impala.campala.model.LotteryRepository
import com.impala.campala.model.ReservationModel
import com.impala.campala.model.ReservationRepository
import com.impala.campala.model.ReviewModel
import com.impala.campala.model.ReviewRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import qrcode.QRCode

/**
 * Model for the Campsite Details screen
 *
 * There will be one of these objects, not one for each campsite
 * The attributes will be updated when a different campsite is displayed
 *
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property name the Campsite name
 * @property location the Campsite location
 * @property description the Campsite description
 * @property maxOccupancy the Campsite occupancy limit
 * @property tags the set of tags the Campsite has
 * @property averageRating the Campsite's average rating out of 5, null if nobody has rated it yet
 * @property imageBytes the Campsite's compressed image, null if it has no image
 * @property campsite the Campsite object whose details are displayed
 * @property lottery the Lottery for the Campsite, if it exists
 * @property joinStatus indicates whether a User can join a lottery or the reason they cannot
 * @property userIsOwner flag for if the current User owns the campsite
 * @property userIsAdmin flag for if the current User is viewing as an admin, which shows the delete and ban buttons
 * @property lotteryExists flag indicating if there is a lottery for the selected campsite
 * @property reservedDates the campsite's upcoming, non-cancelled reservations sorted by start date, only loaded for the owner
 * @property reviews the reviews entrants have left on the campsite
 */
class CampsiteDetailsModel(
    val scope: CoroutineScope
) : ObservableModel<CampsiteDetailsModel>() {
    var name: String = ""
    var location: String = ""
    var description: String = ""
    var maxOccupancy: Int = 0
    var tags: MutableList<String> = mutableListOf()
    var averageRating: Double? = null
    var imageBytes: ByteArray? = null
    var campsite: CampsiteModel? = null
    var lottery: LotteryModel? = null
    var joinStatus: LotteryModel.ProspectiveJoinStatus? = null
    var userIsOwner: Boolean = false
    var userIsAdmin: Boolean = false
    var lotteryExists: Boolean = false
    var reservedDates: List<ReservationModel> = emptyList()
    var reviews: List<ReviewModel> = emptyList()
    var qrCode: QRCode? = null

    /**
     * Called every time the screen is rendered.
     * Gets a campsite object from the repo by ID and uses it to update the properites of this class.
     * Finds a lottery for the campsite if it exists.
     * Also loads the current User's entrant profile.
     * Will notify the view that a change has been made
     *
     * @param campsiteId the ID of the selected campsite, used to get the campsite from the repo
     */
    suspend fun loadCampsite(campsiteId: String) {
        campsite = CampsiteRepository.getCampsiteById(campsiteId)
        this.name = campsite?.name ?: ""
        this.location = campsite?.location ?: ""
        this.description = campsite?.description ?: ""
        this.maxOccupancy = campsite?.maxOccupancy ?: 0
        this.tags = campsite?.tags ?: mutableListOf()
        this.averageRating = campsite?.averageRating
        this.qrCode = null

        val imageId = campsite?.imageId ?: ""
        this.imageBytes = if (imageId.isNotEmpty()) ImageRepository.getImageById(imageId)?.data?.toBytes() else null

        userIsOwner = User.getCurrentRole() == UserViewMode.CAMPSITE_OWNER &&
                CampsiteOwnerProfileRepository.getCampsiteOwnerProfileById(User.getUID())?.ownedCampsites?.contains(campsiteId) == true

        userIsAdmin = User.getCurrentRole() == UserViewMode.ADMIN

        // Only the owner sees the reserved dates, cancelled and past reservations are left out
        reservedDates = if (userIsOwner) {
            ReservationRepository.getReservationsByCampsite(campsiteId)
                .filter { !it.hasBeenCancelled && !it.isPast }
                .sortedBy { it.startDate }
        } else {
            emptyList()
        }

        // Everyone sees the reviews, owner or entrant
        reviews = ReviewRepository.getReviewsByCampsite(campsiteId)

        lottery = LotteryRepository.getLotteryByCampsiteId(campsiteId)

        // This model is shared between campsites, so clear the last campsite's lottery state
        // Otherwise a campsite with no lottery would still show the previous campsite's join button
        lotteryExists = false
        joinStatus = null

        lottery?.let {
            joinStatus = it.evaluateUserAction(User.getUID())
            lotteryExists = true
        }

        notifyObservers(this)
    }

    /**
     * Called when the entrant tries to join the lottery or waitlist
     * Then reloads the campsite so the button shows the new join status
     */
    fun joinLottery() {
        this.scope.launch {
            LotteryRepository.joinLottery(lottery!!.id, User.getUID())
            campsite?.let { loadCampsite(it.id) }
        }
        notifyObservers(this)
    }

    /**
     * Called when the entrant tries to leave the waitlist
     * Then reloads the campsite so the button shows the new join status
     */
    fun leaveWaitlist() {
        this.scope.launch {
            LotteryRepository.leaveLottery(lottery!!.id, User.getUID())
            campsite?.let { loadCampsite(it.id) }
        }
        notifyObservers(this)
    }

    /**
     * Called when the owner generates a QR code
     * Creates a new QR Code with the campsite ID which will link to the details page
     */
    fun generateQrCode() {
        qrCode = QRCode.ofSquares().build("campala://${campsite!!.id}")
        notifyObservers(this)
    }

    /**
     * Called when an admin confirms deleting the campsite
     *
     * @param onDeleted called once the campsite is deleted, used to leave the screen
     */
    fun deleteCampsite(onDeleted: () -> Unit) {
        val campsite = campsite ?: return
        this.scope.launch {
            try {
                CampsiteRepository.softDeleteCampsite(campsite)
                onDeleted()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CampsiteDetailsModel", "Failed to delete campsite", e)
            }
        }
    }

    /**
     * Called when an admin confirms banning the campsite's owner
     *
     * @param onBanned called once the owner is banned, used to leave the screen
     */
    fun banOwner(onBanned: () -> Unit) {
        val campsite = campsite ?: return
        this.scope.launch {
            try {
                CampsiteOwnerProfileRepository.banCampsiteOwner(campsite.ownerId)
                onBanned()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("CampsiteDetailsModel", "Failed to ban campsite owner", e)
            }
        }
    }
}