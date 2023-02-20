package com.skku.grad;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

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

//from url: https://taek2.tistory.com/21
public class LocService extends Service {
    private static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    private static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";
    private static final String ACTION_STOP_ALARM = "stopAlarm";

    private static final int LOCATION_SERVICE_ID = 101;
    private static final String CHANNEL_ID = "loc_service_channel_id";

    private Ringtone ringtone;
    private TextView arrivalTV, nextTV;

    //###
    private String[][] statInfoData = StationInfo.data;
    private int statNumDataLeng = statInfoData.length;
    private double[][] statNumData = new double[statNumDataLeng][5];

    private Map id2IndMap = new HashMap();
    private Map ind2IdMap = new HashMap();
    private Map idNameMap = new HashMap();

    private int gcurInd = -1; //global var로 nearby station의 index를 의미함
    private int gnextInd = -1; //global var로 next station의 index를 의미함
    private int gprevInd = -1; //global var로 next station의 index를 의미함

    private final double c1 = 0.0015;
    private static final DecimalFormat df = new DecimalFormat("0.0000");
    private static final DecimalFormat df2 = new DecimalFormat("0.#######");

    //for simulation mode
    //private String mode = "no-simulation";
    private String mode = "simulation";
    private double[][] simulData = StationInfo.simulData;
    private int simulDataLeng = simulData.length;
    private int simulCnt = -1;

    private int testCnt=0;

    public LocService() {
    }

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

