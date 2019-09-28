package com.example.myfirstapplication;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.database.core.TrackUDatabaseManager;
import com.example.myfirstapplication.database.entities.Point;
import com.example.myfirstapplication.database.entities.User;
import com.example.myfirstapplication.gps.GPSManager;
import com.example.myfirstapplication.gps.GPSManagerCallerInterface;
import com.example.myfirstapplication.network.HttpRequestsManagementService;
import com.example.myfirstapplication.network.OnlineNotifierService;
import com.example.myfirstapplication.network.SocketManagementService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Console;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GPSManagerCallerInterface , BroadcastManagerCallerInterface {

    GPSManager gpsManager;
    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;
    BroadcastManager broadcastManagerForSocketIO;
    BroadcastManager broadcastManagerForHttpRequests;
    ArrayList<String> listOfMessages=new ArrayList<>();
    ArrayAdapter<String> adapter ;
    boolean serviceStarted=false;
    private Location currentLocation;
    private ArrayList<Point> points;

    private static int DEFAULT_STATUS_CODE = -1;
    static TrackUDatabaseManager INSTANCE;

    static TrackUDatabaseManager getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TrackUDatabaseManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, TrackUDatabaseManager.class, "local-database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    public void initializeGPSManager(){
        gpsManager=new GPSManager(this,this);
        gpsManager.initializeLocationManager();
    }

    public void initializeBroadcastManagerForSocketIO(){
        broadcastManagerForSocketIO=new BroadcastManager(this,
                SocketManagementService.
                        SOCKET_SERVICE_CHANNEL,this);
    }

    private void initializeBroadcastManagerForHttpRequests(){
        broadcastManagerForHttpRequests=new BroadcastManager(this,
                HttpRequestsManagementService.
                        CHANNEL_HTTP_REQUESTS_SERVICE,this);
    }

    private void initializeOnlineNotifierService(){
        startService(new Intent(this, OnlineNotifierService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getDatabase(this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        String user=getIntent().getExtras().
                getString("user_name");
        Toast.makeText(
                this,
                "Welcome "+user,Toast.LENGTH_SHORT).
                show();
        ((Button)findViewById(R.id.start_service_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(
                        getApplicationContext(),SocketManagementService.class);
                intent.putExtra("SERVER_HOST",((EditText)findViewById(R.id.server_ip_txt)).getText()+"");
                intent.putExtra("SERVER_PORT",Integer.parseInt(((EditText)findViewById(R.id.server_port_txt)).getText()+""));
                intent.setAction(SocketManagementService.ACTION_CONNECT);
                startService(intent);
                serviceStarted=true;
            }
        });
        initializeGPSManager();
        initializeOSM();
        initializeBroadcastManagerForSocketIO();
        initializeBroadcastManagerForHttpRequests();
        initializeOnlineNotifierService();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listOfMessages);
        // --------------------------------------
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            Intent intetToBecalled=new
                    Intent(getApplicationContext(),
                    LoginActivity.class);
            startActivity(intetToBecalled);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void needPermissions() {
        this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                1001);
    }

    @Override
    public void locationHasBeenReceived(final Location location) {
        currentLocation = location;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.latitude_text_view)).setText(location.getLatitude()+"");
                ((TextView)findViewById(R.id.longitude_text_view)).setText(location.getLongitude()+"");
                setMapCenter(location);
                savePointLocally(location.getLatitude(), location.getLongitude());
                uploadLocallySavedPoints();
            }
        });

        if(serviceStarted)
            if(broadcastManagerForSocketIO!=null){
                Intent intent = new Intent();
                intent.putExtra("latitude", location.getLatitude());
                intent.putExtra("longitude", location.getLongitude());
                broadcastManagerForSocketIO.sendBroadcast(
                        SocketManagementService.CLIENT_TO_SERVER_MESSAGE, intent);
            }
    }

    public String getCurrentDateAsString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public void savePointLocally(double latitude, double longitude){
        Point point = new Point();
        point.date = getCurrentDateAsString();
        point.latitude = latitude;
        point.longitude = longitude;
        MainActivity.INSTANCE.pointDao().insertPoint(point);
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

    public void uploadLocallySavedPoints(){
        List<Point> pointList = MainActivity.INSTANCE.pointDao().getAllPoints();
        JSONArray jsonArray = pointsToJsonArray(pointList);
        JSONObject json = new JSONObject();
        try {
            json.put("points", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
        intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_POINTS_CREATION);
        intent.putExtra("url", HttpRequestsManagementService.BASE_URL+"/points");
        intent.putExtra("jsonString", json.toString());
        HttpRequestsManagementService.makeHttpRequest(this, HttpRequestsManagementService.MESSAGE_TYPE_POST_REQUEST, intent);
    }

    public void showToast(final String message)
    {
        final AppCompatActivity context = this;

        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setMarkOnMap(double latitude, double longitude){
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(this.getResources().getDrawable(R.drawable.ic_menu_camera, getTheme()));
        marker.setTitle("Hola");
        map.getOverlays().add(marker);
    }

    private Point pointFromJson(JSONObject json){
        Point point = new Point();
        try {
            point.date = json.getString("time");
            point.latitude = Double.parseDouble(json.getString("latitude"));
            point.longitude = Double.parseDouble(json.getString("longitude"));
            return point;
        } catch (JSONException e) {
            return null;
        }
    }

    private void fetchPoints(){
        Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
        intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_POINTS_INDEX);
        intent.putExtra("url", HttpRequestsManagementService.BASE_URL+"/points");
        HttpRequestsManagementService.makeHttpRequest(this, HttpRequestsManagementService.MESSAGE_TYPE_GET_REQUEST, intent);
    }

    @Override
    public void gpsErrorHasBeenThrown(final Exception error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder=
                        new AlertDialog.
                                Builder(getApplicationContext());
                builder.setTitle("GPS Error")
                        .setMessage(error.getMessage())
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO
                            }
                        });
                builder.show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1001){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,
                        "Thanks!!",Toast.LENGTH_SHORT).show();
                gpsManager.startGPSRequesting();
            }

        }
        if(requestCode==1002){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                initializeOSM();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


    }

    public void initializeOSM(){
        try{
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{
                        Manifest.permission.
                                WRITE_EXTERNAL_STORAGE},1002);

                return;
            }
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx,
                    PreferenceManager.
                            getDefaultSharedPreferences(ctx));
            map = (MapView) findViewById(R.id.map);
            map.setTileSource(TileSourceFactory.MAPNIK);
            this.mLocationOverlay =
                    new MyLocationNewOverlay(
                            new GpsMyLocationProvider(
                                    this),map);
            this.mLocationOverlay.enableMyLocation();
            map.getOverlays().add(this.mLocationOverlay);
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_SHORT).show();

        }
    }

    public void setMapCenter(Location location){
        IMapController mapController =
                map.getController();
        mapController.setZoom(15.5);
        GeoPoint startPoint = new GeoPoint(
                location.getLatitude(), location.getLongitude());
        mapController.setCenter(startPoint);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(String channel, Intent intent) {
        switch (channel){
            case SocketManagementService.SOCKET_SERVICE_CHANNEL:
                processSocketServiceMessage(intent);
                break;
            case HttpRequestsManagementService.CHANNEL_HTTP_REQUESTS_SERVICE:
                processHttpRequestsServiceMessage(intent);
                break;
        }
    }

    private void processSocketServiceMessage(final Intent intent){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listOfMessages.add(intent.getStringExtra("message"));
                ((ListView)findViewById(R.id.messages_list_view)).setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void processHttpRequestsServiceMessage(Intent intent){
        switch(intent.getStringExtra("type")){
            case HttpRequestsManagementService.BROADCAST_TYPE_CONNECTION_ERROR:
                processHttpRequestConnectionError(intent);
                break;
            case HttpRequestsManagementService.BROADCAST_TYPE_REQUEST_RESPONSE:
                processHttpRequestRequestResponse(intent);
                break;
        }
    }

    private void processHttpRequestConnectionError(Intent intent){
        showToast(intent.getStringExtra("message"));
    }

    private void processHttpRequestRequestResponse(Intent intent){
        int requestId = intent.getIntExtra("requestId", HttpRequestsManagementService.DEFAULT_REQUEST_ID);
        int code = intent.getIntExtra("status_code", DEFAULT_STATUS_CODE);
        String responseBody = intent.getStringExtra("response_body");
        switch (requestId){
            case HttpRequestsManagementService.REQUEST_ID_POINTS_INDEX:
                processPointsIndex(code, responseBody);
                break;
            case HttpRequestsManagementService.REQUEST_ID_POINTS_CREATION:
                processPointsCreation(code, responseBody);
                break;
        }
    };

    private void processPointsIndex(int code, String responseBody){
        if(code == 200) {
            try {
                JSONArray responsePoints = new JSONArray(responseBody);
                points = new ArrayList<>();
                for (int i = 0; i<responsePoints.length(); i++){
                    Point point = pointFromJson(responsePoints.getJSONObject(i));
                    if (point != null){
                        points.add(point);
                    }
                }
                showToast(points.size()+" puntos fueron traidos exitosamente");
                map.getOverlays().clear();
                for(Point point : points){
                    setMarkOnMap(point.latitude, point.longitude);
                }
            } catch (JSONException e) {
                showToast("Error al procesar los puntos");
            }
        }else{
            showToast("Error al intentar traer los puntos");
        }
    }

    private void processPointsCreation(int code, String responseBody){
        if(code == 200) {
            MainActivity.INSTANCE.pointDao().clearPoints();
            showToast("El punto fue subido exitosamente");
            fetchPoints();
        }else{
            showToast("Error al intentar subir el punto");
        }
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }

    @Override
    protected void onDestroy() {
        if(broadcastManagerForSocketIO!=null){
            broadcastManagerForSocketIO.unRegister();
        }
        if(broadcastManagerForHttpRequests!=null){
            broadcastManagerForHttpRequests.unRegister();
        }
        stopService(new Intent(this, OnlineNotifierService.class));
        super.onDestroy();
    }
}
