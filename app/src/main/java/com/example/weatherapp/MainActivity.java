package com.example.weatherapp;

import android.os.Bundle;
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

import com.example.weatherapp.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // ✅ One binding instance — replaces ALL findViewById
    private ActivityMainBinding binding;

    // ✅ Retrofit API
    private WeatherApi weatherApi;

    // ✅ Your API key
    private final String API_KEY = "39540792dedf1579c5821d2dec7dbd8f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ✅ Inflate binding and set root view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Handle edge-to-edge insets if needed
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherApi.class);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        String currentDay = dayFormat.format(new Date());
        binding.day.setText(currentDay);

// Get current date, e.g., "June 21"
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM YYYY");
        String currentDate = dateFormat.format(new Date());
        binding.date.setText(currentDate);

        // ✅ SearchView listener using binding
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchWeather(query.trim());
                binding.searchView.clearFocus(); // Hide keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    // ✅ Weather API call
    private void fetchWeather(String cityName) {
        Call<WeatherResponse> call = weatherApi.getWeatherByCity(cityName, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse data = response.body();

                    // ✅ Bind data to views via binding
                    binding.cityName.setText(data.getName());
                    binding.temp.setText(data.getMain().getTemp() + " °C");
                    String description = data.getWeather().get(0).getDescription();
                    String[] words = description.split(" ");
                    if (words.length > 1) {
                        StringBuilder builder = new StringBuilder();
                        for (String word : words) {
                            builder.append(word).append("\n");
                        }
                        // Remove trailing newline
                        description = builder.toString().trim();
                    }

                    binding.weather.setText(description);
                    binding.maxTemp.setText("Max: " + data.getMain().getTemp_max() + " °C");
                    binding.minTemp.setText("Min: " + data.getMain().getTemp_min() + " °C");
                    binding.humidity.setText(data.getMain().getHumidity() + " %");
                    binding.windSpeed.setText(data.getWind().getSpeed() + " m/s");
                    binding.seaLevel.setText(data.getMain().getSea_level() + " hPa");
                    binding.sunrise.setText(formatUnixTime(data.getSys().getSunrise()));
                    binding.sunset.setText(formatUnixTime(data.getSys().getSunset()));

                    double temp = data.getMain().getTemp();
                    double tempMin = data.getMain().getTemp_min();
                    double tempMax = data.getMain().getTemp_max();

// If min or max equal current temp, just show "N/A" or skip:
                    if (tempMin == temp) {
                        binding.minTemp.setText("");
                    } else {
                        binding.minTemp.setText("Min: " + tempMin + " °C");
                    }

                    if (tempMax == temp) {
                        binding.maxTemp.setText("");
                    } else {
                        binding.maxTemp.setText("Max: " + tempMax + " °C");
                    }
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

    // ✅ Helper to format UNIX time
    private String formatUnixTime(long unixSeconds) {
        Date date = new java.util.Date(unixSeconds * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        return sdf.format(date);
    }
}
