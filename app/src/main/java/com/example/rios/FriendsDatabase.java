package com.example.rios;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.rios.model.FreindsEntity;
import com.example.rios.model.FriendsDao;

@Database(entities = {FreindsEntity.class}, version = 1)
public abstract class FriendsDatabase extends RoomDatabase {

    public abstract FriendsDao friendsDao();

}
