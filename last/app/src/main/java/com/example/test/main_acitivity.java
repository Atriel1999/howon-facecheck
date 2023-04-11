package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class main_acitivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);

        Intent getIntent = getIntent();

        Button btn1 = (Button) findViewById(R.id.btn_student);
        Button btn2 = (Button) findViewById(R.id.btn_professor);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(main_acitivity.this, select_stu.class);
                startActivity(intent1);
                finish();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(main_acitivity.this, select_prof.class);
                startActivity(intent2);
                finish();
            }
        });
    }
}
