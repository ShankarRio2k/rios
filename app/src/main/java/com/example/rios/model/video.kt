package com.example.rios.model

data class video(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val user: User
)
