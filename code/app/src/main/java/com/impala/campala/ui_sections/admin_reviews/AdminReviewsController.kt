package com.impala.campala.ui_sections.admin_reviews

import com.impala.campala.Controller
import com.impala.campala.model.ReviewModel

/**
 * Controller for the reviews branch of admin ui
 *
 * @param model the AdminReviewsModel from which it uses functions
 */
class AdminReviewsController(
    val model: AdminReviewsModel
) : Controller {

    /**
     * Initial load for all reviews
     */
    fun onFirstRender() {
        model.loadAllStoredReviews()
    }

    /**
     * confirmation dialog for deletion of a review
     *
     * @param selectedCampsiteReview the review selected
     */
    fun onReviewDeletePressed(selectedCampsiteReview: ReviewModel) {
        model.openReviewDeletionConfirmation(selectedCampsiteReview)
    }

    /**
     * cancel the deletion of a review
     */
    fun onReviewDeleteCancel() {
        model.closeReviewDeletionConfirmation()
    }

    /**
     * confirm deletion of a review
     */
    fun onReviewDeleteConfirm() {
        model.deleteSelectedCampsiteReview()
    }
}