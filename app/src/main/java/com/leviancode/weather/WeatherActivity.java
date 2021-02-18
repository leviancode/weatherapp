package com.leviancode.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 *
 * Simple Weather App
 *
 * Takes current user's position or the city name from user
 * and shows 5day / 3hour weather forecast from openweathermap.org
 *
 */

public class WeatherActivity extends AppCompatActivity {
    private TextView mCityTextView;
    private TextView mNowTextView;
    private TextView mTempTextView;
    private TextView mDayTextView;
    private ImageView mIconImageView;
    private WeatherListFragment mListFragment;

    private WeatherDownloader mDownloader;
    private WeatherDataController mWeatherDataController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        createFragment();

        mCityTextView = findViewById(R.id.cityTextView);
        mNowTextView = findViewById(R.id.nowTextView);
        mTempTextView = findViewById(R.id.temp1_TextView);
        mDayTextView = findViewById(R.id.day_textView);
        mIconImageView = findViewById(R.id.icon1_ImageView);

        mDownloader = new WeatherDownloader(this);
        mWeatherDataController = WeatherDataController.getInstance();
        mWeatherDataController.getRequestedCity().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String city) {
                updateUI(city);
            }
        });

        requestAndStoreUserCity();
    }

    private void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_weather, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView =
                (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.city));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                mWeatherDataController.setRequestedCity(query);
                hideKeyboard(searchView);
                searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home){
            requestAndStoreUserCity();
        } else if (id == R.id.menu_refresh){
            mWeatherDataController.setRequestedCity(mCityTextView.getText().toString());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            requestAndStoreUserCity();
        } else {
            Toast.makeText(this, R.string.toast_access_denied, Toast.LENGTH_LONG).show();
        }
    }

    private void requestAndStoreUserCity(){
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
                    String city = address.getLocality();
                    if (city != null) {
                        mWeatherDataController.setRequestedCity(city);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFragment(){
        FragmentManager fm = getSupportFragmentManager();
        mListFragment = new WeatherListFragment();
        fm.beginTransaction()
                    .replace(R.id.fragment_container, mListFragment)
                    .commit();
    }

    private void updateUI(String city){
        List<Weather> weatherList = mWeatherDataController.getForecast(city);

        if (weatherList == null) {
            try {
                weatherList = mDownloader.load(city);
            } catch (IOException e) {
                Toast.makeText(this, R.string.toast_no_internet, Toast.LENGTH_LONG).show();
                return;
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, R.string.toast_incorrect_city, Toast.LENGTH_LONG)
                        .show();
                return;
            }
        }
        mWeatherDataController.setForecast(weatherList);
        setCurrentWeather(weatherList.get(0));
        mListFragment.updateForecast(weatherList);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setCurrentWeather(Weather weather){
        mCityTextView.setText(weather.getCity().toUpperCase());
        mTempTextView.setText(getString(R.string.temp, weather.getTemp()));
        Bitmap icon = weather.getIcon();
        if (icon != null) mIconImageView.setImageBitmap(icon);

        mNowTextView.setVisibility(View.VISIBLE);
        mDayTextView.setVisibility(View.VISIBLE);
    }
}