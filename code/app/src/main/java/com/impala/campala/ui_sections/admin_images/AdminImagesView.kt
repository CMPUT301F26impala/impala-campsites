package com.impala.campala.ui_sections.admin_images

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.impala.campala.ImageDisplay
import com.impala.campala.ViewObserver

/**
 * The admin's Images tab, a grid of every image in the app where each one can be deleted
 *
 * @param controller the AdminImagesController instance that is connected to the model
 */
class AdminImagesView(val controller: AdminImagesController) : ViewObserver<AdminImagesModel> {
    private var images by mutableStateOf<Map<String, ByteArray?>>(emptyMap())

    // No KDoc because that's described in the ViewObserver contract
    override fun update(model: AdminImagesModel) {
        this.images = model.images
    }

    /**
     * The main render function to output the screen
     *
     * Also asks the controller to load the images whenever the tab is shown
     *
     * @return Unit, but it does render Composable UI as a side effect
     */
    @Composable
    fun Render() {
        LaunchedEffect(Unit) {
            controller.onFirstRender()
        }

        var imagePendingDeletion by remember { mutableStateOf<String?>(null) }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(images.entries.toList(), key = { it.key }) { (imageId, imageBytes) ->
                Box(modifier = Modifier.aspectRatio(1f)) {
                    ImageDisplay(
                        imageBytes = imageBytes,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                    FilledIconButton(
                        onClick = { imagePendingDeletion = imageId },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Image")
                    }
                }
            }
        }

        imagePendingDeletion?.let { imageId ->
            AlertDialog(
                onDismissRequest = { imagePendingDeletion = null },
                title = { Text("Confirm Image Deletion") },
                text = { Text("Are you sure you want to remove this image? It will be deleted from Campala's storage and removed from any campsites or profiles it was associated with. This cannot be undone.") },
                dismissButton = {
                    TextButton(onClick = { imagePendingDeletion = null }) { Text("Go Back") }
                },
                confirmButton = {
                    TextButton(onClick = {
                        imagePendingDeletion = null
                        controller.deleteImage(imageId)
                    }) { Text("Delete") }
                }
            )
        }
    }
}
