package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfirstapplication.broadcast.BroadcastManager;
import com.example.myfirstapplication.broadcast.BroadcastManagerCallerInterface;
import com.example.myfirstapplication.database.core.DatabaseManager;
import com.example.myfirstapplication.database.entities.User;
import com.example.myfirstapplication.network.HttpRequestsManagementService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements BroadcastManagerCallerInterface {
    private DatabaseManager dbInstance;
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
        setContentView(R.layout.activity_sign_up);
        dbInstance = MainActivity.getDatabase(this);

        final Button button = findViewById(R.id.register_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String nameString= ((EditText)findViewById(R.id.register_name)).getText().toString();
                final String emailString = ((EditText) findViewById(R.id.register_email)).getText().toString();
                final String passwordString = ((EditText) findViewById(R.id.register_password)).getText().toString();
                userParams = new HashMap<>();
                userParams.put("name", nameString);
                userParams.put("email", emailString);
                userParams.put("password", passwordString);
                createUser();
                // Code here executes on main thread after user presses button
            }
        });
        initializeBroadcastManagerForHttpRequests();
    }

    public void createUser(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        JSONObject userBody= new JSONObject(userParams);
        Intent intent = HttpRequestsManagementService.createIntentForHttpRequest(getApplicationContext());
        intent.putExtra("requestId", HttpRequestsManagementService.REQUEST_ID_USER_CREATION);
        intent.putExtra("url", HttpRequestsManagementService.BASE_URL+HttpRequestsManagementService.REQUEST_URL_USER_CREATION);
        intent.putExtra("jsonString", userBody.toString());
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
        if (requestId == HttpRequestsManagementService.REQUEST_ID_USER_CREATION){
            showToast(intent.getStringExtra("message"));
        }
    }

    private void processHttpRequestRequestResponse(Intent intent){
        int requestId = intent.getIntExtra("requestId", HttpRequestsManagementService.DEFAULT_REQUEST_ID);
        int code = intent.getIntExtra("status_code", -1);
        String responseBody = intent.getStringExtra("response_body");
        switch (requestId){
            case HttpRequestsManagementService.REQUEST_ID_USER_CREATION:
                processSignup(code, responseBody);
                break;
        }
    }

    private void processSignup(int code, String responseBody){
        if(code == 200) {
            showToast("Usuario creado con éxito, ingrese con su correo y contraseña");
            try {
                JSONObject json = new JSONObject(responseBody);
                User user = new User();
                user.name = userParams.get("name");
                user.email = userParams.get("email");
                user.passwordHash = userParams.get("password");
                user.externalId = json.getInt("id");
                dbInstance.userDao().insertUser(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intentToBeCalled=new
                    Intent(getApplicationContext(),
                    LoginActivity.class);
            startActivity(intentToBeCalled);
        }else{
            showToast("Ha ocurrido un error al crear nuevo usuario");
        }
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {

    }

    @Override
    protected void onDestroy() {
        if(broadcastManagerForHttpRequests!=null){
            broadcastManagerForHttpRequests.unRegister();
        }
        super.onDestroy();
    }
}