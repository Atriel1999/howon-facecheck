package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

public class select_prof extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_prof);

        Button btn1 = (Button) findViewById(R.id.btn_newpro);
        Button btn2 = (Button) findViewById(R.id.btn_attendance);
        Button btnback = (Button) findViewById(R.id.btn_back);


        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(select_prof.this, input_prof.class);
                startActivity(intent1);
                finish();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(select_prof.this, login_prof.class);
                startActivity(intent2);
                finish();
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(select_prof.this, main_acitivity.class);
                startActivity(intent1);
                finish();
            }
        });
    }
}
