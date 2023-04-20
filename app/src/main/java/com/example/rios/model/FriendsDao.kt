package com.example.rios.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FriendsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFriends(friends: List<FreindsEntity>)

    @Query("SELECT * FROM friends")
    fun getFriends(): List<FreindsEntity>
}
