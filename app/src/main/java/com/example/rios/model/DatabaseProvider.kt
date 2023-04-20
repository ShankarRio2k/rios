package com.example.rios.model

import android.content.Context
import androidx.room.Room
import com.example.rios.FriendsDatabase
import com.example.rios.views.Homeviewmodel

object DatabaseProvider {
    private var database: FriendsDatabase? = null

    fun getDatabase(context: Context): FriendsDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                FriendsDatabase::class.java,
                "friends_database"
            ).build()
        }
        return database!!
    }
}
