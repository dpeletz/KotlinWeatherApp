package com.example.kotlinweatherapp.data;

public class CityData {
    private String cityName;

    public CityData(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
