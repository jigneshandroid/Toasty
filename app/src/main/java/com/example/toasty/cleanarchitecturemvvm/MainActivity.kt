package com.example.toasty.cleanarchitecturemvvm

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.toasty.R

class MainActivity  : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launchWhenStarted {
            viewModel.user.collect { user ->
                user?.let {
                    // Update UI with user data
                    println("User Name: ${it.name}")
                }
            }
        }

        viewModel.fetchUser(1) // Fetch user with ID 1
    }
}