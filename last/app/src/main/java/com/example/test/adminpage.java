package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class adminpage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminpage);

        Intent getIntent = getIntent();

        Button btn1 = (Button) findViewById(R.id.btn_start);
        Button btninput = (Button) findViewById(R.id.btn_input);
        EditText inputbox = (EditText) findViewById(R.id.ipipip);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(adminpage.this, splash.class);
                startActivity(intent1);
                finish();
            }
        });

        btninput.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String ip = inputbox.getText().toString();
                ((Global_variable)getApplication()).setIP(ip);
                String test = ((Global_variable)getApplication()).getIp();
                Toast.makeText(getApplicationContext(), "아이피를 " + test +"로 수정완료", Toast.LENGTH_LONG).show();
                Intent intent1 = new Intent(adminpage.this, splash.class);
                startActivity(intent1);
                finish();
            }
        });



    }
}
