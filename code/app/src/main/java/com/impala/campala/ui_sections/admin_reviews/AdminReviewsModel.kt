package com.impala.campala.ui_sections.admin_reviews

import com.impala.campala.ObservableModel
import com.impala.campala.model.CampsiteRepository
import com.impala.campala.model.ReviewModel
import com.impala.campala.model.ReviewRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the admin's reviews tab, which controls viewing all reviews and removing them
 *
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property reviews Every review in the database
 * @property campsiteNames A helper to get campsite names from their ID
 */
class AdminReviewsModel(
    val scope: CoroutineScope
) : ObservableModel<AdminReviewsModel>() {
    var reviews: List<ReviewModel> = emptyList()
    var campsiteNames: Map<String, String> = emptyMap()

    /**
     * Loads all reviews and the names of the campsites they belong to
     */
    fun initialize() {
        scope.launch {
            reviews = ReviewRepository.getAllReviews()
            campsiteNames = CampsiteRepository.getFilteredCampsites().associate { it.id to it.name }
            notifyObservers(this@AdminReviewsModel)
        }
    }

    /**
     * Deletes a review and takes its rating back out of the campsite's average, then reloads the list
     *
     * @param review the review being deleted
     */
    fun deleteReview(review: ReviewModel) {
        scope.launch {
            ReviewRepository.deleteReview(review)
            // Only adjust the rating if the campsite still exists, otherwise the update would fail
            if (campsiteNames.containsKey(review.campsiteId)) {
                CampsiteRepository.removeRating(review.campsiteId, review.rating)
            }
            initialize()
        }
    }
}
