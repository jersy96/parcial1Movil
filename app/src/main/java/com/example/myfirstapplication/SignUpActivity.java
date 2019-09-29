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

import com.example.myfirstapplication.database.core.DatabaseManager;
import com.example.myfirstapplication.database.entities.User;
import com.example.myfirstapplication.network.HttpRequestsManagementService;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    private DatabaseManager dbInstance;

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
                Map<String, String> newUserParams = new HashMap<>();
                newUserParams.put("name", nameString);
                newUserParams.put("email", emailString);
                newUserParams.put("password", passwordString);
                createUser(newUserParams);

                // Code here executes on main thread after user presses button
            }
        });
    }

    public void createUser(final Map<String, String> userParams){
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
                .url(HttpRequestsManagementService.BASE_URL+"/users")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Ha ocurrido un error con la conexión");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.code() == 200) {
                    showToast("Usuario creado con éxito, ingrese con su correo y contraseña");

                    User user = new User();
                    user.name = userParams.get("name");
                    user.email = userParams.get("email");
                    user.passwordHash = userParams.get("password");

                    dbInstance.userDao().insertUser(user);

                    Intent intetToBecalled=new
                            Intent(getApplicationContext(),
                            LoginActivity.class);
                    startActivity(intetToBecalled);
                }else{
                    showToast("Ha ocurrido un error al crear nuevo usuario");
                }
            }
        });
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

}