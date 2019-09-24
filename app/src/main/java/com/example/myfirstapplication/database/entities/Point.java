package com.example.myfirstapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Point {
    @PrimaryKey(autoGenerate = true)
    public int pointId;

    @ColumnInfo(name="Latitude")
    public double Latitude;

    @ColumnInfo(name="Longitude")
    public double Longitude;

    @ColumnInfo(name="Date")
    public String Date;

}
