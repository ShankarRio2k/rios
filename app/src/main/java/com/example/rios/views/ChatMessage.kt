package com.example.rios.views

import com.google.type.Date

data class ChatMessage(
    var senderId: String = "",
    var senderName: String = "",
    var message: String = "",
    var createdAt: Date? = null,
    var id: String? = null
)
