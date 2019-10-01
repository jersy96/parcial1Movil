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

import com.example.myfirstapplication.database.core.DatabaseManager;
import com.example.myfirstapplication.database.entities.User;
import com.example.myfirstapplication.network.HttpRequestsManagementService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends Activity {
    private DatabaseManager dbInstance;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapplication.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        dbInstance = MainActivity.getDatabase(this);
        ((Button)findViewById(R.id.login_button)).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> loginParams = new HashMap<>();
                loginParams.put("email", ((EditText)findViewById(
                        R.id.login_user_name)).getText().toString());
                loginParams.put("password", ((EditText)findViewById(
                        R.id.login_password)).getText().toString());
                loginUser(loginParams);
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
    }

    public void loginUser(final Map<String, String> userParams){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        JSONObject userBody= new JSONObject(userParams);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, userBody.toString());
        Request request = new Request.Builder()
                .url(HttpRequestsManagementService.BASE_URL+"/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200){
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        processSuccessLogin(json.getString("name"), true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    processFailureLogin("Credenciales invalidas");
                }
            }
        });
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
