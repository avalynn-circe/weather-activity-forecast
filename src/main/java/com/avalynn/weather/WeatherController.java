package com.avalynn.weather;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public String showForm() {
        return "weather";
    }

    @GetMapping("/forecast")
    public String getForecast(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") double minTemp,
            @RequestParam(defaultValue = "150") double maxTemp,
            @RequestParam(defaultValue = "100") double maxWind,
            @RequestParam(defaultValue = "100") double maxHumidity,
            @RequestParam(defaultValue = "false") boolean activitySet,
            Model model) {

        ForecastSummary summary = weatherService.getForecast(
                city, minTemp, maxTemp, maxWind, maxHumidity, activitySet);
        model.addAttribute("summary", summary);
        model.addAttribute("selectedCity", city);
        return "weather";
    }
}