        String data = intent.getStringExtra("data");
        System.out.println("#intent.getStringExtra(data): "+data);

        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_START_LOCATION_SERVICE)) {
                getData();
                //logMethod();
                startLocationService();
                //startLP();
            } else if (action.equals(ACTION_STOP_LOCATION_SERVICE)) {
                stopLocationService();
            } else if (action.equals(ACTION_STOP_ALARM)) {
                stopAlarm();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //from url: https://taek2.tistory.com/21
    private void startLocationService() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        //# interval이 짧게는 조정 않되는것 같음(예:5초이하)
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling  ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());

        startForeground(LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
        stopForeground(true);
        stopSelf();
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                float accuracy = locationResult.getLastLocation().getAccuracy();
                System.out.println("#1 location service latitude, longitude: " + latitude + ", " + longitude+", "+accuracy);

                if (mode.equals("simulation") && simulCnt<simulDataLeng-1) {
                    simulCnt++;
                    latitude = simulData[simulCnt][0];
                    longitude = simulData[simulCnt][1];
                }

                processData(latitude, longitude);
                sendMessage(latitude, longitude);
            }
        }
    };

    //from url: https://goodtogreate.tistory.com/entry/Activity%EC%99%80-Service%EA%B0%84%EC%9D%98-%ED%86%B5%EC%8B%A0
    private void sendMessage(double latitude, double longitude){
        System.out.println("#3 sendMessage");
        Intent intent = new Intent("custom-event-name");
        intent.putExtra("message", "This is location message");
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);

        //set prev,arrival,next station info
        String temp = gprevInd + "";
        if (gprevInd > -1) {
            temp = gprevInd + "  " + statInfoData[gprevInd][1];
        }
        intent.putExtra("prev", temp);

        temp = gcurInd + "";
        if (gcurInd > -1) {
            temp = gcurInd + "  " + statInfoData[gcurInd][1];
        }
        intent.putExtra("arrival", temp);

        temp = gnextInd + "";
        if (gnextInd > -1) {
            temp = gnextInd + "  " + statInfoData[gnextInd][1];
        }
        intent.putExtra("next", temp);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        //for test
        /*
        testCnt++;
        if (testCnt == 3 ) {
            //doNotify(testCnt);
            //startAlarm();
        }
        */
    }

    private void doNotify(int cnt) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("my notification")
                .setContentText("hello")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = cnt;
        notificationManager.notify(notificationId, builder.build());
    }

    private void startAlarm() {
        System.out.println("#startAlarm");
        Uri uriNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uriNotification);
        ringtone.setLooping(false);
        ringtone.play();
    }

    private void stopAlarm() {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    //location provider test
    private LocationManager locationManager;
    private void startLP() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();
        }
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = 0.0;
            double longitude = 0.0;
            if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("#gps: " + Double.toString(latitude )+ '/' + Double.toString(longitude));
                processData(latitude, longitude);
                sendMessage(latitude, longitude);
            }
            if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("#net: " + Double.toString(latitude )+ '/' + Double.toString(longitude));
                processData(latitude, longitude);
                sendMessage(latitude, longitude);
            }
        }

        @Override
        public void onFlushComplete(int requestCode) {
            LocationListener.super.onFlushComplete(requestCode);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            LocationListener.super.onProviderDisabled(provider);
        }
    };

    //################
    public void getData() {
        int ind = 0;
        for(int i = 0; i< statNumDataLeng; i++) {
            String id = statInfoData[i][0];
            String name = statInfoData[i][1];
            String lat = statInfoData[i][2];
            String longi = statInfoData[i][3];
            System.out.println("#1: " + id + ", " + name + ", " + lat + ", " + longi);
            id2IndMap.put(id,ind);
            ind2IdMap.put(ind,id);
            idNameMap.put(id, name);

            ind++;
        }

        ind = 0;
        for(int i = 0; i< statNumDataLeng; i++) {
            String id   = statInfoData[i][0];
            String name = statInfoData[i][1];
            String lat  = statInfoData[i][2];
            String longi = statInfoData[i][3];
            String adjId1 = statInfoData[i][4];
            String adjId2 = statInfoData[i][5];

            int adjInd1 = -1;
            int adjInd2 = -1;
            if (id2IndMap.get(adjId1) != null) {
                adjInd1 = (Integer) id2IndMap.get(adjId1);
            }
            if (id2IndMap.get(adjId2) != null) {
                adjInd2 = (Integer) id2IndMap.get(adjId2);
            }

            statNumData[ind][0] = Double.parseDouble(lat);
            statNumData[ind][1] = Double.parseDouble(longi);
            statNumData[ind][2] = adjInd1;
            statNumData[ind][3] = adjInd2;

            String temp = (String)ind2IdMap.get(ind);
            System.out.println("#2: " + ind + ", " + temp + ", " + statNumData[ind][0] + ", " + statNumData[ind][1] + ", " + statNumData[ind][2] + ", " + statNumData[ind][3]);
            ind++;
        }
    }

    SimpleDateFormat fmt = new SimpleDateFormat("mm:ss");
    Date date;

    private void processData(double latitude, double longitude) {
        date = new Date(System.currentTimeMillis());
        String time = fmt.format(date);
        System.out.println("#2 processData: " + time + "{" + df2.format(latitude) + ", " + df2.format(longitude) + "}");

        //0. check if nearby station or out of station
        int i = 0;
        double diff = 0;
        double tempDiff = 0;
        for(i=0; i< statNumDataLeng; i++) {
            diff = 0;
            tempDiff = latitude - statNumData[i][0];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;

            tempDiff = longitude - statNumData[i][1];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;

            if (diff < c1) {    //means in nearby station
                break;
            }
        }

        //0. get current index, adjacent index1,2
//working
        int curInd = -1;
        if (i< statNumDataLeng) {   //means near station
            curInd = i;
        }

        System.out.println("#ind1: "+curInd+", "+gcurInd+", "+gprevInd+", "+gnextInd);
        if (gcurInd != curInd) { //curInd가 바뀌었으면
            //set gprevInd
            if (curInd != -1) {
                gprevInd = gcurInd;
            }

            //set gcurInd
            if (curInd != -1) {
                gcurInd = curInd;
            }

            //set gnextInd: gprevInd(이전역)과 gcurInd(도착역)으로부터 gnextInd(다음역)을 구함
            if (curInd != -1) {
                Double dbl1 = statNumData[curInd][2];
                int adjInd1 = dbl1.intValue();
                Double dbl2 = statNumData[curInd][3];
                int adjInd2 = dbl2.intValue();
                if (gprevInd == adjInd1) {
                    gnextInd = adjInd2;
                } else if (gprevInd == adjInd2) {
                    gnextInd = adjInd1;
                } else {
                    System.out.println("#gnextInd problem: " + gprevInd + ", " + adjInd1 + ", " + adjInd2);
                    gnextInd = -1;
                    //#! 충무로 경우 보완 필요함: 통과역 인식 못한경우 다음역 처리 보완
                }
            }
        }
        System.out.println("#ind2: "+curInd+", "+gcurInd+", "+gprevInd+", "+", "+gnextInd);
    }

    //log method start
    public void logMethod() {
        //0. file write/read example
        /*
        String name = "test.txt";
        String content = "hi";
        try {
            writeToFile(name, content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String content2 = null;
        try {
            content2 = readFromFile(name);
            //System.out.println("### cont: "+content2);
        } catch (Exception e) {
            e.printStackTrace();
        }
         */

        //2. logcat file write example
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String time = format.format(date);

        if ( isExternalStorageWritable() ) {
            /*
            File appDirectory = new File( Environment.getExternalStorageDirectory()+"/MyApp" );
            //ex)appDirectory :: /storage/emulated/0/MyApp
            Log.d(TAG, "*** onCreate() - appDirectory :: "+appDirectory.getAbsolutePath());
            if ( !appDirectory.exists() ) {
                appDirectory.mkdirs();
            }
             */

            //data/data/com.skku.grad/files
            String logdir = "/data/data/com.skku.grad/files";
            File logFile = new File( logdir, "logcat_" + time + ".txt" );

            //이전 logcat 을 지우고 파일에 새 로그을 씀
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } else if ( isExternalStorageReadable() ) {
            ;
        } else {
            ;
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }

    public void writeToFile(String name, String content) throws Exception {
        FileOutputStream outputStream = openFileOutput(name, MODE_APPEND);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        writer.write(content);
        writer.flush();
        writer.close();
        outputStream.close();
    }

    public String readFromFile(String name) throws Exception {
        FileInputStream fileInputStream = openFileInput(name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        StringBuffer stringBuffer = new StringBuffer();

        String content = null;
        while ((content = reader.readLine()) != null) {
            stringBuffer.append(content + "\n");
        }

        reader.close();
        fileInputStream.close();
        return stringBuffer.toString();
    }

}