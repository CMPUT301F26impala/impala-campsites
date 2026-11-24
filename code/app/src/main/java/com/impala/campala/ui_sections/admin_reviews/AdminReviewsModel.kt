package com.impala.campala.ui_sections.admin_reviews

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.EntrantProfileRepository
import com.impala.campala.model.ReviewModel
import com.impala.campala.model.ReviewRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for reviews branch of admin ui
 *
 * Stores all the reviews gathered from the repository with some additional details
 *
 * @param scope the CoroutineScope
 * @property allStoredCampsiteReviews list of reviews
 * @property campsiteNamesByCampsiteId Campsite names mapped with IDs
 * @property reviewerNamesByUserId names of reviewer mapped with IDs
 * @property isFetchingStoredReviews if the reviews are still being gathered
 * @property wasReviewFetchUnsuccessful if gathering reviews failed
 */
class AdminReviewsModel(
    val scope: CoroutineScope
) : ObservableModel<AdminReviewsModel>() {

    var allStoredCampsiteReviews: List<ReviewModel> = emptyList()
    var campsiteNamesByCampsiteId: Map<String, String> = emptyMap()
    var reviewerNamesByUserId: Map<String, String> = emptyMap()
    var isFetchingStoredReviews = false
    var wasReviewFetchUnsuccessful = false
    var selectedReviewForDeletion: ReviewModel? = null
    var isCurrentlyDeletingReview = false
    var wasReviewDeletionUnsuccessful = false

    /**
     * Gather all the reviews and details and run when admin clicks reveiw tab in nav
     */
    fun loadAllStoredReviews() {
        if (isFetchingStoredReviews || isCurrentlyDeletingReview) { return }
        isFetchingStoredReviews = true
        wasReviewFetchUnsuccessful = false
        notifyObservers(this)
        scope.launch {
            try {
                allStoredCampsiteReviews = ReviewRepository.fetchAllStoredReviews()
                loadAssociatedReviewDetails()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                wasReviewFetchUnsuccessful = true
                Log.e("AdminReviewsModel", "Failed to fetch stored reviews", e)
            } finally {
                isFetchingStoredReviews = false
                notifyObservers(this@AdminReviewsModel)
            }
        }
    } // end of loading reviews

    /**
     * gathers the names of the campsites and details of the stored review including user detail
     */
    suspend fun loadAssociatedReviewDetails() {
        val fetchedCampsiteDisplayNames = mutableMapOf<String, String>()
        val fetchedReviewerDisplayNames = mutableMapOf<String, String>()

        for (currentStoredReview in allStoredCampsiteReviews) {
            val reviewedCampsiteId = currentStoredReview.campsiteId
            val reviewPosterUserId = currentStoredReview.posterId
            if (!fetchedCampsiteDisplayNames.containsKey(reviewedCampsiteId)) {
                val associatedCampsite = CampsiteRepository.getCampsiteById(reviewedCampsiteId)
                var reviewedCampsiteDisplayName = "Unknown Campsite"
                if (associatedCampsite != null && associatedCampsite.name.isNotBlank()) { reviewedCampsiteDisplayName = associatedCampsite.name }
                fetchedCampsiteDisplayNames[reviewedCampsiteId] = reviewedCampsiteDisplayName
            }

            // fetch the reviewer's name if it hasn't already been fetched
            if (!fetchedReviewerDisplayNames.containsKey(reviewPosterUserId)) {
                val associatedEntrantProfile = EntrantProfileRepository.getEntrantProfileById(reviewPosterUserId)
                var reviewPosterDisplayName = "Unknown User"
                if (associatedEntrantProfile != null && associatedEntrantProfile.name.isNotBlank()) { reviewPosterDisplayName = associatedEntrantProfile.name }
                fetchedReviewerDisplayNames[reviewPosterUserId] = reviewPosterDisplayName
            }
        } // end of loop

        campsiteNamesByCampsiteId = fetchedCampsiteDisplayNames
        reviewerNamesByUserId = fetchedReviewerDisplayNames
    } // end of loading associated details

    /**
     * open deletion dialog for user to see
     *
     * @param selectedCampsiteReview clicked review
     */
    fun openReviewDeletionConfirmation(selectedCampsiteReview: ReviewModel) {
        if (isCurrentlyDeletingReview) { return }
        selectedReviewForDeletion = selectedCampsiteReview
        wasReviewDeletionUnsuccessful = false
        notifyObservers(this)
    }

    /**
     * close dialog of deletion without actually deleting anything
     */
    fun closeReviewDeletionConfirmation() {
        if (isCurrentlyDeletingReview) { return }
        selectedReviewForDeletion = null
        wasReviewDeletionUnsuccessful = false
        notifyObservers(this)
    }

    /**
     * actual deletion of the review
     */
    fun deleteSelectedCampsiteReview() {
        if (isCurrentlyDeletingReview) { return }
        val campsiteReviewToBeDeleted = selectedReviewForDeletion
        if (campsiteReviewToBeDeleted == null) { return }
        isCurrentlyDeletingReview = true
        wasReviewDeletionUnsuccessful = false
        notifyObservers(this)

        scope.launch {
            try {
                ReviewRepository.deleteReview(campsiteReviewToBeDeleted)
                allStoredCampsiteReviews = allStoredCampsiteReviews.filter { it.id != campsiteReviewToBeDeleted.id }
                selectedReviewForDeletion = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                wasReviewDeletionUnsuccessful = true
                Log.e("AdminReviewsModel", "Failed to delete campsite review", e)
            } finally {
                isCurrentlyDeletingReview = false
                notifyObservers(this@AdminReviewsModel)
            }
        }
    } // end of review dleete
}