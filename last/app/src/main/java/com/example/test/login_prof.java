package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class login_prof extends Activity {

    public static final byte HEADER_TEXT = 0x04;
    public static final byte HEADER_IMAGE = 0x02;

    private String IP;
    private int PORT;

    private Socket socket;
    private OutputStream os;
    private InputStream is;

    private static final int REQUEST_CAMERA = 40;
    private static final int REQUEST_PERMISSION = 50;

    private static final int HANDLER_MSG_EXIT = 999;
    private static final int HANDLER_MSG_CONNECT = 100;

    private void errorExit(String title, String message){
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    //크기 가져오기
    private byte[] getDataLength(int length) {
        byte[] blen = new byte[4];

        blen[0] |= (byte) ((length & 0xFF000000) >> 24);
        blen[1] |= (byte) ((length & 0xFF0000) >> 16);
        blen[2] |= (byte) ((length & 0xFF00) >> 8);
        blen[3] |= (byte) (length & 0xFF);

        return blen;

    }

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_prof);
        Intent getlntent = getIntent();

        IP = ((Global_variable)getApplication()).getIp();
        PORT = ((Global_variable)getApplication()).getPort();

        Button btn1 = (Button) findViewById(R.id.btn_start);
        Button btn2 = (Button) findViewById(R.id.btn_back);

        //intent1 = new Intent(login_prof.this, studyinfo_prof.class);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectServer();

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent2 = new Intent(login_prof.this, select_prof.class);
                startActivity(intent2);
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }


    private void connectServer() {
        new login_prof.ServerConnectThread().start();
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

        //os.close();
        //socket.close();   if exit

        EditText edi = (EditText) findViewById(R.id.studyname);
        EditText edi1 = (EditText) findViewById(R.id.passwd);


        StringBuilder sb = new StringBuilder();
        sb.append(edi.getText().toString());
        sb.append(",");
        sb.append(edi1.getText().toString());

        final String msg = sb.toString();
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

                    byte[] buffer = new byte[100];
                    int readByteCount = is.read(buffer);

                    String coverted = new String(buffer, 0, readByteCount, "UTF-8");

                    System.out.println("data received:" + coverted);

                    //Toast 전용 핸들러
                    Handler handler_fail = new Handler(Looper.getMainLooper());
                    handler_fail.postDelayed(new Runnable(){
                        public void run(){
//                            Toast.makeText(login_prof.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }, 0);

                    Handler handler_success = new Handler(Looper.getMainLooper());
                    handler_success.postDelayed(new Runnable(){
                        public void run(){
                            Toast.makeText(login_prof.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        }
                    }, 0);


                    if(coverted.equals("false") || coverted == null){
                        handler_fail.sendEmptyMessage(0);
                    } else{
                        Intent intent1 = new Intent(login_prof.this, studyinfo_prof.class);
                        intent1.putExtra("login_id",coverted);
                        startActivity(intent1);
                        handler_success.sendEmptyMessage(0);

                        finish();

                    }



                    os.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}