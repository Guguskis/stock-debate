package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ForecastController {

    private final ForecastService forecastService;

    @Autowired
    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping("/{username}/forecasts")
//    @Cacheable("forecasts")
    public ResponseEntity getForecast(@PathVariable String username) {
        return forecastService.getForecasts(username);
    }
}
