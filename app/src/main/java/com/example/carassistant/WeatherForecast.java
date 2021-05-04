package com.example.carassistant;

import java.util.ArrayList;

public class WeatherForecast {
    private Coordinates coord;
    private ArrayList<Weather> weather;
    private String base;
    private Main main;
    private int visibility;
    private Wind wind;
    private Clouds clouds;
    private int dt;
    private Sys sys;
    private int timezone;
    private int id;
    private String name;
    private int cod;

    public String getWeatherMain() {
        return weather.get(0).main;
    }

    public String getWeatherDescription() {
        return weather.get(0).description;
    }

    public float getMainTemp() {
        return main.temp;
    }

    public float getMainFeelsLike() {
        return main.feels_like;
    }

    public String getName() {
        return name;
    }

    private class Coordinates {
        private float lon;
        private float lat;
    }

    private class Weather {
        private int id;
        private String main;
        private String description;
        private String icon;
    }

    private class Main {
        private float temp;
        private float feels_like;
        private float temp_min;
        private float temp_max;
        private int pressure;
        private int humidity;
    }

    private class Wind {
        private float speed;
        private int deg;
    }

    private class Clouds {
        private int all;
    }

    private class Sys {
        private int type;
        private int id;
        private String country;
        private int sunrise;
        private int sunset;
    }
}
