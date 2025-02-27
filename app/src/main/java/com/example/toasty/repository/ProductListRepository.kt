package com.example.toasty.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.toasty.interfaces.ApiService
import com.example.toasty.models.Product
import com.example.toasty.remotemediator.ProductRemoteMediator
import com.example.toasty.room.ProductListDatabase
import kotlinx.coroutines.flow.Flow

class ProductListRepository(
    private val apiService: ApiService,
    private val productListDatabase: ProductListDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getProductList():Flow<PagingData<Product>>{
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            remoteMediator = ProductRemoteMediator(apiService, productListDatabase),
            pagingSourceFactory = {productListDatabase.getApiProductListDao().getProductList()}
        ).flow
    }

}