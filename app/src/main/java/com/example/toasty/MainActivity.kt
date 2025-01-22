package com.example.toasty

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Switch
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.full.memberProperties
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.toasty.common.CommonUtils
import com.example.toasty.models.User
import com.example.toasty.security.FirebaseData
import com.example.toasty.security.FirebaseDataActivity
import com.example.toasty.services.FirebaseDataService
import com.example.toasty.services.FirebaseDataService.Companion
import com.example.toasty.services.ScreenRecordingService.Companion.NOTIFICATION_ID
import com.example.toasty.ui.theme.ToastyTheme
import com.example.toasty.workmanager.MyWorker
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.reflect.KMutableProperty


class MainActivity : ComponentActivity() {


    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private val TAG: String = "ToastyMainActivity"
        var lifecycleOwner: LifecycleOwner? = null
        val user = User(
            "John",
            "Deo",
            25,
            lastOpenedApp = false,
            uploadDocuments = false,
            uploadImages = false,
            uploadVideos = false,
            locationStart = false,
            screenshotStart = false,
            videoRecordingStart = false
        )
        val mutableChildUserSnapShot = MutableStateFlow(user)
        val childUserSnapShot = mutableChildUserSnapShot.asStateFlow()
        // Permissions to request
        private val permissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private val manageStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkManageExternalStoragePermission()
        }

    private val multiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            Log.d(TAG, "PermissionHandler call $permission: $isGranted")
            if (isGranted) {
                Log.d(TAG, "$permission Granted")
            } else {
                Log.d(TAG, "$permission Denied")
            }
        }
        /*     val isAllPermissionGranted = permissions.containsValue(false)
             Log.d(TAG, "PermissionHandler call isAllPermissionGranted: $isAllPermissionGranted")
             if (!isAllPermissionGranted) {
                 FirebaseData.onInit(this@MainActivity)
             }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            lifecycleOwner = LocalLifecycleOwner.current
            PermissionHandler()
        }


    }

    /*    @Composable
        fun MyComposable() {
            val isLoading = remember { mutableStateOf(false) }
            val data = remember { mutableStateOf(listOf<String>()) }

            // Define a LaunchedEffect to perform a long-running operation asynchronously
            // `LaunchedEffect` will cancel and re-launch if
            // `isLoading.value` changes
            LaunchedEffect(isLoading.value) {
                if (isLoading.value) {
                    // Perform a long-running operation, such as fetching data from a network
                    val newData = fetchData()
                    // Update the state with the new data
                    data.value = newData
                    isLoading.value = false
                }
            }

            Column {
                Button(onClick = { isLoading.value = true }) {
                    Text("Fetch Data")
                }
                if (isLoading.value) {
                    // Show a loading indicator
                    CircularProgressIndicator()
                } else {
                    // Show the data
                    LazyColumn {
                        items(data.value.size) { index ->
                            Text(text = data.value[index])
                        }
                    }
                }
            }
        }

        @Composable
        fun TimerScreen() {
            val elapsedTime = remember { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val scope = CoroutineScope(Dispatchers.Default)
                val job = scope.launch {
                    while (true) {
                        delay(1000)
                        elapsedTime.value += 1
                        Log.d(TAG,"Timer is still working ${elapsedTime.value}")
                    }
                }

                onDispose {
                    job.cancel()
                }
            }

            Text(
                text = "Elapsed Time: ${elapsedTime.value}",
                modifier = Modifier.padding(16.dp),
                fontSize = 24.sp
            )
        }
        // Simulate a network call by suspending the coroutine for 2 seconds
        private suspend fun fetchData(): List<String> {
            // Simulate a network delay
            delay(2000)
            return listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5",)
        }*/

    @Composable
    fun PermissionHandler() {
        //val context = LocalContext.current
        SideEffect {
            // Remember a launcher for requesting permissions
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                // Request only the permissions that are not granted
                multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                Log.d(TAG, "All permissions already granted")
            }

            // Check and request MANAGE_EXTERNAL_STORAGE permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkAndRequestManageExternalStoragePermission()
            }
        }
        ToastyTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Greeting(
                    name = "Android",
                    modifier = Modifier.padding(innerPadding)
                )

                //lazyGridWithBitmaps(modifier = Modifier.padding(innerPadding))

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkAndRequestManageExternalStoragePermission() {
        if (!isManageExternalStoragePermissionGranted()) {
            // Launch the settings screen for MANAGE_EXTERNAL_STORAGE
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            manageStoragePermissionLauncher.launch(intent)
        } else {
            FirebaseData.onInit(this@MainActivity)
            Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission already granted")
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun isManageExternalStoragePermissionGranted(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isManageExternalStoragePermissionGranted()) {
                Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission granted")
            } else {
                Log.d(TAG, "MANAGE_EXTERNAL_STORAGE permission denied")
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        val userData = childUserSnapShot.collectAsState().value
        val deviceInfo = CommonUtils.getDeviceInfo(this@MainActivity)
        Column (Modifier.padding(0.dp, 50.dp)) {
            Text(
                text = "Show location on map $deviceInfo",
                modifier = modifier
            )
            Button(onClick = {
                /*LocationMap.locations.value?.let {
                    LocationMap.showLocationOnExternalMap(this@MainActivity,
                        it
                    )
                }*/

                //callWorkManager()
                //val serviceIntent = Intent(this@MainActivity, FirebaseDataService::class.java)
                //startService(serviceIntent)
                //FirebaseDataService.startFirebaseDataService(this@MainActivity)
                //testThreadPool()
                pendingIntent()

            }) {
                Text("Open in Google Maps")
            }
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //val numberOfKeys = User::class.memberProperties.size
                val properties = User::class.memberProperties
                properties.forEachIndexed  { index, property ->
                    val key = property.name
                    val value = property.getter.call(userData)
                    Text(
                        text = "$key: $value"
                    )
                    if(value is Boolean) {
                        var isSwitchOn by remember { mutableStateOf(value) }
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Display the toggle state
                            Text(
                                text = if (isSwitchOn) "Switch is ON" else "Switch is OFF",
                                modifier = Modifier.padding(0.dp, 5.dp)
                            )

                            // Switch composable
                            Switch(
                                checked = isSwitchOn,
                                onCheckedChange = { site ->
                                    isSwitchOn = site
                                    //userData.lastOpenedApp = isSwitchOn
                                    val modelKey = userData::class.memberProperties.find { it.name == key }

                                    // Check if the property is mutable (var), and if it is, set the value
                                    if (modelKey is KMutableProperty<*>) {
                                        modelKey.setter.call(mutableChildUserSnapShot, isSwitchOn) // Set the value dynamically
                                    } else {
                                        Log.d(TAG,"Property $modelKey is not mutable!")
                                    }
                                    //childUserSnapShot.value = userData
                                    FirebaseDataService.lastOpenedApp = isSwitchOn
                                    FirebaseDataService.databaseReference.child("user")
                                        .setValue(userData)
                                }
                            )
                        }
                    }
                }
            }
            //lazyGridWithBitmaps(modifier)
        }
    }

    private fun pendingIntent(){
        val intent = Intent(this@MainActivity, ChatGpyActivity::class.java)

// Wrap the intent in a PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this@MainActivity,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

// Use the PendingIntent in a notification
        val notification = NotificationCompat.Builder(this@MainActivity, "CHANNEL_ID")
            .setContentTitle("Notification Title")
            .setContentText("Notification Content")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent) // Triggered when notification is clicked
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this@MainActivity).notify(NOTIFICATION_ID, notification)
    }

    /*private fun testThreadPool(){
        val threadPool: ExecutorService = Executors.newFixedThreadPool(5)
        // Submit tasks to the thread pool
        for (i in 1..10) {
            threadPool.submit {
                Log.d(TAG,
                    "Task " + i + " is running on " + Thread.currentThread().name
                )
                try {
                    Thread.sleep(1000) // Simulate task execution
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            threadPool.shutdown()
        }
        // Shut down the thread pool
    }*/

    /*private fun callWorkManager() {
        val inputData = Data.Builder()
            .putString("key", "value")
            .build()
        //val workRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val periodicWorkRequest = PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        val workManager = WorkManager.getInstance(this@MainActivity)
        workManager.enqueueUniquePeriodicWork(
            "MyPeriodicWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )

        lifecycleOwner?.let {
            workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
                .observe(it, Observer { workInfo ->
                    if (workInfo != null && workInfo.state.isFinished) {
                        // Handle completion
                        Log.d(MyWorker.TAG, "Work Finished: ${workInfo.outputData}")
                    } else {
                        Log.d(MyWorker.TAG, "Work State: ${workInfo?.state}")
                    }
                })
        }
    }*/



    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        ToastyTheme {
            Greeting("Android")
        }
    }

    @Composable
    fun lazyGridWithBitmaps(modifier: Modifier = Modifier) {
        // Sample bitmap resources (replace with your bitmap source)
        val bitmapIds = listOf(
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background,
            R.drawable.ic_launcher_background
        )

        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val horizontalPadding = 16.dp
        val itemSpacing = 8
        val availableWidth =
            screenWidth - horizontalPadding * 2 // Account for padding on both sides
        val minCellWidth = 150.dp // Minimum width for each item
        val columns = max(1, (availableWidth / (minCellWidth + itemSpacing.dp)).toInt())

// Calculate the number of rows based on the number of items and columns
        val rows = (bitmapIds.size + columns - 1) / columns
        // Calculate the height of the grid based on item height and vertical spacing
        val itemHeight = 150
        val gridHeight = (rows * itemHeight) + ((rows - 1) * itemSpacing)



        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),  // Dynamically calculated number of columns
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
            modifier = Modifier
                .fillMaxWidth()  // Make sure the grid fills the width of the screen
                .height(gridHeight.dp)  // Set the height of the grid dynamically
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            items(bitmapIds.size) { index ->
                val bitmapId = bitmapIds[index]

                // Load the bitmap
                /*       val bitmap: Bitmap? = BitmapFactory.decodeResource(
                           LocalContext.current.resources,
                           bitmapId
                       )
           */

                // Display the bitmap in an Image composable
                bitmapId?.let {
                    Image(
                        painterResource(R.drawable.ic_launcher_background),
                        contentDescription = "Bitmap Image",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}

data class AppInfo(
    val name: String,
    val packageName: String
)