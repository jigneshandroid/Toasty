package com.example.toasty.models

sealed class Video {

    data class Programming(val title: String, val duration: String) : Video()
    data class Cooking(val title: String, val duration: String) : Video()
    data class Travel(val title: String, val duration: String) : Video()
}