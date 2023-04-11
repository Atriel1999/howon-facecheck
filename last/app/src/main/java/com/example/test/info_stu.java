package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class info_stu extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_stu);

        TextView name = (TextView) findViewById(R.id.attend_name);
        TextView info = (TextView) findViewById(R.id.attend_info);
        Button btnback = (Button) findViewById(R.id.btnback);
        info.setMovementMethod(new ScrollingMovementMethod());


        Intent getIntent = getIntent();
        String getText = ""+getIntent.getStringExtra("attend");
        //value = name,number,date,code /
        System.out.println("Text getting from info_stu is " + getText);

        String arr[] = getText.split("/");
        StringBuilder sb = new StringBuilder();


        for (int i = 0; i < arr.length; i++) {
            //split[0~3]: 0:이름 1:학번 2:날짜 3:강의코드
            String resplit[] = arr[i].split(",");
            sb.append(resplit[0] + getString(R.string.tab) + getString(R.string.tab));
            sb.append(resplit[1] + getString(R.string.tab) + getString(R.string.tab));
            sb.append(resplit[2] + getString(R.string.tab) + getString(R.string.tab));
            try {
                sb.append(resplit[3] + "\n");
            } catch (ArrayIndexOutOfBoundsException e){
                System.out.println("어.. 강의코드가 없네?");
                sb.append("\n");
            }
        }


        String namesplit[] = arr[0].split(",");
        name.setText(namesplit[0] +" 학생의 출석정보");

        String infostr = sb.toString();
        info.setText("이름"+getString(R.string.tab)+getString(R.string.tab)+getString(R.string.tab)
                +"학번"+getString(R.string.tab)+getString(R.string.tab)+getString(R.string.tab)+getString(R.string.tab)
                    + "   날짜"+getString(R.string.tab)+getString(R.string.tab)+getString(R.string.tab)+getString(R.string.tab)
                         +getString(R.string.tab)+  "    강의번호\n" + infostr);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(info_stu.this, select_stu.class);
                startActivity(intent1);
                finish();
            }
        });


    }
}
