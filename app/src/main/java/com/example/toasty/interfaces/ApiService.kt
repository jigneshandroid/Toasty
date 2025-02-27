package com.example.toasty.interfaces

import com.example.toasty.models.ApiProductList
import com.example.toasty.models.TestItem
import com.example.toasty.models.TestUser
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("posts")
    suspend fun getTestItem(@Query("page") page: Int, @Query("per_page") perPage: Int = 10): List<TestItem>

    @GET("posts")
    suspend fun getTestUser(): List<TestUser>

    @POST("h6.php")
    suspend fun getProductList(@Body jsonObject: JsonObject?): ApiProductList?
}


