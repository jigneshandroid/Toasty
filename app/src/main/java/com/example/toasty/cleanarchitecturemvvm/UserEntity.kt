package com.example.toasty.cleanarchitecturemvvm

data class UserEntity(val id: Int, val name: String, val email: String)

fun UserEntity.toDomain(): com.example.toasty.cleanarchitecturemvvm.User {
    return com.example.toasty.cleanarchitecturemvvm.User(id, name, email)
}