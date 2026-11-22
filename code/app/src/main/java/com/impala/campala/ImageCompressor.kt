package com.impala.campala

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Shrinks images picked by the user so they're small enough to store directly in a Firestore document.
 *
 * I started out by asking Gemini 4 Hydrogen in the gemini.google.com webui the following prompt:
 * "If I have a Jetpack Compose app where I get image Uris from a filepicker and I want to compress them to 500 width (whatever height for the aspect ratio) and 50kb sized, as a blob to then give to firebase. How would I go about compressing images? Don't just write code for it, but explain how I would go about figuring out how to do this?"
 * It sort of helped me sketch out a pipeline:
 * 1: Get the image dimensions (so we can figure out how we want to scale it)
 * 2: Scale the image
 * 3.1: Attempt compression at a given quality figure (webp takes in quality from 0 to 100)
 * 3.2: If the output filesize is too large, rerun the compression with a lower quality number until we reach the filesize we want.
 *
 * I took this pipeline, and started researching Bitmaps (a suggestion from our TA advisor)
 * Then used that to create the following class (code is my own)
 *
 * @param contentResolver an android internal used to read the image data that a uri points to
 * @param maxWidth the width in pixels images are scaled down to, height is scaled to keep the aspect ratio. Narrower images aren't scaled up.
 * @param maxBytes the size in bytes the compressed image should fit under
 */
class ImageCompressor(
    private val contentResolver: ContentResolver,
    private val maxWidth: Int = 500,
    private val maxBytes: Int = 50_000,
) {
    private fun scaleUriToBitmap(uri: Uri): Bitmap {
        // This basically turns a uri (a link) to the actual image data so that we can work on it
        val source = ImageDecoder.createSource(contentResolver, uri)

        // Minh pointed out that we should have a maxHeight too (to prevent images that are like 10,000px tall)
        // So now, IF the image is taller than it is wide, we center crop it to a square
        // Campsite images are landscape, and pfps are square
        // So we'll never need more than square
        val croppedBitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val width = info.size.width
            val height = info.size.height
            if (height > width) {
                val top = (height - width) / 2
                decoder.crop = Rect(0, top, width, top + width)
            }
        }

        // After the crop, now we scale it down if needed
        if (croppedBitmap.width <= maxWidth) {
            return croppedBitmap
        }
        val scaledHeight = maxOf(1, croppedBitmap.height * maxWidth / croppedBitmap.width)
        return Bitmap.createScaledBitmap(croppedBitmap, maxWidth, scaledHeight, true)
    }

    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        var quality = 90
        while (true) {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, output)
            val bytes = output.toByteArray()
            if (bytes.size <= maxBytes || quality <= 0) {
                return bytes
            }
            quality -= 10
        }
    }

    /**
     * Scales down and compresses the image at the Uri so it can be stored in a Firestore document.
     *
     * In the examples I looked at online. withContext() was deemed necessary, it runs this on a separate thread so there isn't lag
     *
     * @param uri the uri returned by an android photo picker
     * @return a webp image file as a ByteArray
     */
    suspend fun compress(uri: Uri): ByteArray = withContext(Dispatchers.Default) {
        val bitmap = scaleUriToBitmap(uri)
        compressBitmap(bitmap)
    }

}
