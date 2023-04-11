package com.example.test;
import android.graphics.Bitmap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class studyinfo_prof extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studyinfo_prof);

        Intent getIntent = getIntent();
        String value =""+getIntent.getStringExtra("login_id");

        Button btn_cam = (Button) findViewById(R.id.btn_camera);
        Button btn2 = (Button) findViewById(R.id.btn_manage);
        Button btn3 = (Button) findViewById(R.id.btn_manageattend);
        TextView code = (TextView) findViewById(R.id.lecture_code);

        System.out.println("data received from studyinfo_prof: " + value);  //value = 강의코드
        code.setText(value);
        ((Global_variable)getApplication()).setCode(value);

        btn_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(studyinfo_prof.this, camera_prof.class);
                intent.putExtra("Inputcode", value);
                startActivity(intent);
                finish();

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(studyinfo_prof.this, attendcheck_prof.class);
                intent.putExtra("attend_info",value);
                startActivity(intent);
                finish();

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent3 = new Intent(studyinfo_prof.this, login_prof.class);
                startActivity(intent3);
                finish();
            }
        });
    }





}