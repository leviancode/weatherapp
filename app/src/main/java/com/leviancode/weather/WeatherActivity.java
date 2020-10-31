package com.leviancode.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 *
 * Weather App
 *
 * Takes the city name from user and shows the today weather from openweathermap.org
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
    private TableRow mTimeTableRow;
    private TableRow mIconTableRow;
    private TableRow mTempTableRow;

    private List<Weather> mWeatherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mCityTextView = findViewById(R.id.cityTextView);
        mNowTextView = findViewById(R.id.nowTextView);
        mTempTextView = findViewById(R.id.temp1_TextView);
        mDayTextView = findViewById(R.id.day_textView);
        mCityEditText = findViewById(R.id.cityEditText);
        mShowWeatherButton = findViewById(R.id.showWeather_Button);
        mIconImageView = findViewById(R.id.icon1_ImageView);

        mTimeTableRow = findViewById(R.id.time_TableRow);
        mIconTableRow = findViewById(R.id.icon_TableRow);
        mTempTableRow = findViewById(R.id.temp_TableRow);
        mWeatherList = new ArrayList<>();

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
    }

    public void onShowWeatherButtonClick(View View){
        if (isNetworkAvailable()) {
            String city = mCityEditText.getText().toString().trim();
            String readyApiCall = String.format(API_CALL, city);
            new WeatherDataDownloader().execute(readyApiCall);

            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(mCityEditText.getWindowToken(), 0);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private List<Weather> getForecastAndUpdateUI(String json) throws JSONException {
        List<Weather> result = new ArrayList<>();
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
            Weather weather = new Weather(city,date, temp, icon);
            result.add(weather);
            if (i < 5){
                setForecast(weather, i);
            }
        }
        return result;
    }

    private void setCurrentWeather(Weather weather){
        mCityTextView.setText(weather.getCity());
        mTempTextView.setText(getString(R.string.temp, weather.getTemp()));
        Bitmap icon = weather.getIcon();
        if (icon != null) mIconImageView.setImageBitmap(icon);

        mNowTextView.setVisibility(View.VISIBLE);
        mDayTextView.setVisibility(View.VISIBLE);
    }

    private void setForecast(Weather weather, int index){
        if (index == 0) {
            setCurrentWeather(weather);
        } else {
            ((TextView)mTimeTableRow.getChildAt(index-1)).setText(weather.getFormatDate());
            ((ImageView)mIconTableRow.getChildAt(index-1)).setImageBitmap(weather.getIcon());
            ((TextView)mTempTableRow.getChildAt(index-1)).setText(getString(R.string.temp, weather.getTemp()));
        }
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
                mWeatherList = getForecastAndUpdateUI(json);
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