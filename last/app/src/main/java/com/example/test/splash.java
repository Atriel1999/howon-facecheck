package com.example.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class splash extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 50;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Test);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }

        Intent getIntent = getIntent();

        Button btn = (Button) findViewById(R.id.btn_start);
        Button btnadmin = (Button) findViewById(R.id.btn_admin);

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(splash.this, main_acitivity.class);
                startActivity(intent1);
                finish();
            }
        });


        btnadmin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                System.out.println("dbg 2");
                Intent intent2 = new Intent(splash.this, adminpage.class);
                startActivity(intent2);
                finish();
            }
        });
    }
}
