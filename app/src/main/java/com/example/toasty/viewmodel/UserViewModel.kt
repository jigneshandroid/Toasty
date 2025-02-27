package com.example.toasty.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.toasty.repository.TestUserRepository
import com.example.toasty.models.TestUser
import kotlinx.coroutines.flow.Flow

@SuppressLint("StaticFieldLeak")
class UserViewModel(repository: TestUserRepository) : ViewModel() {


    val userFlow: Flow<PagingData<TestUser>> = repository.getTestUser().cachedIn(viewModelScope)

    //val items = repository.getUsers().cachedIn(viewModelScope)
}
