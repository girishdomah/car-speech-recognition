package com.example.carassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity {
    private TextView textViewLocation;
    private ImageView imageViewMain;
    private TextView textViewMain;
    private TextView textViewDescription;
    private TextView textViewTemperature;
    private TextView textViewFeelsLike;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference locationsRef = db.collection("Locations");

    private static final String TAG = "WeatherActivity";

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        textViewLocation = findViewById(R.id.text_view_location);
        imageViewMain = findViewById(R.id.image_view_main);
        textViewMain = findViewById(R.id.text_view_main);
        textViewDescription = findViewById(R.id.text_view_description);
        textViewTemperature = findViewById(R.id.text_view_temperature);
        textViewFeelsLike = findViewById(R.id.text_view_feels_like);

        // Initialize text to speech
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.FRENCH);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "onInit: Language not supported");
                    }
                } else {
                    Log.e(TAG, "onInit: Initialization failed");
                }
            }
        });

        Intent intent = getIntent();
        String locationName = intent.getStringExtra("locationName");
        Log.d(TAG, "onCreate: " + locationName);

        locationsRef.whereEqualTo("name", locationName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "onSuccess: ");
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Location location = documentSnapshot.toObject(Location.class);
                            Log.d(TAG, "onSuccess: " + location.getLat());
                            Log.d(TAG, "onSuccess: " + location.getLon());

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();

                            OpenWeatherMapApi openWeatherMapApi = retrofit.create(OpenWeatherMapApi.class);

                            Call<WeatherForecast> call = openWeatherMapApi.getWeatherForecast(location.getLat(), location.getLon(), OpenWeatherMapApi.API_KEY);

                            call.enqueue(new Callback<WeatherForecast>() {
                                @Override
                                public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                                    WeatherForecast weatherForecast = response.body();
                                    textViewLocation.setText(weatherForecast.getName());

                                    String weatherMain = weatherForecast.getWeatherMain();

                                    textViewMain.setText(weatherMain);
                                    textViewDescription.setText(weatherForecast.getWeatherDescription());

                                    DecimalFormat decimalFormat = new DecimalFormat("##.#");
                                    decimalFormat.setRoundingMode(RoundingMode.CEILING);

                                    double temperature = convertToCelsius(weatherForecast.getMainTemp());
                                    String temperatureText = decimalFormat.format(temperature) + "°C";
                                    textViewTemperature.setText(temperatureText);

                                    double feelsLike = convertToCelsius(weatherForecast.getMainFeelsLike());
                                    String feelsLikeText = "feels like " + decimalFormat.format(feelsLike) + "°C";
                                    textViewFeelsLike.setText(feelsLikeText);

                                    if (weatherMain.contains("Sun")) {
                                        imageViewMain.setImageResource(R.drawable.sun);
                                        speak("Les temps ensoleillé " + locationName);
                                    } else if (weatherMain.contains("Cloud")) {
                                        imageViewMain.setImageResource(R.drawable.cloudy);
                                        speak("Les temps nuageux " + locationName);
                                    } else if (weatherMain.contains("Rain")) {
                                        imageViewMain.setImageResource(R.drawable.rain);
                                        speak("Ena la pluie " + locationName);
                                    } else if (weatherMain.contains("Clear")) {
                                        imageViewMain.setImageResource(R.drawable.clear);
                                        speak("Les temps clair " + locationName);
                                    }
                                }

                                @Override
                                public void onFailure(Call<WeatherForecast> call, Throwable t) {
                                    Log.d(TAG, "onFailure: ");
                                }
                            });
                        }
                    }
                });
    }

    private void speak(String text) {
        float pitch = 1f;
        float speed = 1f;

        tts.setPitch(pitch);
        tts.setSpeechRate(speed);

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }

    private double convertToCelsius(float kelvin) {
        // return (fahrenheit - 32) * 5 / 9;
        return kelvin - 273.15;
    }
}