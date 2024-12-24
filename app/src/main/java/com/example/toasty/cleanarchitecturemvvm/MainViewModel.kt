package com.example.toasty.cleanarchitecturemvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val getUserUseCase: GetUserUseCase) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    fun fetchUser(userId: Int) {
        viewModelScope.launch {
            val result = getUserUseCase(userId)
            _user.value = result
        }
    }
}