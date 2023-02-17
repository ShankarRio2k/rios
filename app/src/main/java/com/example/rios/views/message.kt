package com.example.rios.views

data class Message(
    val fromId: String = "",
    val toId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis()

)