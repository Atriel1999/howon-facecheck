package com.example.test;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class enroll_stu extends Activity {

    private Button btn_start;
    private String imagePath;

    public static final byte HEADER_TEXT = 0x07;
    public static final byte HEADER_IMAGE = 0x03;

    private String IP;
    private int PORT;

    private Socket socket;
    private OutputStream os;
    private InputStream is;

    private static final int REQUEST_CAMERA = 40;
    private static final int REQUEST_PERMISSION = 50;

    private static final int HANDLER_MSG_CONNECT = 100;
    public static File dir = new File(new File(Environment.getExternalStorageDirectory(), "bleh"), "bleh");


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
        EditText name = (EditText) findViewById(R.id.stu_name);
        EditText id = (EditText) findViewById(R.id.stu_id);
        EditText major = (EditText) findViewById(R.id.stu_major);
        EditText code = (EditText) findViewById(R.id.stu_lecturecode);

        StringBuilder sb = new StringBuilder();
        sb.append(name.getText().toString());
        sb.append(",");
        sb.append(id.getText().toString());
        sb.append(",");
        sb.append(major.getText().toString());
        sb.append(",");
        sb.append(code.getText().toString());

        final String msg = sb.toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] dataMsg = objectToByte(HEADER_TEXT, msg.getBytes("utf-8"));
                    os.write(dataMsg, 0, dataMsg.length);
                    os.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //카메라 intent 실행
    private void goCamera() {
        Intent intentcamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg";
            File file = new File(path);
            if (!file.isFile()) {
                file.createNewFile();
            }

            Uri uri = FileProvider.getUriForFile(this, "com.test.fileprovider", file);
            intentcamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intentcamera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intentcamera, REQUEST_CAMERA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
//            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "촬영 완료", Toast.LENGTH_SHORT).show();
                sendImage();
//            }
        }
    }

    public void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
        }
    }

    private void sendImage() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg";
        File file = new File(path);

        final byte[] imageByteArr = new byte[(int) file.length()];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(imageByteArr);
            fis.close();
            final String quitMsg = "quit";

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = objectToByte(HEADER_IMAGE, imageByteArr);
                        os.write(data);

                        byte[] dataMsg = objectToByte(HEADER_TEXT, quitMsg.getBytes("utf-8"));
                        os.write(dataMsg, 0, dataMsg.length);
                        os.flush();

                        byte[] buffer = new byte[1024];
                        int readByteCount = is.read(buffer);

                        String coverted = new String(buffer, 0, readByteCount, "UTF-8");

                        System.out.println("data received:" + coverted);

                        //Toast 전용 핸들러
                        Handler handler_fail = new Handler(Looper.getMainLooper());
                        handler_fail.postDelayed(new Runnable(){
                            public void run(){
                                Toast.makeText(enroll_stu.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                            }
                        }, 0);

                        Handler handler_success = new Handler(Looper.getMainLooper());
                        handler_success.postDelayed(new Runnable(){
                            public void run(){
                                Toast.makeText(enroll_stu.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            }
                        }, 0);

                        if(coverted.equals("false") || coverted == null){
                            handler_fail.sendEmptyMessage(0);
                        } else {
                            Intent intent1 = new Intent(enroll_stu.this, complete_stu.class);
                            startActivity(intent1);
                            handler_success.sendEmptyMessage(0);

                            finish();
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    os.close();
//    socket.close();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_stu);

        IP = ((Global_variable)getApplication()).getIp();
        PORT = ((Global_variable)getApplication()).getPort();


        Button btn1 = (Button) findViewById(R.id.btn_start);
        Button btn2 = (Button) findViewById(R.id.btn_start1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForPermissions();
                int permissionCheck = ContextCompat.checkSelfPermission(enroll_stu.this, Manifest.permission.CAMERA);
                if(permissionCheck == PackageManager.PERMISSION_DENIED){
                    //권한없음
                    ActivityCompat.requestPermissions(enroll_stu.this,new String[]{Manifest.permission.CAMERA},0);
                }else{
                    connectServer();
                    goCamera();
                }


                //토스트 메시지 출력
                switch (v.getId()) {
                    case R.id.btn_start:
                        Toast.makeText(getApplicationContext(), "본인의 얼굴을 촬영해주세요.", Toast.LENGTH_LONG).show();
                        break;
                }

//                Intent intent1 = new Intent(enroll_stu.this, complete_stu.class);
//                startActivity(intent1);
//                finish();

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(enroll_stu.this, select_stu.class);
                startActivity(intent1);
                finish();
            }
        });


    }


}



    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 10000 && resultCode == RESULT_OK) {
            //사진을 ImageView에 보여줌
            BitmapFactory.Options factory = new BitmapFactory.Options();
            factory.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(imagePath);

            factory.inJustDecodeBounds = false;
            factory.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, factory);

        }
    }

    //Android Camera Application이 설치되어 있는지 확인
    private boolean isExistCameraApplication() {
        PackageManager packageManager = getPackageManager();

        //Camera Application
        Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo>cameraApps = packageManager.queryIntentActivities(cameraApp, PackageManager.MATCH_DEFAULT_ONLY);

        return cameraApps.size() > 0;
    }
} */

/*
            EditText edi = (EditText) findViewById(R.id.memo);
            EditText edi1 = (EditText) findViewById(R.id.memo1);
            EditText edi2 = (EditText) findViewById(R.id.memo2);
            EditText edi3 = (EditText) findViewById(R.id.memo3);

            btn1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                sb.append(edi.getText().toString());
                sb.append(",");
                sb.append(edi1.getText().toString());
                sb.append(",");
                sb.append(edi2.getText().toString());
                sb.append(",");
                sb.append(edi3.getText().toString());
                sb.append(",");
            }
        });*/
