package com.example.myfirstapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.database.core.DatabaseManager;
import com.example.myfirstapplication.database.entities.User;
import com.example.myfirstapplication.network.HttpRequestsManagementService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity implements BroadcastManagerCallerInterface {
    private DatabaseManager dbInstance;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapplication.MESSAGE";
    BroadcastManager broadcastManagerForHttpRequests;
    Map<String, String> userParams;

    private void initializeBroadcastManagerForHttpRequests(){
        broadcastManagerForHttpRequests=new BroadcastManager(this,
                HttpRequestsManagementService.
                        CHANNEL_HTTP_REQUESTS_SERVICE,this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        dbInstance = MainActivity.getDatabase(this);
        ((Button)findViewById(R.id.login_button)).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userParams = new HashMap<>();
                userParams.put("email", ((EditText)findViewById(
                        R.id.login_user_name)).getText().toString());
                userParams.put("password", ((EditText)findViewById(
                        R.id.login_password)).getText().toString());
                loginUser();
            }
        });

        final TextView registerLink=  findViewById(R.id.register_link);
        registerLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                intent.putExtra(EXTRA_MESSAGE, "registrar");
                startActivity(intent);
                // Code here executes on main thread after user presses button
            }
        });
        initializeBroadcastManagerForHttpRequests();
    }

    public void loginUser(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        JSONObject userBody= new JSONObject(userParams);

        Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
        intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_LOGIN);
        intent.putExtra("url", HttpRequestsManagementService.BASE_URL+"/login");
        intent.putExtra("jsonString", userBody.toString());
        HttpRequestsManagementService.makeHttpRequest(this, HttpRequestsManagementService.MESSAGE_TYPE_POST_REQUEST, intent);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(String channel, Intent intent) {
        switch (channel){
            case HttpRequestsManagementService.CHANNEL_HTTP_REQUESTS_SERVICE:
                processHttpRequestsServiceMessage(intent);
                break;
        }
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
        if (requestId == HttpRequestsManagementService.REQUEST_ID_LOGIN){
            boolean validCredentials = false;
            String failureMessage = "Ha ocurrido un error con la conexión";
            User user = null;
            List<User> users = dbInstance.userDao().getUserByEmail(userParams.get("email"));
            if (!users.isEmpty()){
                user = users.get(0);
                validCredentials = user.passwordHash.equals(userParams.get("password"));
                if (!validCredentials){
                    failureMessage = "Credenciales invalidas";
                }
            }

            if(validCredentials){
                processSuccessLogin(user.name, false);
            }else{
                processFailureLogin(failureMessage);
            }
        }else{
            showToast(intent.getStringExtra("message"));
        }
    }

    private void processHttpRequestRequestResponse(Intent intent){
        int requestId = intent.getIntExtra("requestId", HttpRequestsManagementService.DEFAULT_REQUEST_ID);
        int code = intent.getIntExtra("status_code", -1);
        String responseBody = intent.getStringExtra("response_body");
        switch (requestId){
            case HttpRequestsManagementService.REQUEST_ID_LOGIN:
                processLogin(code, responseBody);
                break;
        }
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }

    public void showToast(final String message)
    {
        final LoginActivity context = this;

        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processLogin(int code, String responseBody){
        if (code == 200){
            try {
                JSONObject json = new JSONObject(responseBody);
                processSuccessLogin(json.getString("name"), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            processFailureLogin("Credenciales invalidas");
        }
    }

    private void processSuccessLogin(String userName, boolean online){
        showToast("Ingreso exitoso");

        Intent intetToBeCalled=new
                Intent(getApplicationContext(),
                MainActivity.class);
        intetToBeCalled.putExtra("current_user_name", userName);
        intetToBeCalled.putExtra("online", online);
        startActivity(intetToBeCalled);
    }

    private void processFailureLogin(String message){
        showToast(message);
    }
}
