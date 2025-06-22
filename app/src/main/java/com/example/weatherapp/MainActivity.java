package com.example.weatherapp;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.weatherapp.WeatherApi;

import java.text.SimpleDateFormat;
import java.util.Date;

// https://api.openweathermap.org/data/2.5/weather?q=Delhi&appid=39540792dedf1579c5821d2dec7dbd8f

public class MainActivity extends AppCompatActivity {
    SearchView searchView;
    TextView cityNameTextView, tempTextView, weatherTextView, maxTempTextView, minTempTextView, humidityTextView, windSpeedTextView, sunriseTextView, sunsetTextView, seaLevelTextView;

    // API helper
    WeatherApi weatherApi;

    String API_KEY = "39540792dedf1579c5821d2dec7dbd8f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Bind all views
        searchView = findViewById(R.id.searchView);
        cityNameTextView = findViewById(R.id.cityName);
        tempTextView = findViewById(R.id.temp);
        weatherTextView = findViewById(R.id.weather);
        maxTempTextView = findViewById(R.id.max_temp);
        minTempTextView = findViewById(R.id.min_temp);
        humidityTextView = findViewById(R.id.humidity);
        windSpeedTextView = findViewById(R.id.wind_speed);
        sunriseTextView = findViewById(R.id.sunrise);
        sunsetTextView = findViewById(R.id.sunset);
        seaLevelTextView = findViewById(R.id.sea_level);

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherApi.class);

        // Setup SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchWeather(query.trim());
                searchView.clearFocus(); // close keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }
    private String formatUnixTime(long unixSeconds) {
        Date date = new java.util.Date(unixSeconds * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        return sdf.format(date);
    }


    private void fetchWeather(String cityName) {
        Call<WeatherResponse> call = weatherApi.getWeatherByCity(cityName, API_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse data = response.body();

                    // Bind API data to TextViews
                    cityNameTextView.setText(data.getName());
                    tempTextView.setText(data.getMain().getTemp() + " °C");
                    weatherTextView.setText(data.getWeather().get(0).getDescription());
                    maxTempTextView.setText("Max: " + data.getMain().getTemp_max() + " °C");
                    minTempTextView.setText("Min: " + data.getMain().getTemp_min() + " °C");
                    humidityTextView.setText(data.getMain().getHumidity() + " %");
                    windSpeedTextView.setText(data.getWind().getSpeed() + " m/s");
                    seaLevelTextView.setText(data.getMain().getSea_level() + " hPa");
                    sunriseTextView.setText(formatUnixTime(data.getSys().getSunrise()));
                    sunsetTextView.setText(formatUnixTime(data.getSys().getSunset()));

                } else {
                    Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}