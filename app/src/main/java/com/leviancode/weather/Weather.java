package com.leviancode.weather;

import android.graphics.Bitmap;
import android.text.format.DateFormat;

import java.util.Date;

public class Weather {
    private String city;
    private Date mDate;
    private int mTemp;
    private Bitmap mIcon;
    private final String DATE_FORMAT = "HH:ss";

    public Weather(String city, Date date, int temp, Bitmap icon) {
        this.city = city;
        mDate = date;
        mTemp = temp;
        mIcon = icon;
    }

    public String getCity() {
        return city;
    }

    public Date getDate() {
        return mDate;
    }

    public String getFormatDate(){
        return DateFormat.format(DATE_FORMAT, mDate).toString();
    }

    public int getTemp() {
        return mTemp;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "city='" + city + '\'' +
                ", mDate=" + DateFormat.format("HH:mm", mDate) +
                ", mTemp=" + mTemp +
                ", mIcon=" + mIcon +
                '}';
    }
}
