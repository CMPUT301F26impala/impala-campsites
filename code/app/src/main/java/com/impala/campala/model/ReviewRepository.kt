package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for storing the app's campsite reviews.
 *
 * Model for anything in the app that wants to post or show reviews
 */
object ReviewRepository {
    private val collection by lazy { Firebase.firestore.collection("reviews") }

    /**
     * Gets all reviews left on one campsite
     *
     * Used for the list of reviews on the campsite details screen
     *
     * @param campsiteId ID of the campsite
     *
     * @return list of the campsite's reviews
     */
    suspend fun getReviewsByCampsite(campsiteId: String): List<ReviewModel> {
        return collection.whereEqualTo("campsiteId", campsiteId).get().await()
            .map { ReviewModel.load(it) }
    }

    /**
     * Gathers all reviews left by entrants across all campsites for a list of reviews
     *
     * @return list of all the reviews
     */
    suspend fun fetchAllStoredReviews(): List<ReviewModel> {
        val allStoredReviewDocuments = collection.get().await()
        val allRetrievedCampsiteReviews = allStoredReviewDocuments.map { ReviewModel.load(it) }
        return allRetrievedCampsiteReviews
    }

    /**
     * Saves a review to the database
     *
     * @param review ReviewModel to be saved
     */
    suspend fun saveReview(review: ReviewModel): Unit {
        review.save().await()
    }

    /**
     * Deletes a review from the database
     *
     * @param review ReviewModel to delete
     */
    suspend fun deleteReview(review: ReviewModel): Unit {
        collection.document(review.id).delete().await()
    }
}
