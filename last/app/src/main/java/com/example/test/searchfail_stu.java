package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class searchfail_stu extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchfail_stu);

        Button btn = (Button) findViewById(R.id.btn_back);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gentlntent = getIntent();
                Intent intent2 = new Intent(searchfail_stu.this, search_stu.class);
                startActivity(intent2);
                finish();
            }
        });
    }
}
