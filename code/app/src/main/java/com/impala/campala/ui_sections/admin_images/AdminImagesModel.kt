package com.impala.campala.ui_sections.admin_images

import android.util.Log
import com.impala.campala.ObservableModel
import com.impala.campala.model.ImageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Model for the admin's Images tab
 *
 * @param scope the CoroutineScope that the suspend fetches run in
 *
 * @property images every image's bytes keyed by image ID, null if an image has no data
 */
class AdminImagesModel(
    val scope: CoroutineScope
) : ObservableModel<AdminImagesModel>() {
    var images: Map<String, ByteArray?> = emptyMap()
        private set

    /**
     * Loads every image, called whenever the tab is shown
     */
    fun loadImages() {
        this.scope.launch {
            try {
                images = ImageRepository.getImages().associate { it.id to it.data?.toBytes() }
                notifyObservers(this@AdminImagesModel)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AdminImagesModel", "Failed to load images", e)
            }
        }
    }

    /**
     * Called when an admin confirms deleting an image
     * Deletes it and removes it from any campsites or profiles that use it
     *
     * @param imageId ID of the image to delete
     */
    fun deleteImage(imageId: String) {
        this.scope.launch {
            try {
                ImageRepository.deleteImageAndReferences(imageId)
                images = images - imageId
                notifyObservers(this@AdminImagesModel)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AdminImagesModel", "Failed to delete image", e)
            }
        }
    }
}
