package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfirstapplication.database.core.TrackUDatabaseManager;
import com.example.myfirstapplication.database.entities.Point;
import com.example.myfirstapplication.database.entities.User;

import org.json.JSONArray;
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

public class SignUpActivity extends AppCompatActivity {

    static TrackUDatabaseManager INSTANCE;


    static TrackUDatabaseManager getDatabase(final Context context) {
        Log.i("cule", "que que");
        if (INSTANCE == null) {
            Log.i("cule", "que que 2");
            synchronized (TrackUDatabaseManager.class) {
                Log.i("cule", "que que 3");
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, TrackUDatabaseManager.class, "local-database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // MainActivity.INSTANCE.userDao();
        getDatabase(this);
        final Button button = findViewById(R.id.register_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String nameString= ((EditText)findViewById(R.id.register_name)).getText().toString();
                final String emailString = ((EditText) findViewById(R.id.register_email)).getText().toString();
                final String passwordString = ((EditText) findViewById(R.id.register_password)).getText().toString();

                createUser(emailString, nameString, passwordString);

                // Code here executes on main thread after user presses button
            }
        });
    }

    public void createUser(final String email, final String name, final String password){
        Map<String, String> newUserParams = new HashMap<>();
        newUserParams.put("name", name);
        newUserParams.put("email", email);
        newUserParams.put("password", password);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        JSONObject userBody= new JSONObject(newUserParams);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, userBody.toString());
        Request request = new Request.Builder()
                .url("http://192.168.0.7:3000/users")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Ha ocurrido un error con la conexión");
                Log.i("culeeeee", "aqui estoyyyy");
                saveUserLocally(email, name, password);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.code() == 200) {
                    Log.i("culetagmakia", response.body().toString());
                    saveUserLocally(email, name, password);
                    showToast("Usuario creado con éxito, ingrese con su correo y contraseña");
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

    public void saveUserLocally(String email, String name, String   password) {
        User user= new User();
        user.email = email;
        user.password= password;
        SignUpActivity.INSTANCE.userDao().insertUser(user);
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