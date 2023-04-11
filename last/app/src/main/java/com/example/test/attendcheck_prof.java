package com.example.test;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class attendcheck_prof extends Activity {

    private Button btn_start;
    private String imagePath;
    private String value;
    private String temp ="";

    TextView code, attend_info, studyname, profname;

    public static final byte HEADER_TEXT = 0x05;

    private String IP;
    private int PORT;

    private Socket socket;
    private OutputStream os;
    private InputStream is;

    private static final int HANDLER_MSG_CONNECT = 100;

    //추가함수들
    //크기 가져오기
    private byte[] getDataLength(int length) {
        byte[] blen = new byte[4];

        blen[0] |= (byte) ((length & 0xFF000000) >> 24);
        blen[1] |= (byte) ((length & 0xFF0000) >> 16);
        blen[2] |= (byte) ((length & 0xFF00) >> 8);
        blen[3] |= (byte) (length & 0xFF);

        return blen;

    }

    //int형을 byte로 전환
    private byte[] objectToByte(byte header, byte[] data) {

        byte[] result = null;
        int size = 1 + 4 + data.length;
        result = new byte[size];
        result[0] = header;

        byte[] dataLen = getDataLength(data.length);
        System.arraycopy(dataLen, 0, result, 1, dataLen.length);
        System.arraycopy(data, 0, result, 5, data.length);

        return result;
    }

    private void connectServer() {
        Log.e("test", "connectServer");
        new ServerConnectThread().start();
    }

    class ServerConnectThread extends Thread {
        @Override
        public void run() {
            try {
                socket = new Socket();
                SocketAddress addr = new InetSocketAddress(IP, PORT);
                socket.connect(addr);
                os = socket.getOutputStream();
                is = socket.getInputStream();
                handler.sendEmptyMessage(HANDLER_MSG_CONNECT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            if (what == HANDLER_MSG_CONNECT) {
                sendMsg();
            }
        }
    };

    private void sendMsg() {

        final String msg = value;
        final String quitMsg = "quit";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] dataMsg = objectToByte(HEADER_TEXT, msg.getBytes("utf-8"));
                    os.write(dataMsg, 0, dataMsg.length);
                    os.flush();

                    dataMsg = objectToByte(HEADER_TEXT, quitMsg.getBytes("utf-8"));
                    os.write(dataMsg, 0, dataMsg.length);
                    os.flush();

                    byte[] buffer = new byte[1024];
                    int readByteCount = is.read(buffer);

                    String coverted = new String(buffer, 0, readByteCount, "UTF-8");

                    //System.out.println("dbg2: data received:" + coverted);
                    //coverted 수업이름#교수이름# 출석이름,출석학번,출석날짜/ ...

                    if(coverted.equals("fail")){
                        Toast.makeText(getApplicationContext(),"데이터 받기를 실패하였습니다",Toast.LENGTH_LONG).show();
                    }else {
                        String split[] = coverted.split("#");

                        studyname.setText(split[0]);
                        profname.setText(split[1] +" 교수님");

                        String splitdata[] = split[2].split("/");
                        StringBuilder sb = new StringBuilder();


                        System.out.println("debug data = " + split[2]);

                        for (int i=0; i<splitdata.length;i++){
                            //split[0~3]: 0:이름 1:학번 2:날짜 3:강의코드
                            String resplit[] = splitdata[i].split(",");



                            if(i==0) {
                                sb.append(resplit[2] + "\n");
                                sb.append("이름" + getString(R.string.tab) + getString(R.string.tab) + getString(R.string.tab) + "학번" + "\n");
                                sb.append(resplit[0] + getString(R.string.tab) + getString(R.string.tab));
                                sb.append(resplit[1] + "\n");
                            }
                            else if(resplit[2].equals(temp)) {
                                sb.append(resplit[0] + getString(R.string.tab) + getString(R.string.tab));
                                sb.append(resplit[1] + "\n");

                            }
                            else{
                                sb.append("----------------------------------" + "\n");
                                sb.append(resplit[2] + "\n");
                                sb.append("이름" + getString(R.string.tab) + getString(R.string.tab) + getString(R.string.tab) + "학번" + "\n");
                                sb.append(resplit[0] + getString(R.string.tab) + getString(R.string.tab));
                                sb.append(resplit[1] + "\n");

                            }

                            /*
                            System.out.println("dbg3 temp = " + temp + " split[2] = " + resplit[2]);
                            System.out.println("dbg3 = " + (temp.equals(resplit[2])));*/
                            temp = resplit[2];

                        }
                        String infostr = sb.toString();
                        attend_info.setText(infostr);


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendcheck_prof);

        IP = ((Global_variable)getApplication()).getIp();
        PORT = ((Global_variable)getApplication()).getPort();

        Intent getIntent = getIntent();
        value =""+getIntent.getStringExtra("attend_info");

        code = (TextView) findViewById(R.id.lecture_code);
        attend_info = (TextView) findViewById(R.id.attend_info_prof);
        attend_info.setMovementMethod(new ScrollingMovementMethod());
        studyname = (TextView) findViewById(R.id.study_name);
        profname = (TextView) findViewById(R.id.profname);
        Button backbtn = (Button) findViewById(R.id.btnback);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(attendcheck_prof.this, studyinfo_prof.class);
                intent1.putExtra("login_id",value);
                startActivity(intent1);
                finish();
            }
        });


        System.out.println("data received from studyinfo_prof: " + value);
        code.setText(value);

        connectServer();


    }





}