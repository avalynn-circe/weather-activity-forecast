package com.avalynn.weather;

import lombok.Data;
import java.util.List;

@Data
public class ForecastSummary {
    private String city;
    private double highTemp;
    private double lowTemp;
    private double avgTemp;
    private double avgWind;
    private int matchingDays;
    private String verdict;
    private List<ForecastDay> days;
}