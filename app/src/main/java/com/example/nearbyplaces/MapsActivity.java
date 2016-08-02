package com.example.nearbyplaces;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements NetworkAdapter.NetworkCallBack, View.OnClickListener {
    private static final String NEARBY_PLACES_BASE_API = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String OK = "OK";
    private static final String FILE_NAME = "NearByPlaces";
    private android.location.Location mLocation;
    private List<PlaceList> mPlaceList = new ArrayList<>();
    private List<String> mNameList = new ArrayList<>();
    private float mRadius = 0;

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
        initUi();
        //DialogUtils.showProgress(this, "getting location");
        // startLocation();
    }

    private void initUi() {
        findViewById(R.id.search).setOnClickListener(this);
        findViewById(R.id.download).setOnClickListener(this);
        findViewById(R.id.show).setOnClickListener(this);

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
        DialogUtils.showProgress(this, "getting nearby places");
        String url = constructUrl(nextPageToken);
        Log.d(TAG, "url " + url);
        NetworkAdapter.getInstance().getNearbyPlaces(url, this);
    }

    private String constructUrl(String nextPageToken) {
        String url = NEARBY_PLACES_BASE_API + "key=" + getString(R.string.google_maps_key) + "&" + "location="
                + mLocation.getLatitude() + "," + mLocation.getLongitude() + "&" + "radius=" + getRadius();

        if (!TextUtils.isEmpty(nextPageToken)) {
            url = url + "&" + "pagetoken=" + nextPageToken;
        }
        return url;

    }

    private float getRadius() {
        return mRadius;
    }

    private void setRadius(float radius) {
        mRadius = radius;
    }


    @Override
    public void onSuccess(String val) {
        try {
            PlaceList placeList = (PlaceList) parseStringToObject(val, PlaceList.class);
            if (placeList.getStatus().equals(OK)) {
                mPlaceList.add(placeList);
                final String nextPageToken = placeList.getNextPageToken();
                if (TextUtils.isEmpty(nextPageToken)) {
                    Log.d(TAG, "List Size" + mPlaceList.size());
                    showPlacesInUi();
                } else { // pagination
                    fetchNearByPlaces(nextPageToken);
                    Log.d(TAG, "PAGINATION" + val);
                }
            }
        } catch (JsonSyntaxException e) {
            DialogUtils.dismissProgress();
            diableDownloadButton();
            e.printStackTrace();
            Log.d(TAG, "Json Exceptions" + val);
        }


    }

    @Override
    public void onFailure(String error) {
        DialogUtils.dismissProgress();
        diableDownloadButton();
        Log.d(TAG, "error");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                EditText lat = (EditText) findViewById(R.id.lat);
                EditText longi = (EditText) findViewById(R.id.longitude);
                EditText radius = (EditText)findViewById(R.id.radius);
                String radStr =  radius.getText().toString();
                setRadius(Float.valueOf(radStr));
                String latStr = lat.getText().toString();
                String longStr = longi.getText().toString();

                if (!TextUtils.isEmpty(latStr) && !TextUtils.isEmpty(longStr)) {
                    mLocation = new android.location.Location("");
                    mLocation.setLatitude(Double.parseDouble(lat.getText().toString()));
                    mLocation.setLongitude(Double.parseDouble(longi.getText().toString()));
                    mPlaceList.clear();
                    fetchNearByPlaces(null);
                } else {
                    Toast.makeText(MapsActivity.this, "invalid latitude or longitude", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.download:
                DialogUtils.showProgress(this, "saving file...");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SaveFile saveFile = new SaveFile();
                        saveFile.execute();
                    }
                }, 2000);

                break;

            case R.id.show:
                openSavedFile();

        }

    }

    private void openSavedFile() {
        viewTxtFile(new File(getAlbumStorageDir(FILE_NAME), FILE_NAME + ".txt"));
    }

    public class SaveFile extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... urls) {

            return saveToSdCard();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            DialogUtils.dismissProgress();
            if (result) {
                Toast.makeText(MapsActivity.this, "File saved successfully ", Toast.LENGTH_SHORT).show();
                findViewById(R.id.show).setEnabled(true);
            } else {
                Toast.makeText(MapsActivity.this, "File fail to save ", Toast.LENGTH_SHORT).show();
                findViewById(R.id.show).setEnabled(false);
            }

        }
    }

    private void showPlacesInUi() {
        final LinearLayout container = (LinearLayout) findViewById(R.id.lyt_id);
        container.removeAllViews();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.dismissProgress();
                mNameList.clear();
                for (PlaceList list : mPlaceList) {
                    for (Place place : list.getResults()) {
                        TextView textView = new TextView(MapsActivity.this);
                        textView.setText(place.getName());
                        container.addView(textView);
                        mNameList.add(place.getName());

                    }
                }
                enableDownloadButton();


            }


        });


    }

    private void enableDownloadButton() {
        findViewById(R.id.download).setEnabled(true);
    }

    private void diableDownloadButton() {
        findViewById(R.id.download).setEnabled(false);
    }

    // Method for opening a txt file
    private void viewTxtFile(File filePath) {
        Uri uri = Uri.fromFile(filePath);
        Intent txtIntent = new Intent(Intent.ACTION_VIEW);
        txtIntent.setDataAndType(uri, "text/plain");
        try {
            startActivity(txtIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Can't read txt file", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean saveToSdCard() {
        File file = new File(getAlbumStorageDir(FILE_NAME), FILE_NAME + ".txt");
        PrintWriter write = null;
        boolean hasSaved;
        try {
            write = new PrintWriter(new FileWriter(file));
            for (String name : mNameList) {
                write.println(name);

            }
            hasSaved = true;
        } catch (IOException e) {
            e.printStackTrace();
            hasSaved = false;
        } finally {
            if (write != null) {
                write.flush();
                write.close();
            }

        }
        return hasSaved;
    }


    public static Object parseStringToObject(String response, Type type) throws JsonSyntaxException {
        Object responseObject = null;
        Gson gson = new Gson();
        responseObject = gson.fromJson(response, type);
        return responseObject;
    }


    public File getAlbumStorageDir(String albumName) {
        File file;
        if (hasSdCardMounted()) {
            // Get the directory for the user's public pictures directory.
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), albumName);
            if (!file.mkdirs()) {
                Log.e("SignaturePad", "Directory not created");
            }
        } else { // get internal storage path
            file = new File(getFilesDir(), albumName);
            if (!file.mkdirs()) {
                Log.e("SignaturePad", "Internal path Directory not created");
            }

        }
        return file;
    }

    /**
     * @return true if it has got sdcard or false
     */
    private static boolean hasSdCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
