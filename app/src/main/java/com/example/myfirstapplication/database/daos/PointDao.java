package com.example.myfirstapplication.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myfirstapplication.database.entities.Point;

import java.util.List;

@Dao
public interface PointDao {
    @Query("select * from point")
    List<Point> getAllPoints();

    @Query("select * from point where pointId=:id")
    List<Point> getUserById(int id);

    @Insert
    void insertPoint(Point point);

    @Delete
    void deletePoint(Point point);

    @Query("DELETE FROM point")
    void clearPoints();

}
