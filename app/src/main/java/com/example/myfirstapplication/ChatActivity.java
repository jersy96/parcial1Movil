package com.example.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    // Firebase reference1, reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);

        // Firebase.setAndroidContext(this);
        // reference1 = new Firebase("https://chatapp-60323.firebaseio.com/messages/" + com.example.myfirstapplication.UserDetails.username + "_" + com.example.myfirstapplication.UserDetails.chatWith);
        // reference2 = new Firebase("https://chatapp-60323.firebaseio.com/messages/" + com.example.myfirstapplication.UserDetails.chatWith + "_" + com.example.myfirstapplication.UserDetails.username);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                addMessageBox(messageText, 1);
                addMessageBox(messageText, 2);
                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    // reference1.push().setValue(map);
                    // reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        /*reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox(message, 1);
                }
                else{
                    addMessageBox(message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });*/
    }

    public void addMessageBox(String message, int type){
        /*TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 7.0f;
        */
        View view;
        if(type == 1) {
            //lp2.gravity = Gravity.LEFT;
            //textView.setBackgroundResource(R.drawable.bubble_in);
            view = LayoutInflater.from(this).inflate(R.layout.item_message_sended,null);
            TextView usernameMessage = (TextView) view.findViewById(R.id.text_message_name);
            TextView bodyMessage = (TextView) view.findViewById(R.id.text_message_body);
            //TextView timeMessage = (TextView) view1.findViewById(R.id.text_message_time);
            usernameMessage.setText("Javier");
            bodyMessage.setText(message);
        }
        else{
            //lp2.gravity = Gravity.RIGHT;
            //textView.setBackgroundResource(R.drawable.bubble_out);
            view = LayoutInflater.from(this).inflate(R.layout.item_message_received,null);
            TextView usernameMessage = (TextView) view.findViewById(R.id.text_message_name);
            TextView bodyMessage = (TextView) view.findViewById(R.id.text_message_body);
            //TextView timeMessage = (TextView) view1.findViewById(R.id.text_message_time);
            usernameMessage.setText("Abraham");
            bodyMessage.setText(message);
        }
        //textView.setLayoutParams(lp2);
        //layout.addView(textView);
        layout.addView(view);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
