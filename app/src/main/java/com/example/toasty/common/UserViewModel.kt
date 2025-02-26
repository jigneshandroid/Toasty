package com.example.toasty.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.toasty.MyApplication
import com.example.toasty.RemoteMediatorActivity
import com.example.toasty.interfaces.ApiService
import com.example.toasty.interfaces.TestUserRepository
import com.example.toasty.models.TestItem
import com.example.toasty.models.TestUser
import com.example.toasty.room.TestItemDatabase
import com.jigs.chatgptdemo.network.ApiClient
import kotlinx.coroutines.flow.Flow

@SuppressLint("StaticFieldLeak")
class UserViewModel(repository: TestUserRepository) : ViewModel() {


    val userFlow: Flow<PagingData<TestUser>> = repository.getTestUser().cachedIn(viewModelScope)

    //val items = repository.getUsers().cachedIn(viewModelScope)
}
