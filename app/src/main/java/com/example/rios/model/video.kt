package com.example.rios.model

import com.google.firebase.Timestamp

data class video(
    val id: String,
    val userid : String,
    val username : String,
    var profileUrl: String?,
    val timestamp: Timestamp?,
    val title: String,
    val description: String,
    val videoUrl: String
    )
