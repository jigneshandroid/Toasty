package com.example.toasty.compose

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.toasty.R
import com.example.toasty.navigationgraph.Screen

@Composable
fun GridExample() {
    val items = listOf(
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_background
    ) // Replace with actual drawable resources

    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Creates a 2-column grid
        modifier = Modifier
            .height(565.dp)
            .padding(8.dp),
        content = {
            items(items.size) { index ->
                GridItem(imageRes = items[index])
            }
        }
    )
}

@Composable
fun GridHorizontalExample() {
    val items = listOf(
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
    ) // Replace with actual drawable resources

    LazyHorizontalGrid(rows = GridCells.Adaptive(80.dp), // Creates a 2-column grid
        modifier = Modifier
            .height(150.dp)
            .padding(8.dp)
            .background(Color.Cyan),
        content = {
            items(items.size) { index ->
                GridItem(imageRes = items[index])
            }
        }
    )
}

@Composable
fun GridItem(imageRes: Int) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Grid Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGridExample() {
    GridExample()
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Details.route, arguments = listOf(navArgument("itemId") { type = NavType.StringType })) {
            val itemId = it.arguments?.getString("itemId")
            DetailsScreen(navController, itemId ?: "Unknown")
        }
        navigation(startDestination = Screen.Profile.route, route = "settings") {
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Home Screen")
        Button(onClick = { navController.navigate(Screen.Details.createRoute("123")) }) {
            Text("Go to Details")
        }
        Button(onClick = { navController.navigate("settings") }) {
            Text("Go to Profile")
        }
    }
}

@Composable
fun DetailsScreen(navController: NavController, itemId: String) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Details Screen for item: $itemId")
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile Screen")
        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Home")
        }
    }
}

@Composable
fun LazyGridLayoutTest(){
    LazyColumn(modifier = Modifier
        .fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        // Header
        item {
            Text(
                text = "Header Section",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            GridHorizontalExample()
        }

        item {
            Text(
                text = "Header Section",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp).background(Color.Cyan).fillMaxWidth()
            )
        }

        items(50) { ColumnItem() }

        item {
            GridExample()
        }

    }
}

@Composable
fun ColumnItem(){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        ImageItemLayout()

        Column(modifier = Modifier.padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center) {
            TextLayout()
            TextDescLayout()
        }
    }
}

@Composable
fun ImageItemLayout(){
    Image(painter = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop, alignment = Alignment.CenterStart)
}

@Composable
fun TextLayout(){
    Text(text = "Hello World",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        color = Color.Black,
        textAlign = TextAlign.Start,
        style = LocalTextStyle.current,
        fontSize = 24.sp)
}

@Composable
fun TextDescLayout(){
    Text(text = "Hello World",
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.Red,
        textAlign = TextAlign.Start,
        style = LocalTextStyle.current,
        fontSize = 14.sp)
}