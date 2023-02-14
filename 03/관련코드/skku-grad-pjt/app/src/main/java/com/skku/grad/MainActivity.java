package com.skku.grad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient sFusedLocationClient;

    private LocationCallback sLocationCallback;
    private LocationRequest sLocationRequest;
    private GoogleApiClient sGoogleApiClient;
    private boolean apiconnectionstatus = false ;
    private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY ;
    private double Latitude = 0.0, Longitude = 0.0;
    private static final String TAG = "MainActivity";
    Button realtimegetGPS,metagetGPS ;
    TextView longitude,latitude ;

    //###### add start
    private String[][] orgData = {
            {"01","성신여대","37.5927", "127.0165", "01", "02"},
            {"02","한성대","37.5884", "127.0060", "01", "03"},
            //{"02","한성대","37.5901", "127.0121", "01", "03"}, //### for test
            {"03","혜화","37.5822", "127.0019", "02", "04"},
            {"04","동대문","37.5708", "127.0094", "03", "05"},
            {"05","동대문역사문화공원","37.5653", "127.0079", "04", "06"},
            {"06","충무로","37.5613", "126.9943", "05", "07"},
            {"07","명동","37.5609", "126.9858", "06", "07"},
    };

    private int numDataLeng = orgData.length;
    private double[][] numData = new double[numDataLeng][5];

    private Map id2IndMap = new HashMap();
    private Map ind2IdMap = new HashMap();
    private Map idNameMap = new HashMap();

    private int gcurInd = -1; //global var로 nearby station의 index를 의미함
    private int gnextInd = -1; ////global var로 next station의 index를 의미함
    private double gprevAdjInd1Diff = -1;
    private double gprevAdjInd2Diff = -1;
    TextView tempTV;

    private final double c1 = 0.0015;
    private double deltay = 0;
    private double deltax = 0;
    private static final DecimalFormat df = new DecimalFormat("0.0000");
    private static final DecimalFormat df2 = new DecimalFormat("0.#######");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realtimegetGPS = findViewById(R.id.realtimegetGPS);
        metagetGPS = findViewById(R.id.metagetGPS);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);

        tempTV = findViewById(R.id.textView);

        /*
        realtimegetGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Realtime Activity
                Intent i = new Intent(MainActivity.this, RealtimeActivity.class);
                startActivity(i);
            }
        });
        */

        metagetGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(apiconnectionstatus) {
                    locationSettingsRequest();
                }
            }
        });

        //###
        makeData();
        logTest();
    }

    /**
     * Function to connect googleapiclient
     * */
    private void connectGoogleClient() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
        System.out.println("#connectGoogleClient: "+resultCode);
        if (resultCode == ConnectionResult.SUCCESS) {
            System.out.println("#connectGoogleClient 2: "+resultCode);
            sGoogleApiClient.connect();
        } else {
            int REQUEST_GOOGLE_PLAY_SERVICE = 988;
            googleAPI.getErrorDialog(this, resultCode, REQUEST_GOOGLE_PLAY_SERVICE);
        }
    }

    /**
     * Function to start FusedLocation updates
     */
    @SuppressLint("MissingPermission")
    public void requestLocationUpdate() {
        System.out.println("#requestLocationUpdate");
        //buildGoogleApiClient();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //System.out.println("#requestLocationUpdate in ");

            latitude.setText(getString(R.string.loading));
            longitude.setText(getString(R.string.loading));
            sFusedLocationClient.requestLocationUpdates(sLocationRequest, sLocationCallback, Looper.myLooper());

        }
    }

    /**
     * Build GoogleApiClient and connect
     */
    private synchronized void buildGoogleApiClient() {
        System.out.println("#buildGoogleApiClient");
        sFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                        // Creating a location request
                        sLocationRequest = new LocationRequest();
                        sLocationRequest.setPriority(priority);
                        sLocationRequest.setSmallestDisplacement(0);
                        //sLocationRequest.setNumUpdates(1);
                        sLocationRequest.setNumUpdates(Integer.MAX_VALUE);
                        sLocationRequest.setInterval(5000);


                        // FusedLocation callback
                        sLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(final LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                Latitude = locationResult.getLastLocation().getLatitude();
                                Longitude = locationResult.getLastLocation().getLongitude();


                                if (Latitude == 0.0 && Longitude == 0.0) {
                                    requestLocationUpdate();
                                } else {
                                    // Update Textview
                                    latitude.setText(Double.toString(Latitude));
                                    longitude.setText(Double.toString(Longitude));
                                    //###
                                    //System.out.println("### latitue update");
                                    //String temp = gcurInd + ", " + gnextInd + ", " + df.format(gprevAdjInd1Diff) + ", " + df.format(gprevAdjInd2Diff);
                                    String temp = gcurInd + "";
                                    tempTV.setText(temp);
                                    processData(Latitude, Longitude);
                                }
                            }
                        };

                        // Simple api status check
                        apiconnectionstatus = true ;
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        connectGoogleClient();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(LocationServices.API)
                .build();

        // Connect googleapiclient after build
        connectGoogleClient();
    }

    /**
     * Function to request Location Service Dialog
     */
    private void locationSettingsRequest(){
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(sLocationRequest);
        builder.setAlwaysShow(true);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    // Start FusedLocation if GPS is enabled
                    requestLocationUpdate();
                })
                .addOnFailureListener(e -> {
                    // Show enable GPS Dialog and handle dialog buttons
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                int REQUEST_CHECK_SETTINGS = 214;
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                showLog("Unable to Execute Request");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            showLog("Location Settings are Inadequate, and Cannot be fixed here. Fix in Settings");
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        showLog("Canceled No Thanks");
                    }
                });
    }

    private void showLog(String message) {
        Log.e(TAG, "" + message);
    }

    @Override
    public void onResume(){
        super.onResume();
        buildGoogleApiClient();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        sFusedLocationClient.removeLocationUpdates(sLocationCallback);
    }

    // Handle results of Location Service Dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 214) {
            switch (resultCode){
                case Activity.RESULT_OK:
                {
                    // User enabled GPS start fused location
                    requestLocationUpdate();
                    break;
                }
                case Activity.RESULT_CANCELED:
                {
                    // The user was asked to change settings, but chose not to
                    Toast.makeText(getApplication(), "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
    }

    public void makeData() {
        int ind = 0;
        for(int i = 0; i< numDataLeng; i++) {
            String id = orgData[i][0];
            String name = orgData[i][1];
            String lat = orgData[i][2];
            String longi = orgData[i][3];
            System.out.println("#1: " + id + ", " + name + ", " + lat + ", " + longi);
            id2IndMap.put(id,ind);
            ind2IdMap.put(ind,id);
            idNameMap.put(id, name);

            ind++;
        }

        ind = 0;
        for(int i = 0; i< numDataLeng; i++) {
            String id = orgData[i][0];
            String name = orgData[i][1];
            String lat = orgData[i][2];
            String longi = orgData[i][3];
            String adjId1 = orgData[i][4];
            String adjId2 = orgData[i][5];

            int adjInd1 = -1;
            int adjInd2 = -1;
            if (id2IndMap.get(adjId1) != null) {
                adjInd1 = (Integer) id2IndMap.get(adjId1);
            }
            if (id2IndMap.get(adjId2) != null) {
                adjInd2 = (Integer) id2IndMap.get(adjId2);
            }

            numData[ind][0] = Double.parseDouble(lat);
            numData[ind][1] = Double.parseDouble(longi);
            numData[ind][2] = adjInd1;
            numData[ind][3] = adjInd2;

            String temp = (String)ind2IdMap.get(ind);
            System.out.println("#2: " + ind + ", " + temp + ", " + numData[ind][0] + ", " + numData[ind][1] + ", " + numData[ind][2] + ", " + numData[ind][3]);
            ind++;
        }
    }

    private void processData(double latitude, double longitude) {
        //### test for moving test
        /*
        deltay = deltay-0.0001;
        deltax = deltax-0.0001;
        latitude += deltay;
        longitude += +deltax;
         */

        System.out.println("#processData:," + df2.format(latitude) + ", " + df2.format(longitude));

        //0. check if nearby station or out of station
        int i = 0;
        double diff = 0;
        double tempDiff = 0;
        for(i=0; i<numDataLeng; i++) {
            diff = 0;
            tempDiff = latitude - numData[i][0];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;

            tempDiff = longitude - numData[i][1];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;
            if (i==0) {
                //System.out.println("#diff:," + df.format(latitude) + ", " + df.format(longitude) + ", " + numData[i][0] + ", " + numData[i][1] + ", " + df.format(diff));
            }

            if (diff < c1) {    //means in nearby station
                break;
            }
        }

        //0. get current index, adjacent index1,2
        int curInd = -1;
        int adjInd1 = -1;
        int adjInd2 = -1;
        if (i<numDataLeng) {
            curInd = i;
            Double dbl1 = numData[i][2];
            adjInd1 = dbl1.intValue();
            Double dbl2 = numData[i][3];
            adjInd2 = dbl2.intValue();
        }
        gcurInd = curInd;
        //System.out.println("#current index:," + curInd + ", " + adjInd1 + ", " + adjInd2 + ", " + df.format(diff));

        //0. get direction if going to adjInd1 or adjInd2
        double adjInd1Diff = 0;
        double adjInd2Diff = 0;
        if (curInd > -1) {  //means nearby station
            //get adjInd1Diff
            diff = 0;
            tempDiff = latitude - numData[adjInd1][0];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;

            tempDiff = longitude - numData[adjInd1][1];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;
            adjInd1Diff = diff;

            //get adjInd2Diff
            diff = 0;
            tempDiff = latitude - numData[adjInd2][0];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;

            tempDiff = longitude - numData[adjInd2][1];
            if (tempDiff < 0) {
                tempDiff = tempDiff * -1;
            }
            diff += tempDiff;
            adjInd2Diff = diff;
            //System.out.println("#adjInd1 diff :, " + df.format(latitude) + ", " + df.format(longitude) + ", " + numData[adjInd1][0] + ", " + numData[adjInd1][1] + ", " + df.format(adjInd1Diff));
            //System.out.println("#adjInd2 diff :, " + df.format(latitude) + ", " + df.format(longitude) + ", " + numData[adjInd2][0] + ", " + numData[adjInd2][1] + ", " + df.format(adjInd2Diff));

            if (gprevAdjInd1Diff==-1 && gprevAdjInd1Diff==-1) { //처음으로 nearby station에 진입을 의미함
                //처음 진입하여 gnextInd 값을 구할수 없음. 다음 수신때 진행방향을 예측함
                gnextInd = -1;
            } else { //nearby station에 이미 진입을 의미함
                //0. gnextInd 값 setting: 진행방향으로 다음역 정보를 예측함
                if (gprevAdjInd1Diff > adjInd1Diff) {
                    gnextInd = adjInd1; //adjInd1이 더 가까워졌음을 의미함
                } else {
                    gnextInd = adjInd2;
                }
            }
            gprevAdjInd1Diff = adjInd1Diff;
            gprevAdjInd2Diff = adjInd2Diff;
            //System.out.println("#in  station :, " + gcurInd + ", " + gnextInd);
            System.out.println("#in station :, " + gcurInd);
        } else {    //means out of station
            //...
            gcurInd = -1;   //gcurInd 값은 초기화함: 역을 벗어날 경우 이번역 정보는 지움
            //gnextInd 값은 유지함
            gprevAdjInd1Diff = -1;
            gprevAdjInd2Diff = -1;
            //System.out.println("#out station :, " + gcurInd + ", " + gnextInd);
            System.out.println("#out station :, " + gcurInd);
        }
    }

//log method start
    public void logTest() {
        //Log.d(TAG, " *** onCreate()");
        /*
        String name = "test.txt";
        //에디트 텍스트에서 내용 받아오기
        String content = "hi";
        try {
            //1.(쓰기) 메소드 호출과 동시에 에디트 택스트에서 받은것들 넘겨주기
            writeToFile(name, content);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //
        String content2 = null;
        try {
            //불러올 파일 이름을 던져주며 메소드 실행
            content2 = readFromFile(name);
            System.out.println("### cont: "+content2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String time = format.format(date);

        if ( isExternalStorageWritable() ) {
            //read, write 둘다 가능

            File appDirectory = new File( Environment.getExternalStorageDirectory()+"/MyApp" );
            File logDirectory = new File( appDirectory + "/logs" );
            Log.d(TAG, "*** onCreate() - appDirectory :: "+appDirectory.getAbsolutePath());
            Log.d(TAG, "*** onCreate() - logDirectory :: "+logDirectory.getAbsolutePath());

            //appDirectory 폴더 없을 시 생성
            if ( !appDirectory.exists() ) {
                appDirectory.mkdirs();
            }

            //logDirectory 폴더 없을 시 생성
            if ( !logDirectory.exists() ) {
                logDirectory.mkdirs();
            }

            ///data/data/tafieldscience.realtimefusedlocation/files
            //File logFile = new File( logDirectory, "logcat_" + time + ".txt" );
            File logFile = new File( "/data/data/com.skku.grad/files", "logcat_" + time + ".txt" );
            Log.d(TAG, "*** onCreate() - logFile :: "+logFile);


            //이전 logcat 을 지우고 파일에 새 로그을 씀
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            //read 만 가능
        } else {
            //접근 불가능
        }

    }
    public void writeToFile(String name, String content) throws Exception {
        //2.(쓰기) 자바랑은 다르게 openFileOutput(name, MODE_PRIVATE) 이렇게 사용하는데
        // 받아온 파일이름넣어주고 쉐어드프리퍼런드때 배웠던것처럼 나만 사용하게 하는 모드이다.
        FileOutputStream outputStream = openFileOutput(name, MODE_APPEND);

        //3.(쓰기) OutputStreamWriter 여기에 위에서 파일 이름 설정을 해줌
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        //실제 내용으로 작성하기
        writer.write(content);
        //모든것들 종료 해줌
        writer.flush();
        writer.close();
        outputStream.close();
    }

    //파일을 읽기위한 메소드
    public String readFromFile(String name) throws Exception {
        //2.(읽기) 받아온 이름경로 설정 하고
        FileInputStream fileInputStream = openFileInput(name);
        //3.(읽기) 버퍼에 연동해주기
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        //4.(읽기) 스트링 버퍼 생성
        StringBuffer stringBuffer = new StringBuffer();

        String content = null; // 4.(읽기) 리더에서 라인을 받아오는데 받아올게 없을떄까지 반복
        while ((content = reader.readLine()) != null) {
            stringBuffer.append(content + "\n");
        }
        //사용한것들은 종료
        reader.close();
        fileInputStream.close();
        //5.(읽기)받아온 정보를 다시 리턴해준다
        return stringBuffer.toString();
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

}








