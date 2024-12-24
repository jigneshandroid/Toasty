package com.example.toasty.cleanarchitecturemvvm

import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): UserEntity
}