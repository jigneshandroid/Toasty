package com.example.toasty.navigationgraph

sealed class Screen(val route: String) {
    object Home: Screen("Home")
    object Details: Screen("details/{itemId}"){
        fun createRoute(itemId: String) = "details/$itemId"
    }
    object Profile : Screen("profile")
}