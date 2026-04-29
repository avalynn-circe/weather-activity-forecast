package com.avalynn.weather;

import lombok.Data;

@Data
public class ForecastDay {
    private String dateTime;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private double windSpeed;
    private String description;
    private String city;
    private Boolean matches;
    private double precipitation;
}