package com.example.rios.views

import com.google.firebase.Timestamp

data class post(
    val postId: String,
    val userId: String,
    var username: String?,
    val caption: String,
    val imageUrl: String,
    val timestamp: Timestamp?,
    val likes: List<String> = emptyList(),
    var profileUrl: String?
) {
    constructor() : this("", "", "", "", "", timestamp = null, emptyList(), profileUrl = "")
}
