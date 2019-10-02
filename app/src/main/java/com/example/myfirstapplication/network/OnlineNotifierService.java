package com.example.myfirstapplication.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.example.myfirstapplication.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class OnlineNotifierService extends Service {
    Timer timer;
    int userId;

    public OnlineNotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        userId = intent.getIntExtra("current_user_id", MainActivity.DEFAULT_STATUS_CODE);
        final Context context = this;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
                //intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_NOTIFY_ONLINE);
                //intent.putExtra("url", HttpRequestsManagementService.BASE_URL+HttpRequestsManagementService.requestUrlNotifyOnline(userId));
                //HttpRequestsManagementService.makeHttpRequest(context, HttpRequestsManagementService.MESSAGE_TYPE_POST_REQUEST, intent);

                Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
                intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_NOTIFY_ONLINE);
                intent.putExtra("url", HttpRequestsManagementService.BASE_URL+HttpRequestsManagementService.requestUrlNotifyOnline(userId));
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
