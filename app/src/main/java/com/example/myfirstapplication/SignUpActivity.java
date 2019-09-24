package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final Button button = findViewById(R.id.register_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String nameString= ((EditText)findViewById(R.id.register_name)).getText().toString();
                final String emailString = ((EditText) findViewById(R.id.register_email)).getText().toString();
                final String passwordString = ((EditText) findViewById(R.id.register_password)).getText().toString();
                Log.i("name", nameString);
                Log.i("email", emailString);
                Log.i("password", passwordString);
                // Code here executes on main thread after user presses button
            }
        });
    }
}