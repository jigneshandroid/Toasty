package com.example.toasty.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class ComposeTestActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    @Composable
    fun App(){
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "Home") {
            composable(route = "Home"){
                HomeScreen { navController.navigate("Login/${it}") }
            }
            composable(route = "Login/{email}", arguments = listOf(
                navArgument("email"){
                    type = NavType.StringType
                }
            )){
                val email = it.arguments?.getString("email")?:"default"
                LoginScreen(navController, email)
            }
            composable(route = "Account"){
                AccountScreen(navController)
            }
        }
    }

    @Composable
    fun HomeScreen(onClick:(email:String)->Unit){
        Text(text = "Home Screen",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.clickable {
                onClick("helloWorld@gmail.com")
            })
    }

    @Composable
    fun LoginScreen(navController: NavController, email: String){
        Text(text = "Login Screen $email",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.clickable {
                navController.navigate("Account")
            })
    }

    @Composable
    fun AccountScreen(navController: NavController){
        Text(text = "Account Screen",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.clickable {
                navController.popBackStack()
            })
    }
}