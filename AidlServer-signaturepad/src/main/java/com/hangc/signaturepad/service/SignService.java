package com.hangc.signaturepad.service;

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
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.hangc.signaturepad.CallBack;
import com.hangc.signaturepad.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;



public class SignService extends Service {

    private SignaturePad mSignaturePad;

    private boolean isShown = false;

    private WindowManager mWindowManager;

    private static View mView;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 111:
                    removeSign();
            }
        }
    };


    //client 可以通过Binder获取Service实例
    public class MyBinder extends Binder {
        public SignService getService() {
            return SignService.this;
        }
    }

    //通过binder实现调用者client与Service之间的通信
    private MyBinder binder = new MyBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //getRandomNumber是Service暴露出去供client调用的公共方法
    public int getRandomNumber() {

        return new Random().nextInt();
    }

    public void satrtSign(int timeOut, int startX, int endX, int startY, int endY, CallBack callBack) {

        if (isShown) {
            return;
        }

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
        mView = LayoutInflater.from(this).inflate(R.layout.layout, null);
        mSignaturePad = (SignaturePad) mView.findViewById(R.id.signature_pad);
        mSignaturePad.setPenColor(Color.BLACK);
        //mSignaturePad.setBackgroundColor(Color.TRANSPARENT);
        mWindowManager.addView(mView, params);
        isShown = true;

        handler.sendEmptyMessageDelayed(111,timeOut*1000);
    }

    public void clearSign() {
        if (mSignaturePad != null) {
            mSignaturePad.clear();
        }
    }

    public void removeSign() {
        if (isShown && null != mView) {
            mWindowManager.removeView(mView);
            isShown = false;
        }
    }

    public void saveSign(){
        Bitmap bitmap = mSignaturePad.getTransparentSignatureBitmap();
        if(bitmap.getByteCount()>0){
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

            removeSign();
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

}
