package com.skku.grad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TestBroadcastReceiver extends BroadcastReceiver {
    private final static int NOTICATION_ID = 222;
    private Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("###onRecieve");
        Uri uriNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(context, uriNotification);
        //ringtone.play();
        //ringtone.stop();
    }
}
