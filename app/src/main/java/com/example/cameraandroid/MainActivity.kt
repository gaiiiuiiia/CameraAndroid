package com.example.cameraandroid

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import com.example.cameraandroid.Constants.REQUEST_PERMISSION_CAMERA
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var textureView: TextureView
    private lateinit var btnTakePhoto: FloatingActionButton
    private lateinit var cameraSession: CameraSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textureView = findViewById(R.id.textureView)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)

        cameraSession = CameraSession(this, textureView)

        btnTakePhoto.setOnClickListener {
            cameraSession.takePhoto()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED
                || grantResults[1] == PackageManager.PERMISSION_DENIED)
                {
                Toast.makeText(this, R.string.camera_not_allowed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cameraSession.sessionPause()
    }

    override fun onResume() {
        super.onResume()
        cameraSession.sessionResume()
    }

}