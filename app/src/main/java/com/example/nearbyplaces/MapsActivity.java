package com.example.nearbyplaces;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements NetworkAdapter.NetworkCallBack {
    private static final String NEARBY_PLACES_BASE_API = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final float RADIUS = 200;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String OK = "OK";
    private static final String FILE_NAME = "NearByPlaces";
    private android.location.Location mLocation;
    private List<PlaceList> mPlaceListList = new ArrayList<>();
    private List<String> mNameList = new ArrayList<>();

    public Location.LocationResult locationResult = new Location.LocationResult() {

        @Override
        public void gotLocation(final android.location.Location location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLocation == null) {
                        mLocation = location;
                        Log.d(TAG, "Location ");
                        if (location != null) {
                            fetchNearByPlaces("");
                        } else {
                            Toast.makeText(MapsActivity.this, "wait untill fetching location complete", Toast.LENGTH_SHORT).show();
                            DialogUtils.dismissProgress();

                        }
                    }
                }
            });

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DialogUtils.showProgress(this, "getting location");
        startLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void startLocation() {
        final Location location = new Location();
        boolean isEnabled = location.isLocationPermissionEnabled(this, locationResult);
        if (isEnabled) {
            location.requestLocation();
        } else {
            //error message;
            Toast.makeText(this, "enable location", Toast.LENGTH_SHORT).show();

        }
    }

    private void fetchNearByPlaces(String nextPageToken) {
        String url = constructUrl(nextPageToken);
        Log.d(TAG, "url " + url);
        NetworkAdapter.getInstance().getNearbyPlaces(url, this);
    }

    private String constructUrl(String nextPageToken) {
        String url = NEARBY_PLACES_BASE_API + "key=" + getString(R.string.google_maps_key) + "&" + "location="
                + mLocation.getLatitude() + "," + mLocation.getLongitude() + "&" + "radius=" + RADIUS;

        if (!TextUtils.isEmpty(nextPageToken)) {
            url = url + "&" + "pagetoken=" + nextPageToken;
        }
        return url;

    }


    @Override
    public void onSuccess(String val) {
        try {
            PlaceList placeList = (PlaceList) parseStringToObject(val, PlaceList.class);
            if (placeList.getStatus().equals(OK)) {
                mPlaceListList.add(placeList);
                final String nextPageToken = placeList.getNextPageToken();
                if (TextUtils.isEmpty(nextPageToken)) {
                    Log.d(TAG, "List Size" + mPlaceListList.size());
                    showPlacesInUi();
                } else { // pagination
                    fetchNearByPlaces(nextPageToken);
                    Log.d(TAG, "PAGINATION" + val);
                    //l
                }
            }
        } catch (JsonSyntaxException e) {
            DialogUtils.dismissProgress();
            e.printStackTrace();
            Log.d(TAG, "Json Exceptions" + val);
        }


    }

    @Override
    public void onFailure(String error) {
        DialogUtils.dismissProgress();
        Log.d(TAG, "error");
    }

    public class SaveFile extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {

            return "";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private void showPlacesInUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.dismissProgress();
                for (PlaceList list : mPlaceListList) {
                    for (Place place : list.getResults()) {
                        LinearLayout lyt = (LinearLayout) findViewById(R.id.lyt_id);
                        TextView textView = new TextView(MapsActivity.this);
                        textView.setText(place.getName());
                        lyt.addView(textView);
                        mNameList.add(place.getName());

                    }
                }  saveToSdCard();

            }


        });


    }

    private void saveToSdCard() {
        File file = new File(getAlbumStorageDir(FILE_NAME, this), FILE_NAME + ".txt");
        PrintWriter write = null;
        try {
            write = new PrintWriter(new FileWriter(file));
            for (String name : mNameList) {
                write.println(name);

            }
            Toast.makeText(this, "File saved successfully ", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "File fail to save ", Toast.LENGTH_SHORT).show();
        }
        finally {
            if (write != null) {
                write.flush();
                write.close();
            }

        }

    }


    public static Object parseStringToObject(String response, Type type) throws JsonSyntaxException {
        Object responseObject = null;
        Gson gson = new Gson();
        responseObject = gson.fromJson(response, type);
        return responseObject;
    }


    public static File getAlbumStorageDir(String albumName, Context context) {
        File file;
        if (hasSdCardMounted()) {
            // Get the directory for the user's public pictures directory.
            file  = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), albumName);
            if (!file.mkdirs()) {
                Log.e("SignaturePad", "Directory not created");
            }
        } else { // get internal storage path
            file  = new File(context.getFilesDir(), albumName);
            if (!file.mkdirs()) {
                Log.e("SignaturePad", "Internal path Directory not created");
            }

        }
        return file;
    }

    /**
     *
     * @return true if it has got sdcard or false
     */
    private static boolean hasSdCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
