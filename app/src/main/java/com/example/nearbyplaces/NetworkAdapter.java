package com.example.nearbyplaces;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sohan on 21/7/16.
 */
public class NetworkAdapter {

    private NetworkCallBack mNetworkCallBack;
    private static NetworkAdapter mNetworkAdapter;
    private static final String TAG = NetworkAdapter.class.getSimpleName();

    private NetworkAdapter() {
        //empty constructor;
    }

    public static NetworkAdapter getInstance() {
        if (mNetworkAdapter == null) {
            mNetworkAdapter = new NetworkAdapter();
        }
        return mNetworkAdapter;
    }


    public void getNearbyPlaces(String url, NetworkCallBack callback) {
        mNetworkCallBack = callback;
        new FetchPlaces().execute(url);
    }


    public class FetchPlaces extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return getPlaces(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TextUtils.isEmpty(result)) {
                mNetworkCallBack.onSuccess(result);
            } else {
                mNetworkCallBack.onFailure("error on response");
            }
        }
    }


    private String getPlaces(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            //String contentAsString = readIt(is, len);
            String contentAsString = readInputStream(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public String readInputStream(InputStream entityResponse) {
        InputStreamReader is = new InputStreamReader(entityResponse);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        try {
            String read = br.readLine();

            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public interface NetworkCallBack {
        void onSuccess(String val);

        void onFailure(String error);
    }
}
