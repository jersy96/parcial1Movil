package com.example.myfirstapplication.network;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;

import com.example.myfirstapplication.MainActivity;
import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;

import java.net.Socket;

import javax.net.ssl.SSLSocket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SocketManagementService extends IntentService implements ClientSocketManagerCallerInterface, BroadcastManagerCallerInterface {

    ClientSocketManager clientSocketManager;
    BroadcastManager broadcastManager;
    public static String SOCKET_SERVICE_CHANNEL="com.example.myfirstapplication.SOCKET_SERVICE_CHANNEL";
    public static String SERVER_TO_CLIENT_MESSAGE="SERVER_TO_CLIENT_MESSAGE";
    public static String CLIENT_TO_SERVER_MESSAGE="CLIENT_TO_SERVER_MESSAGE";



    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_CONNECT = "com.example.myfirstapplication.network.action.ACTION_CONNECT";
    private static final String ACTION_BAZ = "com.example.myfirstapplication.network.action.BAZ";

    // TODO: Rename parameters
    private String SERVER_HOST = "172.17.9.21";
    private int SERVER_PORT = 9090;


    public SocketManagementService() {
        super("SocketManagementService");
    }






    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CONNECT.equals(action)) {
                SERVER_HOST = intent.getStringExtra("SERVER_HOST");
                SERVER_PORT = intent.getIntExtra("SERVER_PORT",9090);
                initializeClientSocketManager();
                initializeBroadcastManager();

            }

        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void initializeClientSocketManager(){
        try{
            if(clientSocketManager==null){
                clientSocketManager=new ClientSocketManager(
                        SERVER_HOST,
                        SERVER_PORT,this);
            }
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void initializeBroadcastManager(){
        try{
            if(broadcastManager==null){
                broadcastManager=new BroadcastManager(
                        getApplicationContext(),
                        SOCKET_SERVICE_CHANNEL,
                        this);
            }
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void MessageReceived(String message) {
        try {

            if(broadcastManager!=null){
                Intent intent = new Intent();
                intent.putExtra("message", message);
                broadcastManager.sendBroadcast( SERVER_TO_CLIENT_MESSAGE, intent);
            }
        }catch (Exception error){

        }
    }

    @Override
    public void ErrorFromSocketManager(Exception error) {

    }

    @Override
    public void MessageReceivedThroughBroadcastManager(String channel, Intent intent) {
        try {
            if (clientSocketManager != null) {
                String type = intent.getStringExtra("type");
                if (type.equals(CLIENT_TO_SERVER_MESSAGE)) {
                    String latitude = intent.getStringExtra("latitude");
                    String longitude = intent.getStringExtra("longitude");
                    String message = latitude+" / "+longitude+"\n";
                    clientSocketManager.sendMessage(message);
                }
            }
        }catch (Exception error){

        }

    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }

    @Override
    public void onDestroy() {
        if(broadcastManager!=null){
            broadcastManager.unRegister();
        }
        super.onDestroy();
    }
}
