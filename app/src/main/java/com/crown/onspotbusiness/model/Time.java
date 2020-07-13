package com.crown.onspotbusiness.model;


import com.google.firebase.database.annotations.NotNull;

public class Time {
    public static final String AM = "AM";
    public static final String PM = "PM";

    private int hour;
    private int minute;
    private String zone;

    public Time() {
    }

    public Time(int hour, int minute, String zone) {
        if (hour > 12) hour %= 12;
        else if (hour == 0) hour = 12;

        this.hour = hour;
        this.minute = minute;
        this.zone = zone;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getZone() {
        return zone;
    }

    public boolean isBuggerThan(@NotNull Time time) {
        int hour = time.getHour();
        int thisHour = this.getHour();
        if (this.getZone().equals("PM")) thisHour += 12;
        if (time.getZone().equals("PM")) hour += 12;

        if (thisHour > hour) return true;
        else if (thisHour == hour) return this.getMinute() > time.getMinute();
        return false;
    }
}
