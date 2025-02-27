package com.example.toasty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.toasty.models.Product
import com.example.toasty.repository.ProductListRepository
import kotlinx.coroutines.flow.Flow

class ProductViewModel(repository: ProductListRepository): ViewModel() {

    val productList: Flow<PagingData<Product>> = repository.getProductList().cachedIn(viewModelScope)

}