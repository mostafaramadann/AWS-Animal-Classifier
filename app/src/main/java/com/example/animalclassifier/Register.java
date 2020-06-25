package com.example.animalclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

public class Register extends AppCompatActivity {
    private Handler myHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
    public void submit(View view) {
        String name = ((EditText) findViewById(R.id.user)).getText().toString();
        String password = ((EditText) findViewById(R.id.pass)).getText().toString();
        Database.setBody(name,password , 2);
        Database.getInstance().execute();
        //Register registerActivity = this;
        /*myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Login.login) {
                    finish();
                }
            }
        },5000);*/
    }
}
