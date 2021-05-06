package com.example.cameraandroid

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.util.SparseIntArray
import android.view.Surface

class Photograph(
    private var cameraDevice: CameraDevice,
    private var cameraManager: CameraManager)
{
    private var width: Int  = 640
    private var height: Int = 480

    companion object {
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    init
    {
        setSize()
    }

    fun takePhoto()
    {
        val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)

    }

    private fun setSize()
    {
        val jpegSize = cameraManager
            .getCameraCharacteristics(cameraDevice.id)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageFormat.JPEG)

        if (!jpegSize.isNullOrEmpty()) {
            width = jpegSize[0].width
            height = jpegSize[0].height
        }
    }

}