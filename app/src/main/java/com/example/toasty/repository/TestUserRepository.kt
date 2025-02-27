package com.example.toasty.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.toasty.interfaces.ApiService
import com.example.toasty.models.TestUser
import com.example.toasty.room.TestUserDatabase
import com.example.toasty.remotemediator.TestUserRemoteMediator
import kotlinx.coroutines.flow.Flow

class TestUserRepository(
    private val apiService: ApiService,
    private val testUserDatabase: TestUserDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getTestUser() : Flow<PagingData<TestUser>> {
        Log.d("APIRESPONSE", "getTestUser")
        return Pager(
            config= PagingConfig(pageSize = 10, enablePlaceholders = false),
            remoteMediator = TestUserRemoteMediator(apiService, testUserDatabase),
            pagingSourceFactory = { testUserDatabase.getTestUserDao().getTestUser()}
        ).flow
    }
}