package com.leviancode.weather;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class WeatherListFragment extends Fragment{
    private RecyclerView mWeatherRecyclerView;
    private WeatherAdapter mWeatherAdapter;
    private TextView mDayTextView;
    private int mCurrentDay;
    private WeatherDataController mWeatherDataController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_list, container, false);
        mWeatherRecyclerView = view.findViewById(R.id.weather_recyclerView);

        mDayTextView = Objects.requireNonNull(getActivity()).findViewById(R.id.day_textView);
        mWeatherDataController = WeatherDataController.getInstance();

        mWeatherAdapter = new WeatherAdapter();
        mWeatherRecyclerView.setAdapter(mWeatherAdapter);

        mCurrentDay = Integer.parseInt(DateFormat.format("dd", new Date()).toString());

        return view;
    }

    public void updateForecast(List<Weather> weatherList){
        mWeatherAdapter.setWeatherList(weatherList);
    }

    private class WeatherAdapter extends RecyclerView.Adapter<WeatherHolder> {
        private List<Weather> mWeatherList;

        public WeatherAdapter() {
            mWeatherList = new ArrayList<>();
        }

        public void setWeatherList(List<Weather> weatherList) {
            mWeatherList = weatherList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public WeatherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new WeatherHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull WeatherHolder holder, int position) {
            if (position < getItemCount()-1) {
                Weather weather = mWeatherList.get(position+1);
                holder.bind(weather);
            }
        }

        @Override
        public int getItemCount() {
            return mWeatherList.size()-1;
        }
    }

    private class WeatherHolder extends RecyclerView.ViewHolder {
        private final TextView mTimeTextView;
        private final TextView mTempTextView;
        private final ImageView mIconImageView;
        private Weather mWeather;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_weather, parent, false));

            mTimeTextView = itemView.findViewById(R.id.time_TextView);
            mTempTextView = itemView.findViewById(R.id.temp_TextView);
            mIconImageView = itemView.findViewById(R.id.icon_ImageView);
        }

        public void bind (Weather weather){
            mWeather = weather;

            mTimeTextView.setText(mWeather.getTime());
            mTempTextView.setText(getString(R.string.temp, mWeather.getTemp()));
            mIconImageView.setImageBitmap(mWeather.getIcon());

            if (weather.getTime().equals("09:00") || weather.getTime().equals("21:00")){
                if (weather.getDayOfMonth() == mCurrentDay){
                    mDayTextView.setText(R.string.today);
                } else if (weather.getDayOfMonth() == mCurrentDay + 1){
                    mDayTextView.setText(R.string.tomorrow);
                } else {
                    mDayTextView.setText(weather.getDayAndMonth());
                }
            }
        }

    }

}
