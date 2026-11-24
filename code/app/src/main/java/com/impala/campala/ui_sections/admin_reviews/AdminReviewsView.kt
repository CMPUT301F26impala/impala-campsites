package com.impala.campala.ui_sections.admin_reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import com.impala.campala.model.ReviewModel

/**
 * View for the reviews branch of the admin ui
 *
 * @param controller the AdminReviewsController used to send actions to the model
 */
class AdminReviewsView(
    val controller: AdminReviewsController
) : ViewObserver<AdminReviewsModel> {

    private var allStoredCampsiteReviews by mutableStateOf(listOf<ReviewModel>())
    private var campsiteNamesByCampsiteId by mutableStateOf(mapOf<String, String>())
    private var reviewerNamesByUserId by mutableStateOf(mapOf<String, String>())
    private var isFetchingStoredReviews by mutableStateOf(false)
    private var wasReviewFetchUnsuccessful by mutableStateOf(false)

    /**
     * Puts review details from the model into the ui.
     *
     * @param model the model containing the required details
     */
    override fun update(model: AdminReviewsModel) {
        allStoredCampsiteReviews = model.allStoredCampsiteReviews
        campsiteNamesByCampsiteId = model.campsiteNamesByCampsiteId
        reviewerNamesByUserId = model.reviewerNamesByUserId
        isFetchingStoredReviews = model.isFetchingStoredReviews
        wasReviewFetchUnsuccessful = model.wasReviewFetchUnsuccessful
    }

    init { update(controller.model) }

    /**
     * Render the main review screen whenever the tab is opened
     *
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render() {
        // initial show
        LaunchedEffect(Unit) { controller.onFirstRender() }
        if (isFetchingStoredReviews) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (wasReviewFetchUnsuccessful) {
            Text("Failed to load reviews!")
        } else if (allStoredCampsiteReviews.isEmpty()) {
            Text("No reviews found!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allStoredCampsiteReviews, key = { it.id }) { currentStoredReview ->
                    DrawStoredReview(currentStoredReview)
                    HorizontalDivider()
                }
            } // end of lazy column
        }
    } // end of render for personal ref

    /**
     * display available details of each review
     *
     * @param currentStoredReview the review to show
     */
    @Composable
    private fun DrawStoredReview(currentStoredReview: ReviewModel) {
        var reviewedCampsiteDisplayName = campsiteNamesByCampsiteId[currentStoredReview.campsiteId]
        var reviewPosterDisplayName = reviewerNamesByUserId[currentStoredReview.posterId]
        if (reviewedCampsiteDisplayName == null) { reviewedCampsiteDisplayName = "Unknown Campsite" }
        if (reviewPosterDisplayName == null) { reviewPosterDisplayName = "Unknown User" }

        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = reviewedCampsiteDisplayName,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = reviewPosterDisplayName,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("%.1f Stars".format(currentStoredReview.rating), style = MaterialTheme.typography.titleMedium)
            }
            if (currentStoredReview.reviewText.isNotBlank()) { // if we have text as well in review; like the written text
                Text(currentStoredReview.reviewText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } // end of stored review for personal ref
}