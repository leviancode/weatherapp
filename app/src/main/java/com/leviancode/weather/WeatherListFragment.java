package com.leviancode.weather;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
    private List<Weather> mWeatherList;
    private WeatherAdapter mWeatherAdapter;
    private TextView mDayTextView;
    private int mCurrentDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_list, container, false);
        mWeatherRecyclerView = view.findViewById(R.id.weather_recyclerView);

        mDayTextView = Objects.requireNonNull(getActivity()).findViewById(R.id.day_textView);

        mWeatherList = new ArrayList<>();
        mWeatherAdapter = new WeatherAdapter();
        mWeatherRecyclerView.setAdapter(mWeatherAdapter);
        mWeatherRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
        });

        mCurrentDay = Integer.parseInt(DateFormat.format("dd", new Date()).toString());

        return view;
    }

    public void insert(Weather weather) {
        mWeatherList.add(weather);
        mWeatherAdapter.notifyItemInserted(mWeatherList.size() - 1);
    }

    private class WeatherHolder extends RecyclerView.ViewHolder {
        private TextView mTimeTextView;
        private TextView mTempTextView;
        private ImageView mIconImageView;
        private Weather mWeather;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_weather, parent, false));

            mTimeTextView = itemView.findViewById(R.id.time_TextView);
            mTempTextView = itemView.findViewById(R.id.temp_TextView);
            mIconImageView = itemView.findViewById(R.id.icon_ImageView);
        }

        public void bind (Weather weather, int position){
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

    private class WeatherAdapter extends RecyclerView.Adapter<WeatherHolder> {

        @NonNull
        @Override
        public WeatherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new WeatherHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull WeatherHolder holder, int position) {
            Weather weather = mWeatherList.get(position);
            holder.bind(weather, position);
        }

        @Override
        public int getItemCount() {
            return mWeatherList.size();
        }
    }
}
