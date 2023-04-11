package com.example.test;

import static android.speech.tts.TextToSpeech.ERROR;
import static com.google.mlkit.vision.common.InputImage.IMAGE_FORMAT_NV21;
import static com.google.mlkit.vision.common.InputImage.fromBitmap;
import static com.google.mlkit.vision.common.InputImage.fromByteArray;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.EditText;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.content.ContextWrapper;
import android.content.Context;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

/**
 * Created by BRB_LAB on 2016-06-07.
 */
public class Preview extends Thread {
    private final static String TAG = "Preview : ";


    Handler cameraActivityHandler;

    //tts 변수
    private TextToSpeech tts;
    Handler mMainHandler;
    Handler mBackHandler;
    String name;

    //camera2 함수
    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private TextureView mTextureView;
    StreamConfigurationMap map;
    byte[] bytes;
    

    //detect이미지 함수
    Bitmap bigPictureBitmap;
    Bitmap croppedBitmap;

    //server 함수
    String lecture_code = ""; //Handler로 받는 강의코드
    
    public static final byte HEADER_TEXT = 0x08;
    public static final byte HEADER_IMAGE = 0x02;

    private String IP;
    private int PORT;

    private Socket socket;
    private OutputStream os;
    private InputStream is;

    private static final int REQUEST_CAMERA = 40;
    private static final int REQUEST_PERMISSION = 50;

    private static final int HANDLER_MSG_CONNECT = 100;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);

