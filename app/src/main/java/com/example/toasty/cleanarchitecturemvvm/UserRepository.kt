package com.example.toasty.cleanarchitecturemvvm

interface UserRepository {
    suspend fun getUser(userId: Int): User
}