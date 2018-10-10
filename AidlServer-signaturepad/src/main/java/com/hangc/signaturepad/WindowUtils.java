package com.hangc.signaturepad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.github.gcacace.signaturepad.views.SignaturePad;


public class WindowUtils {
    private static final String LOG_TAG = "WindowUtils";
    private static View mView = null;
    private static WindowManager mWindowManager = null;
    private static Context mContext = null;

    public static Boolean isShown = false;

    private SignaturePad signaturePad;

    /**
     * 显示弹出框
     *
     * @param context
     */
    public void showSign(final Context context) {
        if (isShown) {
            return;
        }
        isShown = true;

        // 获取应用的Context
        mContext = context.getApplicationContext();
        // 获取WindowManager
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

        mView = setUpView(context);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // 类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

        // 设置flag

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
        // 不设置这个flag的话，home页的划屏会有问题

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.CENTER;

        mWindowManager.addView(mView, params);

    }

    private View setUpView(Context context) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.view_layout, null);
        signaturePad = (SignaturePad) view.findViewById(R.id.signaturepad);
        return view;
    }

    /**
     * 隐藏弹出框
     */
    public void hidePopupWindow() {
        if (isShown && null != mView) {
            mWindowManager.removeView(mView);
            isShown = false;
        }

    }

    /**
     * 清空内容
     */
    public void clearSign() {
        signaturePad.clear();
    }


    public void saveSign() {
        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
        if (signatureBitmap.getByteCount() > 0) {
            Log.d(LOG_TAG, "不为空:" + signatureBitmap.getByteCount());
        } else {
            Log.d(LOG_TAG, "为空:" + signatureBitmap.getByteCount());
        }

    }


}