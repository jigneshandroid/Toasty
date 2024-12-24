package com.example.toasty.cleanarchitecturemvvm

class GetUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: Int): User {
        return userRepository.getUser(userId)
    }
}