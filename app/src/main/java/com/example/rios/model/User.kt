package com.example.rios.model

import java.net.URL


data class User(
    val id: String,
    val name: String,
    val bio: String,
    val imageUrl: String?,
    var isFollowed: Boolean,
    var lastMessageTime: String? = null

) {
    constructor() : this("", "", "","",false, null)

}
