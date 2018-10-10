package com.hangc.signaturepad.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hangc.signaturepad.R;
import com.hangc.signaturepad.SignAidlInterface;
import com.hangc.signaturepad.service.SignService;


public class SignActivity extends Activity {

    private View mFloatLayout;
    private WindowManager mWm;

    //标志当前与服务端连接状况的布尔值，false为未连接，true为连接中
    private boolean mBound = false;

    private SignService service = null;

    private boolean isBind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏

        mFloatLayout = LayoutInflater.from(this).inflate(R.layout.layout, null);
        mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);


    }


    private void attempBind() {
        Intent intent = new Intent();
        intent.setAction("com.hangc.signature.aidl");
        intent.setPackage("com.hangc.signaturepad");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void satrtSign(View view) {
        String message = null;
        byte[] msg = new byte[1024];
        try {
            mAidl.satrtSign(30, 640, 1920, 360, 1080, "cc", "dd", msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //new Thread(new Runnable() {
        //    @Override
        //    public void run() {
        //        byte[] msg = new byte[1024];
        //        try {
        //            mAidl.satrtSign(30, 640, 1920, 360, 1080, "cc", "dd", msg);
        //        } catch (RemoteException e) {
        //            e.printStackTrace();
        //        }
        //    }
        //}).start();
    }

    public void removeSign(View view) {
    }

    public void clearSign(View view) {
    }

    public void saveSign(View view) {
        try {
            int i = mAidl.getRandom();
            Toast.makeText(this,i+"--",Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            attempBind();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private SignAidlInterface mAidl = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //连接后拿到 Binder，转换成 AIDL，在不同进程会返回个代理
            mAidl = SignAidlInterface.Stub.asInterface(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAidl = null;
            mBound = false;
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            isBind = true;
            SignService.MyBinder myBinder = (SignService.MyBinder) binder;
            service = myBinder.getService();
            Log.i("Kathy", "ActivityB - onServiceConnected");
            int num = service.getRandomNumber();
            Log.i("Kathy", "ActivityB - onServiceConnected:" + num);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            Log.i("Kathy", "ActivityB - onServiceDisconnected");
        }
    };


    //public void satrtSign(View view) {
    //
    //    service.satrtSign(30, 640, 1920, 360, 1080, new CallBack() {
    //        @Override
    //        public void beginAction() {
    //
    //        }
    //    });
    //
    //}
    //
    //public void removeSign(View view) {
    //    service.removeSign();
    //}
    //
    //public void clearSign(View view) {
    //    service.clearSign();
    //}
    //
    //public void saveSign(View view) {
    //    service.saveSign();
    //}

}
