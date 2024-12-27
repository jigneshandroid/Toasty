package com.example.toasty

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toasty.security.FirebaseData
import com.example.toasty.ui.theme.ToastyTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.HashMap
import kotlin.math.max


class MainActivity : ComponentActivity() {




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
                    lazyGridWithBitmaps(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        FirebaseData.onInit(this@MainActivity)
        Toaster.showToast(this, "Hello world")
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
    val availableWidth = screenWidth - horizontalPadding * 2 // Account for padding on both sides
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