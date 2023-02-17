package com.example.rios.views

import java.net.URL


data class User(
    val name: String,
    val bio: String,
    val imageUrl: String?
) {
    constructor() : this("", "", null)
}