//        ORIENTATIONS.append(Surface.ROTATION_0, 0);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 0);
//        ORIENTATIONS.append(Surface.ROTATION_270, 0);

        /*ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);*/
    }

    // 추가
    public Preview(Context context, TextureView textureView, Handler handler, String IP, int PORT) {
        mContext = context;
        mTextureView = textureView;
        this.cameraActivityHandler = handler;
        this.IP = IP;
        this.PORT = PORT;
    }
    // 추가 끝

    private String getBackFacingCameraId(CameraManager cManager) {
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openCamera() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = getBackFacingCameraId(manager);
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            int permissionCamera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
            if(permissionCamera == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, camera_prof.REQUEST_CAMERA);
            } else {
                manager.openCamera(cameraId, mStateCallback, null);
            }
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureAvailable, width="+width+",height="+height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method stub
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onError");
        }

    };

    protected void startPreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG,"texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setSurfaceTextureListener()
    {
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        setSurfaceTextureListener();
    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPause");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d(TAG, "CameraDevice Close");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }




    public Bitmap byteArrayToBitmap( byte[] $byteArray ) {
        Bitmap bitmap = BitmapFactory.decodeByteArray( $byteArray, 0, $byteArray.length ) ;
        return bitmap;
    }

    protected void takePicture(int rotation) throws CameraAccessException {
        if (null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return");
            return;
        }

        try {
            Size[] jpegSizes = null;
            if (map != null) {
                jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
            }


            int width = 500;
            int height = 500;

            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


//            int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation); //getOrientation(rotation)

            System.out.println("preview내에서의 rotation값 : "+rotation);

            final File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", "pic.jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        System.out.println("dbg3: 저장 전 회전각도는 "+ rotation);
                        save(bytes, rotation);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                            reader.close();
                        }
                    }
                }

                private void save(byte[] bytes, int rotation) throws IOException {

                    System.out.println("dbg1: bytes info: " + bytes);
                    System.out.println("dbg333: save에서 rotation값은 :" + rotation);
                    Bitmap bit = byteArrayToBitmap(bytes);
                    //여기 고침 dbg333 rotateBitmap은 새로추가한 함수
                    //Bitmap rotatebit = rotateBitmap(bit, rotation);

                    Matrix rotateMatrix = new Matrix();

                     rotateMatrix.postRotate((float) rotation + 90);
                    //rotateMatrix.setRotate(rotate, (float) bit.getWidth() / 2, (float) bit.getHeight() / 2);


                    Bitmap sideInversionImg = Bitmap.createBitmap(bit, 0, 0,
                            bit.getWidth(), bit.getHeight(), rotateMatrix, false);


                    System.out.println("dbg1: detectFace Succeed :"+ sideInversionImg);

                    detectFaces(fromBitmap(sideInversionImg,0));
                }
            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //Toast.makeText(mContext, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    startPreview();
                }
            };
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Bitmap rotateBitmap(Bitmap bitmap,int rotate){  //여기 고침 dbg333

        Log.d("TEST", "Image rotate degree= " + rotate);
        Matrix rotateMatrix = new Matrix();

//        rotateMatrix.postRotate((float) rotate);
        rotateMatrix.setRotate(rotate + 90, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);


        Bitmap sideInversionImg = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);

        return sideInversionImg;
    }

    private void detectFaces(InputImage image) {

        // [START set_detector_options]
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();
        // [END set_detector_options]

        // [START get_detector]
        FaceDetector detector = FaceDetection.getClient(options);
        // Or use the default options:
        // FaceDetector detector = FaceDetection.getClient();
        // [END get_detector]

        // [START run_detector]
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_face_info]
                                        System.out.println("dbg1: Founding Faces:"+faces);

                                        for (Face face : faces) {
                                            Rect rectf = face.getBoundingBox();
                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            System.out.println("dbg1 "+ rectf);

                                            bigPictureBitmap = image.getBitmapInternal();

                                            System.out.println("dbg1 Img Width is "+bigPictureBitmap.getWidth() + " Height is" +bigPictureBitmap.getHeight());

                                            //For coordinates location relative to the screen/display
                                            //Bitmap.createScaledBitmap(bigPictureBitmap, image.getWidth(), image.getHeight(), false);
                                            if(rectf.left+rectf.width()>bigPictureBitmap.getWidth()||rectf.top+rectf.height()>bigPictureBitmap.getHeight()) {
                                                System.out.println("오류 얼굴이 사진 외곽에 위치");
                                                System.out.println("bitmapsize" + bigPictureBitmap.getWidth() + "," + bigPictureBitmap.getHeight());
                                                System.out.println("Rect left " + rectf.left + " top " + rectf.top + " width " + rectf.width() + " height " + rectf.height());
                                            }
                                            else {
                                                croppedBitmap = Bitmap.createBitmap(bigPictureBitmap, rectf.left, rectf.top, rectf.width(), rectf.height(), null, false);
                                                //croppedBitmap = Bitmap.createBitmap(bigPictureBitmap , rectf.left-(rectf.width()/2), rectf.top-(rectf.height()/2), rectf.width(), rectf.height(), null, false);
                                                connectServer();
                                            }

                                        }
                                        // [END get_face_info]
                                        // [END_EXCLUDE]
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        System.out.println("dbg fail");
                                    }
                                });
        // [END run_detector]
    }

    public byte[] bitmapToByteArray( Bitmap $bitmap ) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        $bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }

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

    private void connectServer() {
        Log.e("test", "connectServer");
        new Preview.ServerConnectThread().start();
    }

    class ServerConnectThread extends Thread {
        @Override
        public void run() {
            try {
                socket = new Socket();
                SocketAddress addr = new InetSocketAddress(IP, PORT);
                socket.connect(addr);
                os = socket.getOutputStream() ;
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
                sendImage(croppedBitmap);
                System.out.println("이미지 전송 시작");
            }
        }
    };

    private void sendMsg() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lecture_code = "Lq5IZ";
                    byte[] dataMsg = objectToByte(HEADER_TEXT, lecture_code.getBytes("utf-8"));
                    os.write(dataMsg, 0, dataMsg.length);
                    os.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendImage(Bitmap bit) {

        final byte[] imageByteArr = bitmapToByteArray(bit);
        System.out.println("이미지바이트 = " + imageByteArr);

        final String quitMsg = "quit";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = objectToByte(HEADER_IMAGE, imageByteArr);
                    os.write(data);
                    System.out.println("이미지바이트 전송 = " + data);

                    byte[] buffer = new byte[1024];
                    int readByteCount = is.read(buffer);
                    String coverted ="failfind";

                    try {
                        coverted = new String(buffer, 0, readByteCount, "UTF-8");
                    } catch (StringIndexOutOfBoundsException e){
                        System.out.println("dbg1:난 모르겟다!~");
                    }

                    System.out.println("dbg1234 data received:" + coverted);


                    if (coverted.equals("fail")) {
                        System.out.println("이미 출석되었습니다");

                    } else if (coverted.equals("failfind")) {
                        System.out.println("얼굴 인식을 실패하였습니다");
                    } else {
                        name = coverted;
                        System.out.println(coverted + "님 출석되었습니다");

                        Handler mHandler = new Handler(Looper.getMainLooper());
                        String finalCoverted = coverted;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, finalCoverted + "님 출석되었습니다", Toast.LENGTH_SHORT).show();
                                //추가
                                Message msg = handler.obtainMessage();
                                Bundle bundle = new Bundle();
                                bundle.putString("key", finalCoverted);
                                msg.setData(bundle);
                                if(msg == null)
                                    System.out.println("coverted(받은 출석데이터) is Null");
                                //msg.obj = coverted;

                                cameraActivityHandler.sendMessage(msg);
                                //추가 끝


                            }
                        }, 0);


//                    byte[] dataMsg = objectToByte(HEADER_IMAGE, quitMsg.getBytes("utf-8"));
//                    os.write(dataMsg, 0, dataMsg.length);
//                    os.flush();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }






/*
Preview(Handler handler){
    mMainHandler = handler;
}

    public void run(){
        Looper.prepare();
        mBackHandler = new Handler(){
            public void handleMessage(Message msg){
                Message retmsg = new Message();
                retmsg.obj = new String(name);

            }
        };
    }*/

}
