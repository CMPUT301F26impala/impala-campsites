package com.impala.campala.ui_sections.admin_reviews

import com.impala.campala.Controller
import com.impala.campala.model.ReviewModel

/**
 * Controller for the admin's reviews tab
 *
 * @param model the AdminReviewsModel this controller updates
 */
class AdminReviewsController(val model: AdminReviewsModel) : Controller {
    fun initialize() {
        model.initialize()
    }

    /**
     * Called when an admin confirms deleting a review
     *
     * @param review the review being deleted
     */
    fun deleteReview(review: ReviewModel) {
        model.deleteReview(review)
    }
}
