package com.gpsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/*
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
 */

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private List<String > listProviders;
    private TextView tvGpsEnable, tvNetworkEnable, tvPassiveEnable, tvGpsLatitude, tvGpsLongitude;
    private TextView tvNetworkLatitude, tvNetworkLongitude, tvPassiveLatitude, tvPassivekLongitude;
    private String TAG = "LocationProvider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGpsEnable = (TextView)findViewById(R.id.tvGpsEnable);
        tvNetworkEnable = (TextView)findViewById(R.id.tvNetworkEnable);
        tvPassiveEnable = (TextView)findViewById(R.id.tvPassiveEnable);
        tvGpsLatitude = (TextView)findViewById(R.id.tvGpsLatitude);
        tvGpsLongitude = (TextView)findViewById(R.id.tvGpsLongitude);
        tvNetworkLatitude = (TextView)findViewById(R.id.tvNetworkLatitude);
        tvNetworkLongitude = (TextView)findViewById(R.id.tvNetworkLongitude);
        tvPassiveLatitude = (TextView)findViewById(R.id.tvPassiveLatitude);
        tvPassivekLongitude = (TextView)findViewById(R.id.tvPassivekLongitude);

        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();
            Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
            tvGpsLatitude.setText(":: " + Double.toString(lat ));
            tvGpsLongitude.setText((":: " + Double.toString(lng)));
        }
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();
            Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
            tvNetworkLatitude.setText(":: " + Double.toString(lat ));
            tvNetworkLongitude.setText((":: " + Double.toString(lng)));
        }

        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnownLocation != null) {
            double lng = lastKnownLocation.getLongitude();
            double lat = lastKnownLocation.getLatitude();
            Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
            tvPassiveLatitude.setText(":: " + Double.toString(lat ));
            tvPassivekLongitude.setText((":: " + Double.toString(lng)));
        }

        listProviders = locationManager.getAllProviders();
        boolean [] isEnable = new boolean[3];
        for(int i=0; i<listProviders.size();i++) {
            if(listProviders.get(i).equals(LocationManager.GPS_PROVIDER)) {
                isEnable[0] = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                tvGpsEnable.setText(": " +  String.valueOf(isEnable[0]));

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else if(listProviders.get(i).equals(LocationManager.NETWORK_PROVIDER)) {
                isEnable[1] = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                tvNetworkEnable.setText(": " +  String.valueOf(isEnable[1]));

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            } else if(listProviders.get(i).equals(LocationManager.PASSIVE_PROVIDER)) {
                isEnable[2] = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
                tvPassiveEnable.setText(": " +  String.valueOf(isEnable[2]));

                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
            }

        }

        Log.d(TAG, listProviders.get(0) + '/' + String.valueOf(isEnable[0]));
        Log.d(TAG, listProviders.get(1) + '/' + String.valueOf(isEnable[1]));
        Log.d(TAG, listProviders.get(2) + '/' + String.valueOf(isEnable[2]));
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {

        double latitude = 0.0;
        double longitude = 0.0;

        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvGpsLatitude.setText(": " + Double.toString(latitude ));
            tvGpsLongitude.setText((": " + Double.toString(longitude)));
            Log.d(TAG + " GPS : ", Double.toString(latitude )+ '/' + Double.toString(longitude));
        }

        if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvNetworkLatitude.setText(": " + Double.toString(latitude ));
            tvNetworkLongitude.setText((": " + Double.toString(longitude)));
            Log.d(TAG + " NETWORK : ", Double.toString(latitude )+ '/' + Double.toString(longitude));
        }

        if(location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            tvPassiveLatitude.setText(": " + Double.toString(latitude ));
            tvPassivekLongitude.setText((": " + Double.toString(longitude)));
            Log.d(TAG + " PASSIVE : ", Double.toString(latitude )+ '/' + Double.toString(longitude));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없을 경우 최초 권한 요청 또는 사용자에 의한 재요청 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        //locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 2000, 0, this);
    }
}
