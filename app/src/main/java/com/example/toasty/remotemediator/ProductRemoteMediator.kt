package com.example.toasty.remotemediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.toasty.interfaces.ApiService
import com.example.toasty.models.Product
import com.example.toasty.models.ProductListKey
import com.example.toasty.room.ProductListDatabase
import com.google.gson.Gson
import com.google.gson.JsonObject

@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    private val apiService: ApiService,
    private val productListDatabase: ProductListDatabase
) : RemoteMediator<Int, Product>() {
    private val productListDao = productListDatabase.getApiProductListDao()
    private val productListKeyDao = productListDatabase.getProductListKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Product>
    ): MediatorResult {
        return try {
            Log.d("ProductListZood","loadType $loadType")
            val currentKey = when (loadType) {
                LoadType.REFRESH -> {
                    val res = getRemoteKeyOfCurrentPosition(state)
                    res?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val pos = getRemoteKeyOfFirstPosition(state)
                    val prevKey = pos?.prevKey ?: return MediatorResult.Success(pos != null)
                    prevKey
                }
                LoadType.APPEND -> {
                    val pos = getRemoteKeyOfLastPosition(state)
                    val nextKey = pos?.nextKey ?: return MediatorResult.Success(pos != null)
                    nextKey
                }
            }

            Log.d("ProductListZood","currentKey $currentKey")
            val json = Gson().fromJson("{\"productTagId\":13,\"page\":$currentKey,\"rowsPerPage\":30,\"dontshow\":true,\"sandbox\":true,\"isMpin\":true,\"native_app\":true,\"lang\":\"en\",\"ver\":\"6.0.2\",\"deviceType\":\"android\",\"deviceVersion\":\"13\",\"deviceInfo\":\"samsung(Device:SM-M127G_OS:13)\",\"marketCode\":\"UZ\",\"OM_DEVICE_UUID\":\"6fcdb5eaa1100ff9\",\"customerId\":\"773575\",\"api\":\"product.productListV2\",\"b2c_url\":\"\",\"showTranslationKey\":false}", JsonObject::class.java)
            Log.d("ProductListZood","json $json")
            val response = apiService.getProductList(json)
            Log.d("ProductListZood","response $response")
            val endOfPagination =
                response?.ErrorCode.equals("9999") && response?.Data?.Pagination?.totalPage == currentKey

            val prevKey = if (currentKey == 1) null else currentKey - 1
            val nextKey = if (endOfPagination) null else currentKey + 1
            Log.d("ProductListZood","prevKey $prevKey - nextKey $nextKey")
            productListDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    productListDao.deleteAllProduct()
                    productListKeyDao.deleteAll()
                }
                response?.Data?.marketList?.let {
                    productListDao.insertProductList(it)
                }
                val keyList = response?.Data?.marketList?.map {
                    ProductListKey(
                        id = it.productId,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                if (keyList != null) {
                    productListKeyDao.insertKey(keyList)
                }
            }
            MediatorResult.Success(endOfPagination)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyOfCurrentPosition(state: PagingState<Int, Product>): ProductListKey? {
        Log.d("ProductListZood","state.anchorPosition ${state.anchorPosition}")
        return state.anchorPosition?.let { position ->
            Log.d("ProductListZood","closestItemToPosition $position - ${state.closestItemToPosition(position)?.productId}")
            state.closestItemToPosition(position)?.productId?.let {
                Log.d("ProductListZood","productListKeyDao.getProductList $it - ${productListKeyDao.getProductList(id = it)}")
                productListKeyDao.getProductList(id = it)
            }
        }
    }

    private suspend fun getRemoteKeyOfFirstPosition(state: PagingState<Int, Product>): ProductListKey? {
        Log.d("ProductListZood","getRemoteKeyOfFirstPosition ${state.pages} - ${state.pages?.last()?.data}")
        return state.pages?.firstOrNull { it.data?.isNotEmpty() == true }?.data?.firstOrNull().let {
            Log.d("ProductListZood","getRemoteKeyOfFirstPosition productId ${it?.productId}")
            it?.productId?.let { it1 -> productListKeyDao.getProductList(id = it1) }
        }
    }

    private suspend fun getRemoteKeyOfLastPosition(state: PagingState<Int, Product>): ProductListKey? {
        Log.d("ProductListZood","getRemoteKeyOfLastPosition ${state.pages} - ${state.pages?.last()?.data}")
        return state.pages?.lastOrNull { it.data?.isNotEmpty() == true }?.data?.lastOrNull().let {
            Log.d("ProductListZood","getRemoteKeyOfLastPosition productId ${it?.productId}")
            it?.productId?.let { it1 -> productListKeyDao.getProductList(id = it1) }
        }
    }
}