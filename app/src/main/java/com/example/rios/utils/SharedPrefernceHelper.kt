package com.example.rios.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefernceHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)

    fun saveUserDetails(username: String, bio: String, profileImageUrl: String) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("bio", bio)
        editor.putString("profileImageUrl", profileImageUrl)
        editor.apply()
    }

    fun getUserDetails(): Triple<String?, String?, String?> {
        val username = sharedPreferences.getString("username", null)
        val bio = sharedPreferences.getString("bio", null)
        val profileImageUrl = sharedPreferences.getString("profileImageUrl", null)
        return Triple(username, bio, profileImageUrl)
    }

    fun clearUserDetails() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
