package com.example.rios.views

import com.google.firebase.Timestamp
import com.google.type.Date
import com.google.type.DateTime

data class ChatMessage(
    var senderId: String = "",
    var senderName: String = "",
    var message: String = "",
    var createdAt: Timestamp? = null,
    var id: String? = null
)
