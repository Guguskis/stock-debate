package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Forecast;
import lt.liutikas.stockdebate.model.ForecastType;
import lt.liutikas.stockdebate.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ForecastService {

    private final Logger LOG = LoggerFactory.getLogger(ForecastService.class);

    public ResponseEntity getForecasts(String username) {
        List<Forecast> forecasts = new ArrayList<>();

        Forecast forecast = new Forecast();
        Stock stock = new Stock();
        stock.setSymbol("AMD");
        stock.setCompanyName("Advanced micro devices inc.");
        stock.setLogoUrl("https://reactnative.dev/img/tiny_logo.png");
        stock.setLatestPrice(83.15);
        forecast.setStock(stock);

        forecast.setUsername(username);
        forecast.setCreatedDate(LocalDate.parse("2021-01-01").toString());
        forecast.setExpirationDate(LocalDate.parse("2021-01-05").toString());
        forecast.setExpirationPrice(96.5);
        forecast.setTargetPrice(95);
        forecast.setSuccessCoefficient(13);
        forecast.setForecastType(ForecastType.CALL);

        forecasts.add(forecast);

        LOG.info(String.format("Retrieved forecasts for user '%s'", username));

        return ResponseEntity.ok(forecasts);
    }
}
