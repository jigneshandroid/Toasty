package com.example.toasty.cleanarchitecturemvvm

class UserRepositoryImpl(private val userApi: UserApi) : UserRepository {
    override suspend fun getUser(userId: Int): User {
        return userApi.getUser(userId).toDomain()
    }
}