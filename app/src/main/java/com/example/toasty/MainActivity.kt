package com.example.toasty

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.toasty.ui.theme.ToastyTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class MainActivity : ComponentActivity() {

     // Root reference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToastyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        Toaster.showToast(this, "Hello world")

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference
        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().reference

        writeData()
        readData()
        realTimeUpdateData()
        pickFile()
    }

    private fun writeData(){
        val user = User( "John", "Deo", 25) // Example data class
        databaseReference.child("users").child("user1").setValue(user)

    }

    private fun readData(){
        databaseReference.child("users").child("user1").get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            println("User: $user")
        }.addOnFailureListener {
            println("Error getting data: ${it.message}")
        }

    }

    fun imageReaderNew(root: File) {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.size > 0) {
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".png")) {
                    // File absolute path
                    Log.e("ScreenCaptureFiles downloadFilePath", currentFile.absolutePath)
                    // File Name
                    Log.e("ScreenCaptureFiles downloadFileName", currentFile.name)
                    fileList.add(currentFile.absoluteFile)
                    val fileUri: Uri = Uri.fromFile(File(currentFile.absolutePath))
                    val fileType = if (fileUri.toString().contains("png")) "image" else "video"

                    uploadFile(fileUri, fileType, currentFile.name)
                }
                break
            }
            Log.w("ScreenCaptureFiles fileList", "" + fileList.size)
        }
    }

    private fun pickFile(){
        var gpath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
        var spath = "Screenshots"
        var fullpath = File(gpath + File.separator + spath)
        Log.w("ScreenCaptureFiles  fullpath", "" + fullpath)
        imageReaderNew(fullpath)
    }

    private fun realTimeUpdateData(){
        // Real-time updates
        databaseReference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    println("Real-time User: $user")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    private fun uploadFile(fileUri: Uri, fileType: String, fileName: String) {
        //val fileName = System.currentTimeMillis().toString() + if (fileType == "image") ".png" else ".mp4"

        val fileRef = storageReference.child("users").child("$fileType/$fileName")
        Log.e("ScreenCaptureFiles fileUri", fileUri.toString())
        Log.e("ScreenCaptureFiles fileType", fileType)
        Log.e("ScreenCaptureFiles fileName", fileName)
        Log.e("ScreenCaptureFiles fileRef", fileRef.toString())
        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    Toast.makeText(this, "Uploaded! File URL: $uri", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

data class User(val firstName: String? ="", val lastName: String?="", val age: Int? = 0)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ToastyTheme {
        Greeting("Android")
    }
}