package com.example.rios.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceUtils {

    private const val SHARED_PREFERENCES_NAME = "user_details"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserDetails(context: Context, username: String, bio: String, profileImageUrl: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString("username", username)
        editor.putString("bio", bio)
        editor.putString("profileImageUrl", profileImageUrl)
        editor.apply()
    }

    fun getUserDetails(context: Context): Triple<String?, String?, String?> {
        val username = getSharedPreferences(context).getString("username", null)
        val bio = getSharedPreferences(context).getString("bio", null)
        val profileImageUrl = getSharedPreferences(context).getString("profileImageUrl", null)
        return Triple(username, bio, profileImageUrl)
    }
    fun getUsername(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", null)
    }
    fun getBio(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        return sharedPreferences.getString("bio", null)
    }

    fun getProfileImageUrl(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        return sharedPreferences.getString("profileImageUrl", null)
    }
    fun setUsername(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.apply()
    }

    fun clearUserDetails(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
