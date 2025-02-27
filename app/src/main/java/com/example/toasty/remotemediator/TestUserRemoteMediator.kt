package com.example.toasty.remotemediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.toasty.interfaces.ApiService
import com.example.toasty.models.TestUser
import com.example.toasty.models.TestUserKey
import com.example.toasty.room.TestUserDatabase


@OptIn(ExperimentalPagingApi::class)
class TestUserRemoteMediator(
    private val apiService: ApiService,
    private val testUserDatabase: TestUserDatabase
): RemoteMediator<Int, TestUser>() {
    private val testUserDao = testUserDatabase.getTestUserDao()
    private val testUserKeyDao = testUserDatabase.getTestUserKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TestUser>
    ): MediatorResult {
        return try{
            Log.d("APIRESPONSE", "load Remote mediator")
            val currentPage = when(loadType){
                LoadType.REFRESH->{
                    val res = getRemoteKeyOfCurrentPosition(state)
                    res?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND->{
                    val res = getRemoteKeyOfFirstPosition(state)
                    val prevKey = res?.prevKey ?: return MediatorResult.Success( res != null)
                    prevKey
                }
                LoadType.APPEND->{
                    val res = getRemotKeyOfLastPosition(state)
                    val nextKey = res?.nextKey ?: return MediatorResult.Success( res != null)
                    nextKey
                }
            }
            Log.d("APIRESPONSE", "load Remote mediator $currentPage")
            val response = apiService.getTestUser()
            Log.d("APIRESPONSE",response.toString())
            val endOfPaginationReached = response.isEmpty()
            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            testUserDatabase.withTransaction {
                if(loadType==LoadType.REFRESH){
                    testUserDao.deleteAll()
                    testUserKeyDao.deleteAll()
                }
                testUserDao.insertAll(response)
                val keyList = response.map {
                    TestUserKey(it.id,
                        prevPage,
                        nextPage
                    )
                 }
                testUserKeyDao.insert(keyList)
            }
            MediatorResult.Success(endOfPaginationReached)
        }catch(e:Exception){
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyOfCurrentPosition(state: PagingState<Int, TestUser>): TestUserKey?{
        return state.anchorPosition?.let { position->
            state.closestItemToPosition(position)?.id?.let { id->
                testUserKeyDao.getTestUserKey(id = id)
            }
        }
    }

    private suspend fun getRemoteKeyOfFirstPosition(state: PagingState<Int, TestUser>): TestUserKey?{
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { testUser ->
            testUserKeyDao.getTestUserKey(id = testUser.id)
        }
    }

    private suspend fun getRemotKeyOfLastPosition(state: PagingState<Int, TestUser>):TestUserKey?{
        return state?.pages?.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { testUser->
            testUserKeyDao.getTestUserKey(id = testUser.id)
        }
    }
}