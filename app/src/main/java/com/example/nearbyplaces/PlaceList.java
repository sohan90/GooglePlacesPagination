package com.example.nearbyplaces;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sohan on 21/7/16.
 */
public class PlaceList {
    @SerializedName("status")
    String mStatus;

    @SerializedName("results")
    List<Place> mResults;

    @SerializedName("next_page_token")
    String mNextPageToken;

    public String getNextPageToken() {
        return mNextPageToken;
    }

    public List<Place> getResults() {
        return mResults;
    }

    public String getStatus() {
        return mStatus;
    }
}
