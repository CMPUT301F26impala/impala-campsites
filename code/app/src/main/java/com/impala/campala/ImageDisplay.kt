package com.impala.campala

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * This is a reusable component (like Box) that renders an image if one is provided, otherwise just a white square
 *
 * The image is cropped to fill whatever size the modifier gives it.
 *
 * @param imageBytes the compressed image bytes straight from firebase
 * @param modifier sets the size and shape of the image, the white box uses the same size and shape
 */
@Composable
fun ImageDisplay(imageBytes: ByteArray?, modifier: Modifier = Modifier) {
    // The Image primitive doesn't support just passing bytes in, you have to decode it to a bitmap
    val bitmap = remember(imageBytes) {
        imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "User Uploaded Image",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color.White))
    }
}
