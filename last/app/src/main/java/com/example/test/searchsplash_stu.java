package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class searchsplash_stu extends Activity {

    private Button btn_start;
    private String imagePath;

    public static final byte HEADER_TEXT = 0x06;

    private Socket socket;
    private OutputStream os;
    private InputStream is;

    private static final int HANDLER_MSG_CONNECT = 100;

    private String value ="";
    ImageView iv;

    private String IP;
    private int PORT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchsplash_stu);

        IP = ((Global_variable)getApplication()).getIp();
        PORT = ((Global_variable)getApplication()).getPort();

        Intent getIntent = getIntent();
        String getText = ""+getIntent.getStringExtra("data");
        //value = name,number
        System.out.println("Text getting from Intent is " + getText);
        value = getText;


        iv = (ImageView) findViewById(R.id.loading);

        Animation anim = AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.rotate_anim);

        connectServer();

        //회전시작 100번
        iv.startAnimation(anim);

        //회전스탑

    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==0){
            if(grantResults[0]==0){
                Toast.makeText(this,"카메라 권한 승인",Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(this,"카메라 권한이 거절되었습니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

                    System.out.println("data received:" + coverted);
                    iv.setImageAlpha(0);
                    iv.setVisibility(View.VISIBLE);
                    if(coverted.equals("fail")){
                        Intent intent = new Intent(searchsplash_stu.this, searchfail_stu.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(searchsplash_stu.this, info_stu.class);
                        intent.putExtra("attend", coverted);
                        startActivity(intent);
                        finish();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




}