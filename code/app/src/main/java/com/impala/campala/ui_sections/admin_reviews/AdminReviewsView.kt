package com.impala.campala.ui_sections.admin_reviews

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impala.campala.ViewObserver
import com.impala.campala.model.ReviewModel
import com.impala.campala.ui.theme.AppTheme
import com.impala.campala.ui_sections.components.CardDetail

/**
 * View for the admin's reviews ta
 *
 * @param controller the AdminReviewsController that handles this View's actions
 */
class AdminReviewsView(val controller: AdminReviewsController) : ViewObserver<AdminReviewsModel> {
    var reviews by mutableStateOf<List<ReviewModel>>(emptyList())
    var campsiteNames by mutableStateOf<Map<String, String>>(emptyMap())

    private var pendingDelete by mutableStateOf<ReviewModel?>(null)

    override fun update(model: AdminReviewsModel) {
        reviews = model.reviews
        campsiteNames = model.campsiteNames
    }

    @Composable
    fun Render() {
        LaunchedEffect(Unit) {
            controller.initialize()
        }

        if (reviews.isEmpty()) {
            Text("No reviews yet", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(reviews, key = { it.id }) { review ->
                    RenderReview(review)
                }
            }
        }

        pendingDelete?.let { review ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete review?") },
                text = { Text("This review will be permanently removed.") },
                confirmButton = {
                    TextButton(onClick = {
                        controller.deleteReview(review)
                        pendingDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
                }
            )
        }
    }

    @Composable
    private fun RenderReview(review: ReviewModel) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    campsiteNames[review.campsiteId] ?: "Unknown campsite",
                    style = MaterialTheme.typography.headlineSmall
                )
                CardDetail(Icons.Default.Star, "%.1f Stars".format(review.rating))
                if (review.reviewText.isNotBlank()) {
                    Text(review.reviewText, style = MaterialTheme.typography.bodyLarge)
                }
            }
            IconButton(onClick = { pendingDelete = review }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete review",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}