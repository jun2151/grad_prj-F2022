package com.skku.grad;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button startBtn, stopAlarmBtn;
    TextView latiTV,longTV ;

    private static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    private static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    private static final String ACTION_STOP_ALARM = "stopAlarm";

    private TextView prevTV, arrivalTV, nextTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("#oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = findViewById(R.id.startBtn);
        stopAlarmBtn = findViewById(R.id.stopAlarmBtn);

        latiTV = findViewById(R.id.latitudeTV);
        longTV = findViewById(R.id.longitudeTV);

        prevTV = findViewById(R.id.prevTV);
        arrivalTV = findViewById(R.id.arrivalTV);
        nextTV = findViewById(R.id.nextTV);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocService.class);
                intent.putExtra("data", "hi~");
                intent.setAction(ACTION_START_LOCATION_SERVICE);
                startService(intent);
            }
        });
        stopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocService.class);
                intent.setAction(ACTION_STOP_ALARM);
                startService(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        System.out.println("#onResume");
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));
    }

    @Override
    protected void onPause() {
        System.out.println("#onPause");
        super.onPause();
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onDestroy() {
        System.out.println("#onDestroy");
        Intent intent = new Intent(getApplicationContext(), LocService.class);
        intent.setAction(ACTION_START_LOCATION_SERVICE);
        stopLocationService();
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            String prev = intent.getStringExtra("prev");
            String arrival = intent.getStringExtra("arrival");
            String next = intent.getStringExtra("next");
            System.out.println("#4 message reciever: "+latitude+", "+longitude+", "+arrival+", "+next);

            // Update Textview
            latiTV.setText(Double.toString(latitude));
            longTV.setText(Double.toString(longitude));
            prevTV.setText(prev);
            arrivalTV.setText(arrival);
            nextTV.setText(next);
        }
    };

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocService.class);
            intent.setAction(ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

}








