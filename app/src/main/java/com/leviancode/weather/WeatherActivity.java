package com.leviancode.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 *
 * Simple Weather App
 *
 * Takes current user's position or the city name from user
 * and shows 5day / 3hour weather forecast from openweathermap.org
 *
 */

public class WeatherActivity extends AppCompatActivity {
    private final String API_CALL = "http://api.openweathermap.org/data/2.5/forecast?q=%s&units=metric&appid=a37138f5a06c41c860095d6f04d8c702";
    private final String API_ICON_CALL = "http://openweathermap.org/img/wn/%s@2x.png";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.getDefault());

    private TextView mCityTextView;
    private TextView mNowTextView;
    private TextView mTempTextView;
    private TextView mDayTextView;
    private EditText mCityEditText;
    private ImageView mIconImageView;
    private Button mShowWeatherButton;
    private WeatherListFragment mWeatherList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mCityTextView = findViewById(R.id.cityTextView);
        mNowTextView = findViewById(R.id.nowTextView);
        mTempTextView = findViewById(R.id.temp1_TextView);
        mDayTextView = findViewById(R.id.day_textView);
        mCityEditText = findViewById(R.id.cityEditText);
        mShowWeatherButton = findViewById(R.id.showWeather_Button);
        mIconImageView = findViewById(R.id.icon1_ImageView);

        mCityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) mShowWeatherButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        createFragment();
        loadWeather(getCurrentCity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            loadWeather(getCurrentCity());
        }
    }

    private String getCurrentCity(){
        String result = "";

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);

                if (addressList != null && addressList.size() > 0){
                    Address address = addressList.get(0);
                    result = address.getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void createFragment(){
        FragmentManager fm = getSupportFragmentManager();
        mWeatherList = new WeatherListFragment();
        fm.beginTransaction()
                    .replace(R.id.fragment_container, mWeatherList)
                    .commit();
    }

    public void onShowWeatherButtonClick(View View){
        if (isNetworkAvailable()) {
            String city = mCityEditText.getText().toString().trim();
            loadWeather(city);

            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mCityEditText.getWindowToken(), 0);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private void loadWeather(String city){
        String readyApiCall = String.format(API_CALL, city);
        new WeatherDataDownloader().execute(readyApiCall);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setForecast(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        String city = jsonObject.getJSONObject("city").getString("name");
        JSONArray jsonArray = jsonObject.getJSONArray("list");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonPart = jsonArray.getJSONObject(i);

            int temp = (int)Math.round(Double.parseDouble(jsonPart.getJSONObject("main").getString("temp")));

            String iconId = jsonPart.getJSONArray("weather").getJSONObject(0).getString("icon");
            Bitmap icon = null;
            Date date = null;
            try {
                date = dateFormatter.parse(jsonPart.getString("dt_txt"));
                icon = new IconDownloader().execute(iconId).get();
            } catch (ExecutionException | InterruptedException | ParseException e) {
                e.printStackTrace();
            }
            Weather weather = new Weather(city, date, temp, icon);
            if (i == 0){
                setCurrentWeather(weather);
            } else {
                mWeatherList.insert(weather);
            }
        }
    }

    private void setCurrentWeather(Weather weather){
        mCityTextView.setText(weather.getCity());
        mTempTextView.setText(getString(R.string.temp, weather.getTemp()));
        Bitmap icon = weather.getIcon();
        if (icon != null) mIconImageView.setImageBitmap(icon);

        mNowTextView.setVisibility(View.VISIBLE);
        mDayTextView.setVisibility(View.VISIBLE);
    }

    private class WeatherDataDownloader extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... apiCall) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(apiCall[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                int data = reader.read();

                while (data != -1){
                    result.append((char) data);
                    data = reader.read();
                }

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);

            try {
                setForecast(json);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(WeatherActivity.this, "Incorrect city", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private class IconDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... iconIds) {
            Bitmap bitmap = null;
            try {
                String iconUrl = String.format(API_ICON_CALL, iconIds[0]);
                URL url = new URL(iconUrl);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}