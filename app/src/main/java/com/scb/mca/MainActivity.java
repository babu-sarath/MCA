package com.scb.mca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View view) {
        intent=new Intent(this,StudentHome.class);
        startActivity(intent);
    }

    public void forgotPassword(View view) {
        intent=new Intent(this,ForgotPassword.class);
        startActivity(intent);
    }
}