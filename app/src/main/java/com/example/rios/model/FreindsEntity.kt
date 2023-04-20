package com.example.rios.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")

data class FreindsEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bio: String,
    val imageUrl: String?
)
