package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.Executors
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success->
            if(success){
                providerFileManager.insertImageToStore(photoInfo)
            }

        }
        takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()){ success->
            if(success){
                providerFileManager.insertVideoToStore(videoInfo)
            }
        }

        findViewById<Button>(R.id.photo_button).setOnClickListener{
            isCapturingVideo = false
            checkStoragePermission{
                openImageCapture()
            }
        }
        findViewById<Button>(R.id.video_button).setOnClickListener{
            isCapturingVideo = true
            checkStoragePermission{
                openVideoCapture()
            }
        }
    }

    private fun openImageCapture(){
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        photoInfo?.uri?.let{ uri->
            takePictureLauncher.launch(uri)
        }
    }

    private fun openVideoCapture(){
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        videoInfo?.uri?.let{ uri->
            takeVideoLauncher.launch(uri)
        }
    }

    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT <
            android.os.Build.VERSION_CODES.Q) {
//Check for the WRITE_EXTERNAL_STORAGE permission
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
// If the permission is granted
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
// if the permission is not granted, request the permission
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions,
            grantResults)
        when (requestCode) {
//Check if requestCode is for the External Storage permission or not
                    REQUEST_EXTERNAL_STORAGE -> {
// If granted, open the camera
                if ((grantResults.isNotEmpty() && grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
                return
            }
// for other request code, do nothing
            else -> {
            }
        }
    }

    companion object{
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    private lateinit var providerFileManager: ProviderFileManager
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    private var isCapturingVideo = false
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>
}