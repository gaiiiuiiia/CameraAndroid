package com.example.cameraandroid

import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.SparseIntArray
import android.view.Surface
class Photographer(
    private var cameraDevice : CameraDevice,
    private var cameraManager: CameraManager,
    private var photoHandler: PhotoHandler,
    private var rotation: Int)
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

    fun takePhotoRequest(backgroundHandler: Handler?): Pair<ImageReader, CaptureRequest.Builder> {
        val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
        val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(reader.surface)
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))

        reader.setOnImageAvailableListener(photoHandler.getToSaveListener(), backgroundHandler)

        return Pair(reader, captureBuilder)
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