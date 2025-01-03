package com.example.toasty

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toasty.security.FirebaseData
import com.example.toasty.security.LocationMap
import com.example.toasty.ui.theme.ToastyTheme
import kotlin.math.max


class MainActivity : ComponentActivity() {

    private val TAG: String = "ToastyMainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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

        val multiplePermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isAllPermissionGranted = permissions.containsValue(false)
            Log.d(TAG, "PermissionHandler call isAllPermissionGranted: $isAllPermissionGranted")
            if (!isAllPermissionGranted) {
                FirebaseData.onInit(this@MainActivity)
            }
            permissions.forEach { (permission, isGranted) ->
                Log.d(TAG, "PermissionHandler call $permission: $isGranted")
                /*if (isGranted) {
                    Toast.makeText(this@MainActivity, "$permission Granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this@MainActivity, "$permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }*/
            }
        }

        SideEffect {
            // Remember a launcher for requesting permissions
            multiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
        ToastyTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Greeting(
                    name = "Android",
                    modifier = Modifier.padding(innerPadding)
                )
                lazyGridWithBitmaps(modifier = Modifier.padding(innerPadding))

            }
        }
    }

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
        LocationMap.removeLocationUpdate()
    }

}