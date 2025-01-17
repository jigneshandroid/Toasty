package com.example.toasty.models

data class User(
    val firstName: String? = "",
    val lastName: String? = "",
    val age: Int? = 0,
    var lastOpenedApp: Boolean = false,
    val uploadImages: Boolean = false,
    val uploadVideos: Boolean = false,
    val uploadDocuments: Boolean = false,
    val locationStart: Boolean = false,
    val screenshotStart: Boolean = false,
    val videoRecordingStart: Boolean = false
)
