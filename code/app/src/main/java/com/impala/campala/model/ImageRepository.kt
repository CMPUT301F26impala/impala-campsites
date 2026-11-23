package com.impala.campala.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Repository for storing the app's images.
 *
 * Images should already be compressed (see ImageCompressor) before they're saved
 */
object ImageRepository {
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    /**
     * Gets a single image by its ID
     *
     * @param id ID of the image to fetch
     *
     * @return The matching image, or null if it doesn't exist or the fetch fails
     */
    suspend fun getImageById(id: String): ImageModel? {
        return try {
            val snapshot = db.collection("images").document(id).get().await()
            snapshot.toObject(ImageModel::class.java) // null if the document doesn't exist
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves an image to the database, creating it if it doesn't exist yet
     *
     * @param image ImageModel to be saved
     */
    suspend fun saveImage(image: ImageModel): Unit {
        image.save().await()
    }

    /**
     * Deletes an image from the database
     *
     * @param id ID of the image to delete
     */
    suspend fun deleteImage(id: String): Unit {
        db.collection("images").document(id).delete().await()
    }
}
