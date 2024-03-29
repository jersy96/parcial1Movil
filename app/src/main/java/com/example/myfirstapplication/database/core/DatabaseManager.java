package com.example.myfirstapplication.database.core;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myfirstapplication.database.daos.PointDao;
import com.example.myfirstapplication.database.daos.UserDao;
import com.example.myfirstapplication.database.entities.Point;
import com.example.myfirstapplication.database.entities.User;

@Database(entities = {User.class, Point.class},version = 3, exportSchema = false)
public abstract class DatabaseManager extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PointDao pointDao();
}