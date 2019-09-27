package com.example.myfirstapplication.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadcastManager extends BroadcastReceiver {

    private Context context;
    private String channel;
    private BroadcastManagerCallerInterface caller;

    public BroadcastManager(Context context,
                            String channel,
                            BroadcastManagerCallerInterface caller) {
        this.context = context;
        this.channel = channel;
        this.caller = caller;
        initializeBroadcast();
    }

    public void initializeBroadcast(){
        try{
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(channel);
            context.registerReceiver(this,intentFilter);
        }catch (Exception error){
            caller.ErrorAtBroadcastManager(error);
        }
    }

    public void unRegister(){
        try{
            context.unregisterReceiver(this);
        }catch (Exception error){
            caller.ErrorAtBroadcastManager(error);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        caller.MessageReceivedThroughBroadcastManager(
                        this.channel, intent);
    }

    public void sendBroadcast(String type, Intent intent){
        try{
            intent.setAction(channel);
            intent.putExtra("type", type);
            context.sendBroadcast(intent);
        }catch (Exception error){
            caller.ErrorAtBroadcastManager(error);
        }
    }
}
