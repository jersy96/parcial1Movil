package com.example.myfirstapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

    @PrimaryKey
    public int userId;

    @ColumnInfo(name="name")
    public String name;

    @ColumnInfo(name="email")
    public String email;

    @ColumnInfo(name="password_hash")
    public String passwordHash;

    @ColumnInfo(name="externalId")
    public int externalId;
}
