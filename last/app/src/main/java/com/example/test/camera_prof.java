package com.example.test;

import static com.google.mlkit.vision.common.InputImage.IMAGE_FORMAT_NV21;
import static com.google.mlkit.vision.common.InputImage.fromBitmap;
import static com.google.mlkit.vision.common.InputImage.fromByteArray;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.hardware.camera2.CameraAccessException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.*;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;


public class camera_prof extends Activity {
    private TextureView mCameraTextureView;
    private Preview mPreview;
    // 추가
    private TextToSpeech tts;
    Handler handler;
    // 추가 끝

    private String IP;
    private int PORT;

    Activity mainActivity = this;

    private static final String TAG = "camera_prof";

    static final int REQUEST_CAMERA = 1;
    private int Attendtime = 10;

    ImageView img;

    int rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //=================================================
        //메인스레드에서 기기의 각도를 구해서 preview.takepicture(rotation)로 넘겨줌
        OrientationEventListener orientEventListener;

        orientEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int arg0) {
//                System.out.println("회전 각도 출력 arg0 : "+arg0);// 메인스레드에서의 회전 각도 출력
                rotation = arg0;
//                rotation = arg0;
            }
        };

        if (orientEventListener.canDetectOrientation()) {
            orientEventListener.enable();
        }
        //System.out.println("rotation : "+rotation);
        //==============================================

        IP = ((Global_variable)getApplication()).getIp();
        PORT = ((Global_variable)getApplication()).getPort();

        // 추가
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String result = bundle.getString("key");

//                if(msg.obj != null) {
//                    String result = (String) msg.obj;
                System.out.println(result);
                tts.setPitch(1.0f);         // 음성 톤을 2.0배 올려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                // editText에 있는 문장을 읽는다.
                tts.speak(result+"님 안녕하세요", TextToSpeech.QUEUE_FLUSH, null);
            }
            //}
        };
        // 추가 끝

        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.camera_prof);
        Intent getIntent = getIntent();

        String code = ""+getIntent.getStringExtra("Inputcode");

        ImageButton button = findViewById(R.id.take_photo);
        Button restartbtn = (Button) findViewById(R.id.restart_btn);
        Button startbtn = (Button) findViewById(R.id.starting_btn);
        Button backbtn = (Button) findViewById(R.id.btn_back);

        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        mPreview = new Preview(this, mCameraTextureView, handler, IP, PORT);
        img = (ImageView) findViewById(R.id.imagefree);

        EditText timeset1 = (EditText)findViewById(R.id.timeset);




//        CountDownTimer CDT = new CountDownTimer(Attendtime * 1000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                System.out.println("dbg4 Attendtime =" + Attendtime);
//                //반복실행할 구문
//                try {
//                    System.out.println("dbg5 Attendtime =" + Attendtime);
//                    mPreview.takePicture(rotation);
//                } catch (CameraAccessException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            public void onFinish() {
//                Toast.makeText(getApplicationContext(), "출석체크가 종료되었습니다.", Toast.LENGTH_LONG).show();
//            }
//        };

//
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Attendtime = Integer.parseInt(timeset1.getText().toString());
                System.out.println("출석 실행시간은: " + Attendtime);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (Attendtime >= 1) {

                            try {
                                System.out.println("dbg5 남은출석시간 =" + Attendtime);
                                mPreview.takePicture(rotation);

                                if(Attendtime == 1){
                                    Toast.makeText(getApplicationContext(), "출석체크가 종료되었습니다.", Toast.LENGTH_LONG).show();
                                }

                                Attendtime--;
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(camera_prof.this, studyinfo_prof.class);
                intent1.putExtra("login_id", code);
                startActivity(intent1);
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPreview.takePicture(rotation);
                    //final File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", "pic.jpg");
                    //FileInputStream file = new FileInputStream(Environment.getExternalStorageDirectory() + "/DCIM", "pic.jpg");
                    //InputStream is = null ;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                //여기까지 try_catch

            }
        });

        restartbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
                            mPreview = new Preview(mainActivity, mCameraTextureView, handler, IP, PORT);
                            mPreview.openCamera();
                            Log.d(TAG,"mPreview set");
                        } else {
                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
    }









}