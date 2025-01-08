package com.example.toasty.security

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.toasty.R
import com.example.toasty.services.ScreenRecordingService


class ScreenRecordingActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_recording)

        val startButton: Button = findViewById(R.id.startRecording)
        val stopButton: Button = findViewById(R.id.stopRecording)
        val startScreenShotButton: Button = findViewById(R.id.startScreenshots)
        val stopScreenShotButton: Button = findViewById(R.id.stopScreenshots)

        startButton.setOnClickListener {
            recordScreen = true
            if(checkAudioPermission()) startScreenRecording()
        }

        stopButton.setOnClickListener {
            stopScreenRecording()
        }

        startScreenShotButton.setOnClickListener {
            recordScreen = false
            if(checkAudioPermission()) startScreenRecording()
        }

        stopScreenShotButton.setOnClickListener {
            stopScreenRecording()
        }

    }

    private fun checkAudioPermission(): Boolean{
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                SCREEN_AUDIO_RECORD_CODE
            )
            false
        }else{
            true
        }
    }

    private fun startScreenRecording() {
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, SCREEN_RECORD_REQUEST_CODE)
    }

    private fun stopScreenRecording() {
        val serviceIntent = Intent(this, ScreenRecordingService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenRecording()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCREEN_RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
                    putExtra("RESULT_CODE", resultCode)
                    putExtra("DATA", data)
                }
                startForegroundService(serviceIntent)
                Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Error Recording MediaRecorder: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == SCREEN_AUDIO_RECORD_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Audio Permission Granted", Toast.LENGTH_SHORT).show()
        }  else if (requestCode == SCREEN_STORAGE_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val SCREEN_RECORD_REQUEST_CODE = 1001
        private const val SCREEN_AUDIO_RECORD_CODE = 1002
        private const val SCREEN_STORAGE_CODE = 1003
        var recordScreen = true
        lateinit var mediaProjectionManager: MediaProjectionManager
    }
}
