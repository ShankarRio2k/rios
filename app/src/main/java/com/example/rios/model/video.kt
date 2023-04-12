package com.example.rios.model

import com.google.firebase.Timestamp

data class video(
    val id: String = "",
    val userid: String = "",
    val username: String = "",
    var profileUrl: String? = null,
    val timestamp: Timestamp? = null,
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnail: String = ""
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", null, null, "", "", "", "")
}
