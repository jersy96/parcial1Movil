package com.example.myfirstapplication.network;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequestsManagementService extends IntentService {
    public static final int DEFAULT_REQUEST_ID = 0;
    public static final int REQUEST_ID_POINTS_INDEX = 1;
    public static final int REQUEST_ID_POINTS_CREATION = 2;
    public static final int REQUEST_ID_NOTIFY_ONLINE = 3;
    public static final int REQUEST_ID_USERS_INDEX = 4;
    public static final int REQUEST_ID_POINTS_INDEX_BY_USER = 5;
    public static final int REQUEST_ID_LOGIN = 6;
    public static final String ACTION_INIT_HTTP_REQUEST_MANAGER = "com.example.myfirstapplication.network.action.ACTION_INIT_HTTP_REQUEST_MANAGER";
    public static final String CHANNEL_HTTP_REQUESTS_SERVICE = "com.example.myfirstapplication.HTTP_REQUESTS_SERVICE_CHANNEL";
    public static final String MESSAGE_TYPE_POST_REQUEST = "POST_REQUEST";
    public static final String MESSAGE_TYPE_GET_REQUEST = "GET_REQUEST";
    public static final String BROADCAST_TYPE_CONNECTION_ERROR = "CONNECTION_ERROR";
    public static final String BROADCAST_TYPE_REQUEST_RESPONSE = "REQUEST_RESPONSE";
    public static final String BASE_URL = "http://192.168.0.4:3000";
    // public static final String BASE_URL = "http://192.168.43.204:3000";
    // public static final String BASE_URL = "http://192.168.1.7:3000";

    String url;
    private int requestId;

    public static Intent createIntentForHttpRequest(Context context){
        return new Intent(context, HttpRequestsManagementService.class);
    }

    public static void makeHttpRequest(Context context, String type, Intent intent){
        intent.setAction(HttpRequestsManagementService.ACTION_INIT_HTTP_REQUEST_MANAGER);
        intent.putExtra("type", type);
        context.startService(intent);
    }

    public HttpRequestsManagementService() {
        super("HttpRequestsManagementService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_HTTP_REQUEST_MANAGER.equals(action)) {
                try {
                    String type = intent.getStringExtra("type");
                    url = intent.getStringExtra("url");
                    requestId = intent.getIntExtra("requestId", DEFAULT_REQUEST_ID);

                    switch(type) {
                        case MESSAGE_TYPE_POST_REQUEST:
                            makePostRequest(intent.getStringExtra("jsonString"));
                            break;
                        case MESSAGE_TYPE_GET_REQUEST:
                            makeGetRequest();
                            break;
                    }
                }catch (Exception error){

                }
            }
        }
    }

    private void makePostRequest(String jsonString){
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonString);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(getCallback());
    }

    private void makeGetRequest(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(getCallback());
    }

    private Callback getCallback(){
        return new Callback() {
            private Intent getIntent(){
                Intent intent = new Intent();
                intent.putExtra("requestId", requestId);
                return  intent;
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Intent intent = getIntent();
                intent.putExtra("message", "Error de conexion");
                deliverBroadcast(BROADCAST_TYPE_CONNECTION_ERROR, intent);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Intent intent = getIntent();
                intent.putExtra("status_code", response.code());
                intent.putExtra("response_body", response.body().string());
                deliverBroadcast(BROADCAST_TYPE_REQUEST_RESPONSE, intent);
            }
        };
    }

    private void deliverBroadcast(String type, Intent intent){
        intent.putExtra("type", type);
        intent.setAction(CHANNEL_HTTP_REQUESTS_SERVICE);
        sendBroadcast(intent);
    }
}
