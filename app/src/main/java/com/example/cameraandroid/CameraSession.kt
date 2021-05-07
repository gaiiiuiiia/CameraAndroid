package com.example.cameraandroid

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast

class CameraSession(
    private val activity: Activity,
    private val textureView: TextureView
) {
    private val manager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private val textureListener = initTextureListener()
    private val cameraStateCallback = initCameraStateCallback()
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    fun takePhoto()
    {
        cameraDevice?.let { cameraDevice ->

            val photoHandler = PhotoHandler(
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
            )
            val photographer = Photographer(
                                    cameraDevice,
                                    manager,
                                    photoHandler,
                                    activity.windowManager.defaultDisplay.rotation)
            val res: Pair<ImageReader, CaptureRequest.Builder> = photographer.takePhotoRequest(backgroundHandler)

            val outputSurfaces = mutableListOf(
                OutputConfiguration(res.first.surface),
                OutputConfiguration(Surface(textureView.surfaceTexture))
            )

            cameraDevice.createCaptureSession(
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputSurfaces,
                    activity.mainExecutor,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            session.capture(
                                res.second.build(),
                                object : CameraCaptureSession.CaptureCallback() {
                                    override fun onCaptureCompleted(
                                        session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult
                                    ) {
                                        super.onCaptureCompleted(session, request, result)
                                        createCameraPreview()
                                        Toast.makeText(activity, R.string.photo_success, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                backgroundHandler
                            )
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }
                )
            )
        }
    }

    fun sessionResume()
    {
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
        startBackgroundThread()
    }

    fun sessionPause()
    {
        cameraDevice?.close()
        textureView.surfaceTextureListener = null
        stopBackgroundThread()
    }

    private fun openCamera()
    {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Constants.REQUEST_PERMISSION_CAMERA
            )
            return
        }

        getCameraId()?.let {
            manager.openCamera(it, cameraStateCallback, backgroundHandler)
        }
    }

    private fun createCameraPreview()
    {
        val texture = textureView.surfaceTexture
        texture?.setDefaultBufferSize(previewWidth, previewHeight)
        val surface = Surface(texture)
        // запрос на предоставление изображения с камеры
        val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        builder?.addTarget(surface)
        val stateCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                builder?.let {
                    it.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    session.setRepeatingRequest(
                        it.build(),
                        null,
                        backgroundHandler
                    )
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }

        cameraDevice?.createCaptureSession(
            SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                listOf(OutputConfiguration(surface)),
                activity.mainExecutor,
                stateCallback
            )
        )
    }

    private fun startBackgroundThread()
    {
        backgroundThread = HandlerThread("Camera Thread")
        backgroundThread?.start()
        backgroundThread?.let {
            backgroundHandler = Handler(it.looper)
        }
    }

    private fun stopBackgroundThread()
    {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(javaClass.simpleName, e.message, e)
        }
    }

    private fun initTextureListener() = object : TextureView.SurfaceTextureListener
    {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            previewWidth = width
            previewHeight = height
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            previewWidth = width
            previewHeight = height
        }
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean =false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun initCameraStateCallback() = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun getCameraId(): String?
    {
        return manager.cameraIdList.first { id ->
            manager.getCameraCharacteristics(id)
            .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }
}