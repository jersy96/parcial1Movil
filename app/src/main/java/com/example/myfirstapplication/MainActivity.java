package com.example.myfirstapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.database.core.DatabaseManager;
import com.example.myfirstapplication.database.entities.Point;
import com.example.myfirstapplication.database.entities.User;
import com.example.myfirstapplication.gps.GPSManager;
import com.example.myfirstapplication.gps.GPSManagerCallerInterface;
import com.example.myfirstapplication.network.HttpRequestsManagementService;
import com.example.myfirstapplication.network.OnlineNotifierService;
import com.example.myfirstapplication.network.SocketManagementService;

import android.preference.PreferenceManager;
import android.util.Log;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GPSManagerCallerInterface , BroadcastManagerCallerInterface, AdapterView.OnItemSelectedListener {

    private static String DEFAULT_USERS_SPINNER_VALUE = "Ninguno";
    GPSManager gpsManager;
    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;
    BroadcastManager broadcastManagerForSocketIO;
    BroadcastManager broadcastManagerForHttpRequests;
    ArrayList<String> listOfMessages=new ArrayList<>();
    ArrayAdapter<String> adapter ;
    boolean serviceStarted=false;
    boolean online;
    private Location currentLocation;
    TextView latitudeTextView;
    TextView longitudeTextView;
    TextView onlineTextView;
    TextView initialDateTextView;
    TextView finalDateTextView;
    String current_user_name;
    String initial_date;
    String final_date;
    private DatePickerDialog.OnDateSetListener mDateSetListenerInitialDate;
    private DatePickerDialog.OnDateSetListener mDateSetListenerFinalDate;
    private ArrayList<String> userNames;
    private ArrayList<User> users;
    private ArrayAdapter<String> usersAdapter;
    private Integer selectedUserId;

    private static int DEFAULT_STATUS_CODE = -1;
    static DatabaseManager INSTANCE;

    static DatabaseManager getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, DatabaseManager.class, "local-database")
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
        final Spinner spinner = (Spinner) findViewById(R.id.users_spinner);
        userNames = new ArrayList<>();
        users = new ArrayList<>();
        users.add(null);
        userNames.add(DEFAULT_USERS_SPINNER_VALUE);
        // Create an ArrayAdapter using the string array and a default spinner layout
        usersAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, userNames);
        // Specify the layout to use when the list of choices appears
        usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(usersAdapter);
        spinner.setOnItemSelectedListener(this);
        latitudeTextView = ((TextView)findViewById(R.id.latitude_text_view));
        longitudeTextView = ((TextView)findViewById(R.id.longitude_text_view));
        onlineTextView = ((TextView)findViewById(R.id.online_text_view));
        onlineTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_switch_on, 0, 0, 0);
        initialDateTextView = ((TextView)findViewById(R.id.initial_date_text_view));
        finalDateTextView = ((TextView)findViewById(R.id.final_date_text_view));
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        current_user_name = getIntent().getStringExtra("current_user_name");
        online = getIntent().getBooleanExtra("online", true);
        Toast.makeText(
                this,
                "Welcome "+ current_user_name,Toast.LENGTH_SHORT).
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


        ((Button)findViewById(R.id.clear_filter_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setSelection(0);
                initial_date = null;
                initialDateTextView.setText("Initial date:");
                final_date = null;
                finalDateTextView.setText("Final date:");
            }
        });

        ((Button)findViewById(R.id.search_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchPointsByUser();
            }
        });

        ((Button)findViewById(R.id.initial_date_button)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(
                                MainActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                mDateSetListenerInitialDate,
                                year,month,day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }

                });

        ((Button)findViewById(R.id.final_date_button)).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(
                                MainActivity.this,
                                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                mDateSetListenerFinalDate,
                                year,month,day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }
                });

        mDateSetListenerInitialDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("wtffffffff", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;
                initial_date = date;
                initialDateTextView.setText(date);
            }
        };

        mDateSetListenerFinalDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("wtffffffff", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = month + "/" + day + "/" + year;
                final_date = date;
                finalDateTextView.setText(date);
            }
        };

        initializeOSM();
        initializeGPSManager();
        initializeBroadcastManagerForSocketIO();
        initializeBroadcastManagerForHttpRequests();
        initializeOnlineNotifierService();
        fetchUsersList();
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
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (users.get(pos) == null){
            selectedUserId = null;
        }else{
            selectedUserId = users.get(pos).externalId;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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

        if (id == R.id.nav_chat) {
            Intent intetToBecalled=new
                    Intent(getApplicationContext(),
                    ChatActivity.class);
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
                processLocationUpdate();
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

    private void processLocationUpdate(){
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        latitudeTextView.setText(latitude+"");
        longitudeTextView.setText(longitude+"");
        setMapCenter(currentLocation);
        Point point = savePointLocally(latitude, longitude);
        setMarkOnMap(point, current_user_name, online);
        if (online){
            uploadLocallySavedPoints();
        }
    }

    public String getCurrentDateAsString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public Point savePointLocally(double latitude, double longitude){
        Point point = new Point();
        point.date = getCurrentDateAsString();
        point.latitude = latitude;
        point.longitude = longitude;
        MainActivity.INSTANCE.pointDao().insertPoint(point);
        return point;
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

    public void setMarkOnMap(Point point, String name, boolean isOnline){
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(point.latitude, point.longitude));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (isOnline){
            marker.setIcon(this.getResources().getDrawable(R.drawable.ic_location_on_green, getTheme()));
        }else{
            marker.setIcon(this.getResources().getDrawable(R.drawable.ic_location_on_red, getTheme()));
        }
        marker.setTitle(name+", "+point.date);
        map.getOverlays().add(marker);
    }

    private void fetchPoints(){
        Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
        intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_POINTS_INDEX);
        intent.putExtra("url", HttpRequestsManagementService.BASE_URL+"/points");
        HttpRequestsManagementService.makeHttpRequest(this, HttpRequestsManagementService.MESSAGE_TYPE_GET_REQUEST, intent);
    }

    private void fetchUsersList(){
        Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
        intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_USERS_INDEX);
        intent.putExtra("url", HttpRequestsManagementService.BASE_URL+"/users");
        HttpRequestsManagementService.makeHttpRequest(this, HttpRequestsManagementService.MESSAGE_TYPE_GET_REQUEST, intent);
    }

    private void fetchPointsByUser(){
        if (selectedUserId == null){
            showToast("Selecciona un usuario primero");
        }else{
            Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
            intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_POINTS_INDEX_BY_USER);
            intent.putExtra("url", HttpRequestsManagementService.BASE_URL+"/points_by_user?user_id="+selectedUserId);
            HttpRequestsManagementService.makeHttpRequest(this, HttpRequestsManagementService.MESSAGE_TYPE_GET_REQUEST, intent);
        }
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
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listOfMessages.add(intent.getStringExtra("message"));
                ((ListView)findViewById(R.id.messages_list_view)).setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });*/
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
        int requestId = intent.getIntExtra("requestId", HttpRequestsManagementService.DEFAULT_REQUEST_ID);
        if (requestId == HttpRequestsManagementService.REQUEST_ID_NOTIFY_ONLINE){
            onlineTextView.setText("Offline");
            onlineTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_switch_off, 0, 0, 0);
            this.online = false;
        }else{
            showToast(intent.getStringExtra("message"));
        }
    }

    private void processHttpRequestRequestResponse(Intent intent){
        int requestId = intent.getIntExtra("requestId", HttpRequestsManagementService.DEFAULT_REQUEST_ID);
        int code = intent.getIntExtra("status_code", DEFAULT_STATUS_CODE);
        String responseBody = intent.getStringExtra("response_body");
        switch (requestId){
            case HttpRequestsManagementService.REQUEST_ID_POINTS_INDEX:
            case HttpRequestsManagementService.REQUEST_ID_POINTS_INDEX_BY_USER:
                processPointsIndex(code, responseBody);
                break;
            case HttpRequestsManagementService.REQUEST_ID_POINTS_CREATION:
                processPointsCreation(code, responseBody);
                break;
            case HttpRequestsManagementService.REQUEST_ID_NOTIFY_ONLINE:
                processOnlineRequestResponse(code, responseBody);
                break;
            case HttpRequestsManagementService.REQUEST_ID_USERS_INDEX:
                processUsersIndex(code, responseBody);
                break;
        }
    };

    private void drawPointsOnMap(JSONArray responsePoints) throws JSONException {
        for (int i = 0; i<responsePoints.length(); i++){
            JSONObject json = responsePoints.getJSONObject(i);
            Point point = new Point();
            point.date = getCurrentDateAsString();
            point.latitude = json.getDouble("latitude");
            point.longitude = json.getDouble("longitude");
            setMarkOnMap(
                    point,
                    json.getString("user_name"),
                    json.getBoolean("user_online"));
        }
    }

    private void processPointsIndex(int code, String responseBody){
        if(code == 200) {
            try {
                map.getOverlays().clear();
                JSONArray responsePoints = new JSONArray(responseBody);
                drawPointsOnMap(responsePoints);
                showToast(responsePoints.length()+" puntos fueron traidos exitosamente");
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

    private void processOnlineRequestResponse(int code, String responseBody){
        boolean previousOnlineValue = online;
        this.online = code == 200;
        if(online && !previousOnlineValue){
            onlineTextView.setText("Online");
            onlineTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_switch_on, 0, 0, 0);
            uploadLocallySavedPoints();
        }
    }

    private void processUsersIndex(int code, String responseBody){
        if(code == 200) {
            try {
                userNames.clear();
                users.clear();
                users.add(null);
                userNames.add(DEFAULT_USERS_SPINNER_VALUE);
                JSONArray responseUsers = new JSONArray(responseBody);
                for (int i = 0; i<responseUsers.length(); i++){
                    JSONObject json = responseUsers.getJSONObject(i);
                    User user = new User();
                    user.externalId = json.getInt("id");
                    user.name = json.getString("name");
                    userNames.add(user.name);
                    users.add(user);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                showToast("Error al procesar los usuarios");
            }
        }else{
            showToast("Error al intentar traer los usuarios");
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
