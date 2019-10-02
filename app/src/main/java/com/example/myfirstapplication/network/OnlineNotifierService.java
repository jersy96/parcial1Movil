package com.example.myfirstapplication.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class OnlineNotifierService extends Service {
    Timer timer;

    public OnlineNotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        final Context context = this;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
                intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_NOTIFY_ONLINE);
                intent.putExtra("url", HttpRequestsManagementService.BASE_URL+HttpRequestsManagementService.REQUEST_URL_NOTIFY_ONLINE);
                HttpRequestsManagementService.makeHttpRequest(context, HttpRequestsManagementService.MESSAGE_TYPE_GET_REQUEST, intent);
            }
        }, 5, 5000);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
