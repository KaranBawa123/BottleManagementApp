package com.hill.water.Fragments;

public class DataItem {
    private String date;
    private long oneLitreCount;
    private long twoFiftyMlCount;
    private long fiveHundredMlCount;
    private long fiveLitreCount;

    public DataItem(String date, long oneLitreCount, long twoFiftyMlCount, long fiveHundredMlCount, long fiveLitreCount) {
        this.date = date;
        this.oneLitreCount = oneLitreCount;
        this.twoFiftyMlCount = twoFiftyMlCount;
        this.fiveHundredMlCount = fiveHundredMlCount;
        this.fiveLitreCount = fiveLitreCount;
    }

    public String getDate() {
        return date;
    }

    public long getOneLitreCount() {
        return oneLitreCount;
    }

    public long getTwoFiftyMlCount() {
        return twoFiftyMlCount;
    }

    public long getFiveHundredMlCount() {
        return fiveHundredMlCount;
    }

    public long getFiveLitreCount() {
        return fiveLitreCount;
    }
}
