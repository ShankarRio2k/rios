package com.example.rios

import android.app.Application
import androidx.room.Room

class MyApplication : Application() {

    private var database: FriendsDatabase? = null

    fun getDatabase(): FriendsDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                applicationContext,
                FriendsDatabase::class.java,
                "friends_database"
            ).build()
        }
        return database!!
    }
}
