package com.example.toasty.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Toast
import com.example.toasty.security.FirebaseDataActivity.Companion.mediaProjectionManager
import com.example.toasty.security.FirebaseDataActivity.Companion.recordScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScreenRecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "ScreenRecordingChannel"
        const val NOTIFICATION_ID = 1
        private lateinit var mediaProjection: MediaProjection
        private var mediaRecorder: MediaRecorder? = null
        private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
        private lateinit var imageReader: ImageReader
        private var surface: Surface? = null
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            buildNotification("Screen recording is active.")
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("RESULT_CODE", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("DATA")

        mediaProjection =
            mediaProjectionManager.getMediaProjection(resultCode, data!!)
        if (recordScreen) {
            // start screen recording
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    setupMediaRecorder()
                    withContext(Dispatchers.Main) {
                        // Ready to start recording
                        surface?.let { startScreenRecording(it) }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            // Start taking screenshots
            setupImageReader()
            startScreenCapture()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (recordScreen) stopScreenRecording()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Recording")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Screen Recording")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }
    }

    private fun setupImageReader() {
        val metrics = resources.displayMetrics
        val screenDensity = metrics.densityDpi
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
        Log.d(
            "ScreenRecordingApp",
            "setupImageReader:screenWidth $screenWidth -screenHeight- $screenHeight -screenDensity- $screenDensity"
        )
    }

    private fun startScreenCapture() {
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.let {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * image.width

                val bitmap = Bitmap.createBitmap(
                    image.width + rowPadding / pixelStride,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()
                Log.d("ScreenRecordingApp", "startScreenCapture: ${bitmap.toString()}")
                saveBitmap(bitmap)
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun saveBitmap(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val filename = "screenshot_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Screenshots"
                )
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
                uri.toString()
            }
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Screenshots"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "screenshot_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                Log.d("ScreenRecordingApp", "Screenshot saved: ${file.absolutePath}")
                Toast.makeText(this, "Screenshot saved: ${file.absolutePath}", Toast.LENGTH_LONG)
                    .show()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("ScreenRecordingApp", "Failed to save screenshot: ${e.toString()}")
                Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMediaRecorder() {
        try {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ScreenshotsVideo"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "screen_record${System.currentTimeMillis()}.mp4")
            val filePath = file.absolutePath
            val metrics = resources.displayMetrics
            //val filePath = "${getExternalFilesDir(null)?.absolutePath}/screen_record.mp4"
            /*            CoroutineScope(Dispatchers.IO).launch {
                            val file = File(getExternalFilesDir(null), "screen_record.mp4")
                            if (!file.exists()) file.createNewFile()
                        }*/

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(filePath)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                //setVideoSize(metrics.widthPixels, metrics.heightPixels)
                setVideoSize(1080, 1920)
                setVideoEncodingBitRate(512 * 1000)
                setVideoFrameRate(30)

                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                //prepare()
            }

            mediaRecorder?.prepare()
            surface = mediaRecorder?.surface
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startScreenRecording(surface: Surface) {
        virtualDisplay?.release()
        val metrics = resources.displayMetrics
        val screenDensity = metrics.densityDpi
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenRecording",
            1080, 1920, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )

        mediaRecorder?.start()
    }

    private fun stopScreenRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            virtualDisplay?.release()
            virtualDisplay = null
            mediaProjection?.stop()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
