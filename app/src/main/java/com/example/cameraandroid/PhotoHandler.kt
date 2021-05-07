package com.example.cameraandroid

import android.media.Image
import android.media.ImageReader
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoHandler(
    private var saveDirectory: String
) {
    fun getToSaveListener(): ImageReader.OnImageAvailableListener
    {
        return ImageReader.OnImageAvailableListener { reader_ ->
            var image: Image? = null
            try {
                image = reader_.acquireLatestImage()
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                FileOutputStream(getFileToSave()).use {
                    it.write(bytes)
                }

            } catch (e: FileNotFoundException) {
                Log.e(javaClass.simpleName, e.message, e)
            } catch (e: IOException) {
                Log.e(javaClass.simpleName, e.message, e)
            } finally {
                image?.close()
            }
        }
    }

    private fun getFileToSave(): File {
        return File(saveDirectory, createFileName())
    }

    private fun createFileName(): String {
        return SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.getDefault())
            .format(Calendar.getInstance().time) + ".jpg"
    }
}