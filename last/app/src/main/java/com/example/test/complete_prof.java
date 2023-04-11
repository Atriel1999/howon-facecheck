package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class complete_prof extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_prof);

        Intent getIntent = getIntent();

        Button btn1 = (Button) findViewById(R.id.btn_start);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {




                Intent intent1 = new Intent(complete_prof.this, select_prof.class);
                startActivity(intent1);
                finish();
            }

        });
    }
}
