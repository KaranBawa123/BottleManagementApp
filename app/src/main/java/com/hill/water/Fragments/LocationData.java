package com.hill.water.Fragments;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationData implements Parcelable {
    private String agentUid;
    private double latitude;
    private double longitude;
    private String date;
    private String agentName;
    private String agentPhone;
    private String placeName;

    public LocationData() {}

    public LocationData(String agentUid, double latitude, double longitude, String date, String agentName, String agentPhone, String placeName) {
        this.agentUid = (agentUid != null) ? agentUid : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = (date != null) ? date : "N/A";
        this.agentName = (agentName != null) ? agentName : "Unknown Agent";
        this.agentPhone = (agentPhone != null) ? agentPhone : "N/A";
        this.placeName = (placeName != null) ? placeName : "Unknown Location";
    }

    // Getters
    public String getAgentUid() {
        return agentUid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDate() {
        return date;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getPhoneNumber() {
        return agentPhone;
    }

    public String getPlaceName() {
        return placeName;
    }


    protected LocationData(Parcel in) {
        agentUid = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        date = in.readString();
        agentName = in.readString();
        agentPhone = in.readString();
        placeName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(agentUid);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(date);
        dest.writeString(agentName);
        dest.writeString(agentPhone);
        dest.writeString(placeName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationData> CREATOR = new Creator<LocationData>() {
        @Override
        public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        @Override
        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };
}
