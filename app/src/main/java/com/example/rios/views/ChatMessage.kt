package com.example.rios.views

import com.example.rios.utils.FirebaseUtils.firebaseAuth

data class ChatMessage(
    var message: String = "",
    var room: String = "",
    var currenttime: String = "",
    var senderid: String = firebaseAuth.currentUser!!.uid
)