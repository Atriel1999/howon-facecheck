package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class search_stu extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_stu);

        Intent getIntent = getIntent();

        Button btn1 = (Button) findViewById(R.id.btn_start);
        Button btn2 = (Button) findViewById(R.id.btn_back);

        EditText name = (EditText) findViewById(R.id.search_name);
        EditText number = (EditText) findViewById(R.id.search_number);

        StringBuilder sb = new StringBuilder();


        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String getName = name.getText().toString();
                String getNumber = number.getText().toString();

                sb.append(getName);
                sb.append(",");
                sb.append(getNumber);

                String str = sb.toString();

                System.out.println("Texet getting from build is " + str);

                Intent intent1 = new Intent(search_stu.this, searchsplash_stu.class);
                intent1.putExtra("data",str);
                startActivity(intent1);
                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(search_stu.this, select_stu.class);
                startActivity(intent2);
                finish();
            }
        });


    }
}
