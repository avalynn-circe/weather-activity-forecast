package com.avalynn.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherService {

    private final String API_KEY = "dff1a44f0d343e3269ddeb29c389d2b3";
    private final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";

    public ForecastSummary getForecast(
            String city,
            double minTemp,
            double maxTemp,
            double maxWind,
            double maxHumidity,
            double maxPrecip,
            boolean activitySet) {

        RestTemplate restTemplate = new RestTemplate();
        String url = FORECAST_URL + "?q=" + city + "&appid=" + API_KEY + "&units=imperial&cnt=40";
        String json = restTemplate.getForObject(url, String.class);

        List<ForecastDay> results = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode list = root.get("list");

            for (JsonNode entry : list) {
                double temp = entry.get("main").get("temp").asDouble();
                double wind = entry.get("wind").get("speed").asDouble();
                int humidity = entry.get("main").get("humidity").asInt();
                // precipitation is under "rain" -> "3h" and may not exist
                JsonNode rain = entry.get("rain");
                double precipitation = (rain != null && rain.get("3h") != null)
                        ? rain.get("3h").asDouble()
                        : 0.0;

                ForecastDay day = new ForecastDay();
                String raw = entry.get("dt_txt").asText();
                LocalDateTime ldt = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                int hour = ldt.getHour();
                boolean isDaytime = hour >= 6 && hour <= 20;
                day.setDateTime(ldt.format(DateTimeFormatter.ofPattern("EEE M/d  h a")));
                day.setTemperature(Math.round(temp * 10.0) / 10.0);
                day.setFeelsLike(Math.round(entry.get("main").get("feels_like").asDouble() * 10.0) / 10.0);
                day.setHumidity(humidity);
                day.setWindSpeed(Math.round(wind * 10.0) / 10.0);
                day.setDescription(entry.get("weather").get(0).get("description").asText());
                day.setCity(city);
                day.setPrecipitation(precipitation);

                // Evaluate against activity parameters
                boolean matches = activitySet && isDaytime
                        && temp >= minTemp
                        && temp <= maxTemp
                        && wind <= maxWind
                        && humidity <= maxHumidity
                        && precipitation <= maxPrecip;

                day.setMatches(matches);
                results.add(day);
            }
        } catch (Exception e) {
            System.out.println("Error parsing weather data: " + e.getMessage());
        }

        // Build summary
        ForecastSummary summary = new ForecastSummary();
        summary.setCity(city);
        summary.setDays(results);
        summary.setMatchingDays((int) results.stream().filter(d -> Boolean.TRUE.equals(d.getMatches())).count());

        if (!results.isEmpty()) {
            double high = results.stream().mapToDouble(ForecastDay::getTemperature).max().orElse(0);
            double low = results.stream().mapToDouble(ForecastDay::getTemperature).min().orElse(0);
            double avgTemp = results.stream().mapToDouble(ForecastDay::getTemperature).average().orElse(0);
            double avgWind = results.stream().mapToDouble(ForecastDay::getWindSpeed).average().orElse(0);
            double avgPrecip = results.stream()
                    .mapToDouble(ForecastDay::getPrecipitation)
                    .average().orElse(0);


            summary.setHighTemp(Math.round(high * 10.0) / 10.0);
            summary.setLowTemp(Math.round(low * 10.0) / 10.0);
            summary.setAvgTemp(Math.round(avgTemp * 10.0) / 10.0);
            summary.setAvgWind(Math.round(avgWind * 10.0) / 10.0);
            summary.setAvgPrecip(Math.round(avgPrecip * 10.0) / 10.0);

            long matchCount = results.stream().filter(d -> Boolean.TRUE.equals(d.getMatches())).count();
            long totalCount = results.size();
            double matchRatio = (double) matchCount / totalCount;

            if (matchRatio > 0.6) {
                summary.setVerdict("Great week for your activity!");
            } else if (matchRatio > 0.3) {
                summary.setVerdict("A few good windows this week.");
            } else if (matchRatio > 0) {
                summary.setVerdict("Limited opportunities this week.");
            } else {
                summary.setVerdict("Conditions don't look favorable this week.");
            }
        } else {
            summary.setVerdict("No forecast data available.");
        }

        return summary;
    }
}