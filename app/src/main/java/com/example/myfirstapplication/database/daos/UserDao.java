package com.example.myfirstapplication.database.daos;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myfirstapplication.database.entities.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("select * from user")
    List<User> getAllUsers();


    @Query("select * from user where userId=:id")
    List<User> getUserById(int id);

    @Query("select * from user where email=:email")
    List<User> getUserByEmail(String email);

    @Insert
    void insertUser(User user);

    @Delete
    void deleteUser(User user);

}
