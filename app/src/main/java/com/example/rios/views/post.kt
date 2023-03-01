package com.example.rios.views

import com.google.protobuf.Timestamp

data class post(
    val postId: String,
    val userId: String,
    val username: String,
    val caption: String,
    val imageUrl: String,
    val timestamp: Timestamp?
) {
    constructor() : this("", "", "", "","", timestamp = null)

}
