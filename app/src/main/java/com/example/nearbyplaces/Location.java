package com.example.nearbyplaces;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Akshay Bhat.
 */
public class Location {

    private Timer mTimer;
    private LocationManager mLocationManager;
    private LocationResult mLocationResult;
    private boolean mGpsEnabled = false;
    private boolean mNetworkEnabled = false;

    public boolean isLocationPermissionEnabled(Context context, LocationResult result) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        mLocationResult = result;
        if(mLocationManager == null)
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!isLocationEnabled(context)) {
            return false;
        }
        //exceptions will be thrown if provider is not permitted.
        try {
            mGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            mNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //don't start listeners if no provider is enabled
        if(!mGpsEnabled && !mNetworkEnabled) {
            return false;
        }

        return true;
    }


    public void requestLocation(){
        if(mGpsEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        }
        if(mNetworkEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        }
        mTimer = new Timer();
        mTimer.schedule(new GetLastLocation(), 10000);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(android.location.Location location) {
            mTimer.cancel();
            mLocationResult.gotLocation(location);
            mLocationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {

        }
        public void onProviderEnabled(String provider) {

        }
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(android.location.Location location) {
            mTimer.cancel();
            mLocationResult.gotLocation(location);
            mLocationManager.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {

        }
        public void onProviderEnabled(String provider) {

        }
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    private class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            android.location.Location netLoc = null;
            android.location.Location gpsLoc = null;
            if(mGpsEnabled) {
                gpsLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if(mNetworkEnabled) {
                netLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            //if there are both values use the latest one
            if(gpsLoc != null && netLoc != null){
                if(gpsLoc.getTime() > netLoc.getTime()) {
                    mLocationResult.gotLocation(gpsLoc);
                } else {
                    mLocationResult.gotLocation(netLoc);
                }
                return ;
            }

            if(gpsLoc != null){
                mLocationResult.gotLocation(gpsLoc);
                return ;
            }
            if(netLoc != null){
                mLocationResult.gotLocation(netLoc);
                return ;
            }
            mLocationResult.gotLocation(null);
        }
    }

    public static abstract class LocationResult {
        public abstract void gotLocation(android.location.Location location);
    }
}
