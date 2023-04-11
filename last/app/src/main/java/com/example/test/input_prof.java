package com.example.test;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class input_prof extends Activity {


    public static final byte HEADER_TEXT = 0x01;
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
        setContentView(R.layout.input_prof);

        IP = ((Global_variable)getApplication()).getIp();
        PORT = ((Global_variable)getApplication()).getPort();

        Button btn1 = (Button) findViewById(R.id.btn_start);
        Button btn2 = (Button) findViewById(R.id.btn_start1);

        btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                connectServer();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(input_prof.this, select_prof.class);
                startActivity(intent1);
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

        //os.close();
        //socket.close();   if exit

        EditText edi = (EditText) findViewById(R.id.memo);
        EditText edi1 = (EditText) findViewById(R.id.memo1);
        EditText edi2 = (EditText) findViewById(R.id.memo2);
        EditText edi3 = (EditText) findViewById(R.id.memo3);
        EditText edi4 = (EditText) findViewById(R.id.memo4);

        StringBuilder sb = new StringBuilder();
        sb.append(edi.getText().toString());
        sb.append(",");
        sb.append(edi1.getText().toString());
        sb.append(",");
        sb.append(edi2.getText().toString());
        sb.append(",");
        sb.append(edi3.getText().toString());
        sb.append(",");
        sb.append(edi4.getText().toString());

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

                    os.close();

                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}




