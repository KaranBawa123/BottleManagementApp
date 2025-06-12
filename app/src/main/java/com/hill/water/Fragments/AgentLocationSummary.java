package com.hill.water.Fragments;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class AgentLocationSummary implements Parcelable {
    private String agentUid;
    private String agentName;
    private String agentPhone;
    private int totalLocations;
    private String lastDate;
    private List<LocationData> locations;

    public AgentLocationSummary(String agentUid, String agentName, String agentPhone, int totalLocations, String lastDate, List<LocationData> locations) {
        this.agentUid = agentUid;
        this.agentName = (agentName != null) ? agentName : "";
        this.agentPhone = (agentPhone != null) ? agentPhone : "";
        this.totalLocations = totalLocations;
        this.lastDate = (lastDate != null) ? lastDate : "N/A";
        this.locations = (locations != null) ? locations : new ArrayList<>();
    }

    // Getters
    public String getAgentUid() {
        return agentUid;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAgentPhone() {
        return agentPhone;
    }

    public int getTotalLocations() {
        return totalLocations;
    }

    public String getLastDate() {
        return lastDate;
    }

    public List<LocationData> getLocations() {
        return locations;
    }

    protected AgentLocationSummary(Parcel in) {
        agentUid = in.readString();
        agentName = in.readString();
        agentPhone = in.readString();
        totalLocations = in.readInt();
        lastDate = in.readString();
        locations = new ArrayList<>();
        in.readList(locations, LocationData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(agentUid);
        dest.writeString(agentName);
        dest.writeString(agentPhone);
        dest.writeInt(totalLocations);
        dest.writeString(lastDate);
        dest.writeList(locations);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AgentLocationSummary> CREATOR = new Creator<AgentLocationSummary>() {
        @Override
        public AgentLocationSummary createFromParcel(Parcel in) {
            return new AgentLocationSummary(in);
        }

        @Override
        public AgentLocationSummary[] newArray(int size) {
            return new AgentLocationSummary[size];
        }
    };
}
