package com.example.animalclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

public class Login extends AppCompatActivity {

    public static boolean login = false;
    private Handler myHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myHandler = new Handler();
    }

    public void login(View view) {
        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        Database.setBody(username, password, 1);
        Database.getInstance().execute();
        Login loginActivity = this;
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(login) {
                    Intent intent = new Intent(loginActivity, MainActivity.class);
                    startActivity(intent);
                }
            }
        }, 1000);

    }

    public void Register(View view) {
        Intent intent = new Intent(this,Register.class);
        startActivity(intent);

    }
}
