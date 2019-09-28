package com.example.myfirstapplication.database.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import androidx.room.Room;

import com.example.myfirstapplication.MainActivity;
import com.example.myfirstapplication.database.core.TrackUDatabaseManager;
import com.example.myfirstapplication.database.entities.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LocalDatabaseManagerService extends IntentService {
    // Actions
    private static final String INSERT_POINT = "com.example.myfirstapplication.database.services.action.INSERT_POINT";
    private static final String GET_POINTS = "com.example.myfirstapplication.database.services.action.GET_POINTS";
    private static final String DELETE_POINTS = "com.example.myfirstapplication.database.services.action.DELETE_POINTS";
    private static final String CREATE_USER = "com.example.myfirstapplication.database.services.action.CREATE_USER";
    private static final String GET_USER_BY_EMAIL = "com.example.myfirstapplication.database.services.action.GET_USER_BY_EMAIL";

    // Params
    private static final double PARAM_DEFAULT_DOUBLE = 0;
    private static final String PARAM_INSERT_POINT_LATITUDE = "com.example.myfirstapplication.database.services.extra.PARAM_INSERT_POINT_LATITUDE";
    private static final String PARAM_INSERT_POINT_LONGITUDE = "com.example.myfirstapplication.database.services.extra.PARAM_INSERT_POINT_LONGITUDE";

    TrackUDatabaseManager instance;

    public LocalDatabaseManagerService() {
        super("LocalDatabaseManagerService");
    }

    public static void startActionInsertPoint(Context context, double latitude, double longitude) {
        Intent intent = new Intent(context, LocalDatabaseManagerService.class);
        intent.setAction(INSERT_POINT);
        intent.putExtra(PARAM_INSERT_POINT_LATITUDE, latitude);
        intent.putExtra(PARAM_INSERT_POINT_LONGITUDE, longitude);
        context.startService(intent);
    }

    public static void startActionGetPoints(Context context) {
        Intent intent = new Intent(context, LocalDatabaseManagerService.class);
        intent.setAction(GET_POINTS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            setInstance(this);
            final String action = intent.getAction();
            switch (action){
                case INSERT_POINT:
                    final double latitude = intent.getDoubleExtra(PARAM_INSERT_POINT_LATITUDE, PARAM_DEFAULT_DOUBLE);
                    final double longitude = intent.getDoubleExtra(PARAM_INSERT_POINT_LONGITUDE, PARAM_DEFAULT_DOUBLE);
                    handleActionInsertPoint(latitude, longitude);
                    break;
                case GET_POINTS:
                    handleActionGetPoints();
                    break;
            }
        }
    }

    // Handle action methods
    private void handleActionInsertPoint(double latitude, double longitude) {
        Point point = new Point();
        point.date = getCurrentDateAsString();
        point.latitude = latitude;
        point.longitude = longitude;
        instance.pointDao().insertPoint(point);
    }

    private void handleActionGetPoints(){
    }

    // Methods
    private void setInstance(final Context context) {
        if (instance == null) {
            synchronized (TrackUDatabaseManager.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context, TrackUDatabaseManager.class, "local-database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
    }

    public String getCurrentDateAsString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public JSONObject pointToJson(Point point){
        try {
            JSONObject json = new JSONObject();
            json.put("user_id", 1);
            json.put("latitude",point.latitude);
            json.put("longitude",point.longitude);
            json.put("time", point.date);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }

    public JSONArray pointsToJsonArray(List<Point> points){
        JSONArray jsonArray = new JSONArray();
        for (Point point : points){
            jsonArray.put(pointToJson(point));
        }
        return jsonArray;
    }
}
