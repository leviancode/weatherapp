package com.leviancode.weather;

import android.graphics.Bitmap;
import android.text.format.DateFormat;

import java.util.Date;

public class Weather {
    private String mCity;
    private Date mDate;
    private int mTemp;
    private Bitmap mIcon;
    private final String DATE_FORMAT_TIME = "HH:ss";
    private final String DATE_FORMAT_DAY_MONTH = "dd MMM";
    private final String DATE_FORMAT_DAY = "dd";

    public Weather(String city, Date date, int temp, Bitmap icon) {
        mCity = city;
        mDate = date;
        mTemp = temp;
        mIcon = icon;
    }

    public String getCity() {
        return mCity;
    }

    public Date getDate() {
        return mDate;
    }

    public String getTime(){
        return DateFormat.format(DATE_FORMAT_TIME, mDate).toString();
    }

    public int getDayOfMonth(){
        return Integer.parseInt(DateFormat.format(DATE_FORMAT_DAY, mDate).toString());
    }

    public String getDayAndMonth(){
        return DateFormat.format(DATE_FORMAT_DAY_MONTH, mDate).toString();
    }

    public int getTemp() {
        return mTemp;
    }

    public Bitmap getIcon() {
        return mIcon;
    }
}
