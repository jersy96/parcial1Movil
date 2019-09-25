package com.example.myfirstapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Point {
    @PrimaryKey(autoGenerate = true)
    public int pointId;

    @ColumnInfo(name="userId")
    public int userId;

    @ColumnInfo(name="latitude")
    public double latitude;

    @ColumnInfo(name="Longitude")
    public double longitude;

    @ColumnInfo(name="Date")
    public String date;
}
