package com.example.nearbyplaces;

/**
 * Created by sohan on 21/7/16.
 */

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Implement this class from "Serializable"
 * So that you can pass this class Object to another using Intents
 * Otherwise you can't pass to another actitivy
 */
public class Place implements Serializable {

    @SerializedName("id")
    String mId;

    @SerializedName("name")
    String mName;

    @SerializedName("reference")
    String mReference;

    @SerializedName("icon")
    String mIcon;

    @SerializedName("vicinity")
    String mVicinity;

    @SerializedName("geometry")
    Geometry mGeometry;

    @SerializedName("formatted_address")
    String mFormattedAddress;

    @SerializedName("formatted_phone_number")
    String mFormattedPhoneNo;

    @Override
    public String toString() {
        return mName + " - " + mId + " - " + mReference;
    }

    public static class Geometry implements Serializable {
        @SerializedName("location")
        Location mLocation;

        public Location getLocation() {
            return mLocation;
        }
    }

    public static class Location implements Serializable {
        @SerializedName("lat")
        double mLat;

        @SerializedName("lng")
        double mLng;

        public double getLat() {
            return mLat;
        }

        public double getLng() {
            return mLng;
        }
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getReference() {
        return mReference;
    }

    public String getIcon() {
        return mIcon;
    }

    public Geometry getGeometry() {
        return mGeometry;
    }

    public String getFormattedPhoneNo() {
        return mFormattedPhoneNo;
    }

    public String getFormattedAddress() {
        return mFormattedAddress;
    }

    public String getVicinity() {
        return mVicinity;
    }
}
