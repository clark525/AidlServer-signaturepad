package com.hangc.signaturepad;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;


public class SignService extends Service {

    private final static int CLEAR_SIGNATURE = 0x000;
    private final static int CANCEL_SIGNATUE = 0x111;

    private SignaturePad mSignaturePad;

    private boolean isShown = false;

    private WindowManager mWindowManager;

    private static View mView;


    //Handler handler = new Handler() {
    //    @Override
    //    public void handleMessage(Message msg) {
    //        switch (msg.what){
    //            case 111:
    //                removeSignature();
    //        }
    //    }
    //};

    private IBinder iBinder = new SignAidlInterface.Stub() {

        @Override
        public int satrtSign(int timeOut, int startX, int endX, int startY, int endY, String pngPath, String txtPath, byte[] message) throws RemoteException {
            satrtSignature(timeOut, startX, endX, startY, endY);
            return 0;
        }

        @Override
        public int cancelSign(byte[] message) throws RemoteException {
            cancelSignature();
            return 0;
        }

        @Override
        public int clearSign(byte[] message) throws RemoteException {
            clearSignature();
            return 0;
        }

        @Override
        public int confirmSign(byte[] message) throws RemoteException {
            confirmSignature();
            return 0;
        }

        @Override
        public int getRandom() throws RemoteException {
            return getRandomNumber();
        }
    };

    //
    ////client 可以通过Binder获取Service实例
    //public class MyBinder extends Binder {
    //    public SignService getService() {
    //        return SignService.this;
    //    }
    //}
    //
    ////通过binder实现调用者client与Service之间的通信
    //private MyBinder binder = new MyBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Intent intent = new Intent("com.hangc.signaturepad.destroy");
        sendBroadcast(intent);
        super.onDestroy();
    }

    //getRandomNumber是Service暴露出去供client调用的公共方法
    public int getRandomNumber() {

        return new Random().nextInt();
    }

    private Handler handler = null;

    public void satrtSignature(final int timeOut, final int startX, final int endX, final int startY, final int endY) {

        if (isShown) {
            return;
        }

        final Context mContxt = this;

        // 在UI线程中开启一个子线程
        new Thread(new Runnable() {
            @SuppressLint("HandlerLeak")
            @Override
            public void run() {
                // 在子线程中初始化一个Looper对象
                Looper.prepare();
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        // 把UI线程发送来的消息显示到屏幕上。
                        Log.i("main", "what=" + msg.what + "," + msg.obj);
                        //Toast.makeText(WorkThreadActivity.this, "what="+msg.what+","+msg.obj, Toast.LENGTH_SHORT).show();
                        switch (msg.what) {
                            case CLEAR_SIGNATURE:
                                mSignaturePad.clear();
                                break;
                            case CANCEL_SIGNATUE:
                                mWindowManager.removeView(mView);
                                isShown = false;
                                break;
                        }
                    }
                };

                mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

                WindowManager.LayoutParams mWmParams = new WindowManager.LayoutParams();

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);

                params.format = PixelFormat.RGBA_8888;
                //params.format = PixelFormat.RGBA_8888;
                //params.alpha=0.0f;

                //初始化后不首先获得窗口焦点。不妨碍设备上其他部件的点击、触摸事件。
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                params.width = endX - startX;
                params.height = endY - startY;
                DisplayMetrics dm = getResources().getDisplayMetrics();
                int width = dm.widthPixels / 2; //540
                int heigth = dm.heightPixels / 2;//960
                int halfX = (endX - startX) / 2;
                int halfY = (endY - startY) / 2;
                params.x = startX - width + halfX;
                params.y = startY - heigth + halfY;
                mView = LayoutInflater.from(mContxt).inflate(R.layout.layout, null);
                mSignaturePad = (SignaturePad) mView.findViewById(R.id.signature_pad);
                mSignaturePad.setPenColor(Color.BLACK);
                //mSignaturePad.setBackgroundColor(Color.TRANSPARENT);
                mWindowManager.addView(mView, params);
                isShown = true;

                handler.sendEmptyMessageDelayed(CANCEL_SIGNATUE, timeOut * 1000);

// 把刚才初始化的Looper对象运行起来，循环消息队列的消息
                Looper.loop();


            }

        }).start();

        //mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //
        //WindowManager.LayoutParams mWmParams = new WindowManager.LayoutParams();
        //
        //WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
        //
        //params.format = PixelFormat.RGBA_8888;
        ////params.format = PixelFormat.RGBA_8888;
        ////params.alpha=0.0f;
        //
        ////初始化后不首先获得窗口焦点。不妨碍设备上其他部件的点击、触摸事件。
        //params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //params.width = endX - startX;
        //params.height = endY - startY;
        //DisplayMetrics dm = getResources().getDisplayMetrics();
        //int width = dm.widthPixels / 2; //540
        //int heigth = dm.heightPixels / 2;//960
        //int halfX = (endX - startX) / 2;
        //int halfY = (endY - startY) / 2;
        //params.x = startX - width + halfX;
        //params.y = startY - heigth + halfY;
        //mView = LayoutInflater.from(this).inflate(R.layout.layout, null);
        //mSignaturePad = (SignaturePad) mView.findViewById(R.id.signature_pad);
        //mSignaturePad.setPenColor(Color.BLACK);
        ////mSignaturePad.setBackgroundColor(Color.TRANSPARENT);
        //mWindowManager.addView(mView, params);
        //isShown = true;

        //handler.sendEmptyMessageDelayed(111,timeOut*1000);
    }

    public void clearSignature() {
        if (mSignaturePad != null) {
            //mSignaturePad.clear();
            handler.sendEmptyMessage(CLEAR_SIGNATURE);
        }
    }

    public void cancelSignature() {
        if (isShown && null != mView) {
            //mWindowManager.removeView(mView);
            //isShown = false;
            handler.sendEmptyMessage(CANCEL_SIGNATUE);
        }
    }

    public void confirmSignature() {
        Bitmap bitmap = mSignaturePad.getTransparentSignatureBitmap();
        if (bitmap.getByteCount() > 0) {
            Bitmap signatureBitmap = mSignaturePad.getTransparentSignatureBitmap();
            if (addJpgSignatureToGallery(signatureBitmap)) {
                //Toast.makeText(MainActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
            }
            if (addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())) {
                //Toast.makeText(MainActivity.this, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainActivity.this, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
            }

            cancelSignature();
        }
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public boolean addSvgSignatureToGallery(String signatureSvg) {
        boolean result = false;
        try {
            File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    class LooperThread extends Thread {
        public Handler mHandler;

        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    // process incoming messages here  *
                }
            };
            Looper.loop();
        }


    }
}