package com.example.toasty.models

import androidx.room.PrimaryKey

data class ApiProductList(
    val id:Int,
    val ErrorCode:String,
    val Message:String,
    val Data:DataObj
)

data class DataObj(
    val marketList:List<Product>,
    val Pagination:PaginationObj
)

data class PaginationObj(
    val page:Int,
    val rowsPerPage:Int,
    val totalCount:Int,
    val totalPage:Int
)
