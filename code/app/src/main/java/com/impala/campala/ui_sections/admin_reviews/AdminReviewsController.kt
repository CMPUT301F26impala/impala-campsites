package com.impala.campala.ui_sections.admin_reviews

import com.impala.campala.Controller

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
}