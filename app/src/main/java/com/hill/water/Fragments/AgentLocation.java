package com.hill.water.Fragments;

public class AgentLocation {
    private String agentName;
    private String agentPhone;
    private String date;
    private String placeName;
    private double latitude;
    private double longitude;
    private String agentUid;

    // Default constructor required for Firebase
    public AgentLocation() {
    }

    public AgentLocation(String agentName, String agentPhone, String date, String placeName, double latitude, double longitude, String agentUid) {
        this.agentName = agentName;
        this.agentPhone = agentPhone;
        this.date = date;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.agentUid = agentUid;
    }

    // Getters and Setters
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentPhone() {
        return agentPhone;
    }

    public void setAgentPhone(String agentPhone) {
        this.agentPhone = agentPhone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAgentUid() {
        return agentUid;
    }

    public void setAgentUid(String agentUid) {
        this.agentUid = agentUid;
    }
}
