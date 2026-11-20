package com.impala.campala.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.impala.campala.model.ReviewModel
import com.impala.campala.model.ReviewRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewRepositoryTest {
    val review = ReviewModel("entrant.id", "campsite.id", 4.5, "Great lake view")
    val otherReview = ReviewModel("other.entrant.id", "campsite.id", 2.0, "")
    val otherCampsiteReview = ReviewModel("entrant.id", "other.campsite.id", 3.0, "Fine")

    val repo = ReviewRepository()

    @Before
    fun setUp() {
        val db = FirebaseFirestore.getInstance()

        val snapshot = Tasks.await(db.collection("reviews").get())

        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }

        // Commit the batch synchronously too
        Tasks.await(batch.commit())
    }

    @Test
    fun getReviewsForCampsite(): Unit = runBlocking {
        assertEquals(0, repo.getReviewsByCampsite("campsite.id").size)

        repo.saveReview(review)
        repo.saveReview(otherReview)
        repo.saveReview(otherCampsiteReview)

        val stored = repo.getReviewsByCampsite("campsite.id")
        assertEquals(setOf(review, otherReview), stored.toSet())
    }

    @Test
    fun deleteReview(): Unit = runBlocking {
        repo.saveReview(review)
        repo.deleteReview(review)

        assertEquals(0, repo.getReviewsByCampsite("campsite.id").size)
    }
}
