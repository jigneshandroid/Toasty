package com.example.toasty.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.toasty.interfaces.ApiService
import com.example.toasty.models.TestItem
import com.example.toasty.models.TestItemKey
import com.example.toasty.room.TestItemDatabase

@OptIn(ExperimentalPagingApi::class)
class TestItemRemoteMediator(
    private val apiService: ApiService,
    private val testItemDatabase: TestItemDatabase
) : RemoteMediator<Int, TestItem>() {

    private val testItemDao = testItemDatabase.getTestItemDao()
    private val testItemKeyDao = testItemDatabase.getTestItemKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TestItem>
    ): MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = getRemoteKeyCloseToCurrentItem(state)
                    remoteKey?.next?.minus(1) ?: 1
                }

                LoadType.PREPEND -> {
                    val prevKeyRemote = getRemoteKeyForFirstItem(state)
                    val prevPage =
                        prevKeyRemote?.prev ?: return MediatorResult.Success(prevKeyRemote != null)
                    prevPage
                }

                LoadType.APPEND -> {
                    val nextKeyRemote = getRemoteKeyForLastItem(state)
                    val nextPage =
                        nextKeyRemote?.next ?: return MediatorResult.Success(nextKeyRemote != null)
                    nextPage
                }
            }
            val response = apiService.getTestItem(1)
            val endOfPaginationReached = response.size == currentPage

            val prevPage = if (currentPage == 1) null else currentPage - 1
            val nextPage = if (endOfPaginationReached) null else currentPage + 1

            testItemDatabase.withTransaction {
                testItemDao.insert(response)
                val listKeys = response.map {
                    TestItemKey(
                        id = it.id,
                        prev = prevPage,
                        next = nextPage
                    )
                }
                testItemKeyDao.insertKey(listKeys)
            }
            MediatorResult.Success(endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyCloseToCurrentItem(state: PagingState<Int, TestItem>): TestItemKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                testItemKeyDao.getItemKey(id = id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, TestItem>): TestItemKey? {
        return state.pages.firstOrNull { it.data.isEmpty() }?.data?.firstOrNull()?.let { testItem ->
            testItemKeyDao.getItemKey(id = testItem.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TestItem>): TestItemKey? {
        return state.pages.lastOrNull { it.data.isEmpty() }?.data?.lastOrNull()?.let { testItem ->
            testItemKeyDao.getItemKey(id = testItem.id)
        }
    }
}