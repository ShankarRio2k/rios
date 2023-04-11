package com.example.rios.utils

import android.content.Context
import android.content.SharedPreferences
class SharedPrefernceHelper(private val context: Context) {

    private val SHARED_PREFS_NAME = "user_details"
    private val KEY_USERNAME = "username"
    private val KEY_BIO = "bio"
    private val KEY_PROFILE_IMAGE_URL = "profileImageUrl"

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserDetails(username: String, bio: String, profileImageUrl: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_BIO, bio)
        editor.putString(KEY_PROFILE_IMAGE_URL, profileImageUrl)
        editor.apply()
    }

    fun getUserDetails(): Triple<String?, String?, String?> {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val bio = sharedPreferences.getString(KEY_BIO, null)
        val profileImageUrl = sharedPreferences.getString(KEY_PROFILE_IMAGE_URL, null)
        return Triple(username, bio, profileImageUrl)
    }

    fun clearUserDetails() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    //Original Functions
    fun setString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun setInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}
