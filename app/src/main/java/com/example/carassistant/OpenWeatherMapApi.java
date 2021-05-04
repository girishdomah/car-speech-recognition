package com.example.carassistant;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherMapApi {
    public static final String API_KEY = "5f29318dd109dec4d9db7b25262ea8d5";

    @GET("weather?")
    Call<WeatherForecast> getWeatherForecast(@Query("lat") double lat, @Query("lon") double lon, @Query("appId") String apiKey);
}
