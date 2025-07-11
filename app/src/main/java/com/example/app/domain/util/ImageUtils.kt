package com.example.app.domain.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.IOException

/** Utility for safely loading scaled bitmaps from a URI. */
fun loadScaledBitmap(context: Context, uri: Uri, maxSize: Int = 1024): Bitmap? {
    return try {
        // Decode bounds first
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        val inSample = calculateInSampleSize(options.outWidth, options.outHeight, maxSize, maxSize)
        val opts = BitmapFactory.Options().apply { inSampleSize = inSample }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, opts)
        }
    } catch (e: IOException) {
        null
    }
}

private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    var w = width
    var h = height
    while (w / inSampleSize > reqWidth || h / inSampleSize > reqHeight) {
        inSampleSize *= 2
    }
    return inSampleSize
}
