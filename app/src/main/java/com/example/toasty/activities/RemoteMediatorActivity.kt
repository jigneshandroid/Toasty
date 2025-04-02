package com.example.toasty.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.toasty.models.Product
import com.example.toasty.repository.ProductListRepository
import com.example.toasty.room.ProductListDatabase
import com.example.toasty.viewmodel.ProductViewModel
import com.jigs.chatgptdemo.network.ApiClient

class RemoteMediatorActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //val repository = TestUserRepository(ApiClient.chatApi, TestUserDatabase.getInstance(this@RemoteMediatorActivity))
        val repository = ProductListRepository(ApiClient.retrofitApi, ProductListDatabase.getInstance(this@RemoteMediatorActivity))

        setContent {
            ItemScreen(repository)
        }
    }

    @Composable
    fun ItemScreen(repository: ProductListRepository) {

        Log.d("APIRESPONSE", "ItemScreen call")
        //val viewModel: UserViewModel = viewModel<UserViewModel>()
        //val viewModel = UserViewModel(repository)
        val viewModel = ProductViewModel(repository)
        val items = viewModel.productList.collectAsLazyPagingItems()
        LazyColumn {
            items(items.itemCount) { index ->
                val item = items[index]
                if (item != null) {
                    ItemRow(item = item)
                }
            }

            items.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator()
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        item {
                            Text(text = "Error loading more items")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ItemRow(item: Product?) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item?.name?.let { Text(text = it, style = MaterialTheme.typography.titleMedium) }
            item?.categoryName?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
        }
    }

}