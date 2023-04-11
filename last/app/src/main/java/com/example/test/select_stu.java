package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class select_stu extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_stu);

        Intent getIntent = getIntent();

        Button btn1 = (Button) findViewById(R.id.btn_enroll);
        Button btn2 = (Button) findViewById(R.id.btn_search);
        Button btnback = (Button) findViewById(R.id.btn_back);


        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(select_stu.this, enroll_stu.class);
                startActivity(intent1);
                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(select_stu.this, search_stu.class);
                startActivity(intent2);
                finish();
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(select_stu.this, main_acitivity.class);
                startActivity(intent1);
                finish();
            }
        });
    }
}

