package com.hangc.signaturepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.hangc.signaturepad.destroy")){
            Intent sevice = new Intent(context, SignService.class);
            context.startService(sevice);
        }
    }
}
