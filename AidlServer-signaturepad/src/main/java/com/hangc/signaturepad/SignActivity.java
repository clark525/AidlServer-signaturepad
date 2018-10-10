package com.hangc.signaturepad;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.hangc.signaturepad.service.SignService;



public class SignActivity extends Activity {

    private View mFloatLayout;
    private WindowManager mWm;


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

        //LinearLayout linearLayout = new LinearLayout()

        Intent intent = new Intent(this, SignService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

    }

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


    public void satrtSign(View view) {

        service.satrtSign(30, 640, 1920, 360, 1080, new CallBack() {
            @Override
            public void beginAction() {

            }
        });

    }

    public void removeSign(View view) {
        service.removeSign();
    }

    public void clearSign(View view) {
        service.clearSign();
    }

    public void saveSign(View view) {
        service.saveSign();
    }

}
