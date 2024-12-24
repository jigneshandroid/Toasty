package com.jigs.chatgptdemo.network

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApi {
    @Headers("Authorization: Bearer YOUR_API_KEY")
    @POST("v1/chat/completions")
    suspend fun sendMessage(@Body body: ChatRequest): ChatResponse
}

data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
