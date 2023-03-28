package com.example.rios.views

import com.google.firebase.Timestamp
data class post(
    val postId: String,
    val userId: String,
    var username: String?,
    val caption: String,
    val imageUrl: String,
    val timestamp: Timestamp?,
    val likes: List<Int> = emptyList(),
    var profileUrl: String?,
    var isLiked: Boolean = false // Add isLiked property with default value false
) {
    constructor() : this("", "", "", "", "", timestamp = null, emptyList(), profileUrl = "")
}
