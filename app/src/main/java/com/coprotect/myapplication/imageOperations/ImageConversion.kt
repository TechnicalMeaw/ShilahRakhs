package com.coprotect.myapplication.imageOperations

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class ImageConversion {
    companion object{
        fun bitmapToByteArray(bitmap: Bitmap, imageSize: Int): ByteArray {
            val newBitmap = ImageResizer.generateThumb(bitmap, imageSize)
            val stream = ByteArrayOutputStream()
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)

            return stream.toByteArray()
        }
    }
}