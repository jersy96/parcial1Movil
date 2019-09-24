package com.example.myfirstapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapplication.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        ((Button)findViewById(R.id.login_button)).
                setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intetToBecalled=new
                        Intent(getApplicationContext(),
                        MainActivity.class);
                intetToBecalled.putExtra("user_name",
                        ((EditText)findViewById(
                                R.id.login_user_name)).getText().toString());
                intetToBecalled.putExtra("user_password",
                        ((EditText)findViewById(
                                R.id.login_password)).getText().toString());
                startActivity(intetToBecalled);
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
}
