package com.skku.grad;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyService extends Service {
    @Override
    public void onCreate() {
        System.out.println("#service on create");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId ) {
        System.out.println("#service on satrt command");
        if (intent==null) {
            return Service.START_STICKY;
        }
        //processCommand(intent, flags, startId);
        //processCommand2(intent, flags, startId);
        sendMessage("hi~");

        return super.onStartCommand(intent, flags, startId);
    }

    private void processCommand(Intent intent, int flags, int startId) {
        for (int i=0; i<5; i++) {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("#sleep: "+i);
        }
    }

    private void processCommand2(Intent intent, int flags, int startId) {
        for (int i=0; i<10; i++) {
            System.out.println("#processCommand2");
            Intent showInt = new Intent(getApplicationContext(), TTSActivity.class);
            showInt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    |Intent.FLAG_ACTIVITY_SINGLE_TOP
                    |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            showInt.putExtra("temp","hi~");
            startActivity(showInt);
        }
    }

    private void sendMessage(String str){
        System.out.println("#sendMessage");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("message", "This is my first message: "+str);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
