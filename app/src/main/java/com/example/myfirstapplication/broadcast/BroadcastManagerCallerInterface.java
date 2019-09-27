package com.example.myfirstapplication.broadcast;

import android.content.Intent;

public interface BroadcastManagerCallerInterface {

    void MessageReceivedThroughBroadcastManager(
            String channel, Intent intent);

    void ErrorAtBroadcastManager(Exception error);
}
