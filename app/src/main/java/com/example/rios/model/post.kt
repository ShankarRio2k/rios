package com.example.rios.model

import com.google.firebase.Timestamp

data class post(
    val postId: String,
    val userId: String,
    var username: String?,
    val caption: String,
    val imageUrl: String,
    val timestamp: Timestamp?,
    var likes: MutableList<Long> = mutableListOf(),
    var profileUrl: String?,
    var isLiked: Boolean = false
) {
    constructor() : this("", "", "", "", "", timestamp = null, mutableListOf(), profileUrl = "")
}
