package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.ForecastParser;
import lt.liutikas.stockdebate.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static lt.liutikas.stockdebate.model.ForecastType.CALL;

@Component
public class ForecastService {

    private final Logger LOG = LoggerFactory.getLogger(ForecastService.class);

    private final RestTemplate discussionTemplate;
    private final ForecastParser forecastParser;

    public ForecastService(@Qualifier("discussion") RestTemplate discussionTemplate, ForecastParser forecastParser) {
        this.discussionTemplate = discussionTemplate;
        this.forecastParser = forecastParser;
    }

    public ResponseEntity getForecasts(String username) {
        RedditUser redditUser = new RedditUser();

        List<Comment> comments;
        try {
            comments = discussionTemplate.getForObject(String.format("/api/reddituser/%s/comments", username), List.class);
        } catch (RestClientException e) {
            String message = parseErrorMessage(e);
            LOG.error(String.format("Failed to retrieve comments for user '%s'. Reason: '%s'", username, message));
            return ResponseEntity.badRequest().body(message);
        }

        List<ParsedForecast> parsedForecasts = getParsedForecasts(comments);

        List<Forecast> forecasts = new ArrayList<>();

        forecasts.add(getAmdCallForecast());
        forecasts.add(getAmdCallForecast());
        forecasts.add(getIntelPutForecast());
        forecasts.add(getAmdCallForecast());
        forecasts.add(getIntelPutForecast());
        forecasts.add(getAmdCallForecast());
        forecasts.add(getIntelPutForecast());
        forecasts.add(getIntelPutForecast());
        forecasts.add(getIntelPutForecast());
        forecasts.forEach(this::updateSuccessCoefficient);

        redditUser.setUsername(username);
        redditUser.setForecasts(forecasts);

        LOG.info(String.format("Retrieved forecasts for user '%s'", username));

        return ResponseEntity.ok(redditUser);
    }

    private List<ParsedForecast> getParsedForecasts(List<Comment> comments) {
        return comments.stream()
                .map(Comment::getText)
                .map(forecastParser::parse)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String parseErrorMessage(RestClientException e) {
        Pattern pattern = Pattern.compile("\\[(.*)\\]");
        Matcher matcher = pattern.matcher(e.getMessage());

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return e.getMessage();
        }
    }

    private Forecast getIntelPutForecast() {
        Forecast forecast = new Forecast();
        Stock stock = new Stock();
        stock.setSymbol("INTC");
        stock.setCompanyName("Intel corporation");
        stock.setLogoUrl("https://reactnative.dev/img/tiny_logo.png");
        stock.setLatestPrice(43.15);
        forecast.setStock(stock);

        forecast.setCreatedDate(LocalDate.parse("2020-02-01").toString());
        forecast.setExpirationDate(LocalDate.parse("2020-04-01").toString());
        forecast.setExpirationPrice(40.5);
        forecast.setStrikePrice(34);
        forecast.setForecastType(ForecastType.PUT);
        return forecast;
    }

    private Forecast getAmdCallForecast() {
        Forecast forecast = new Forecast();
        Stock stock = new Stock();
        stock.setSymbol("AMD");
        stock.setCompanyName("Advanced micro devices inc.");
        stock.setLogoUrl("https://reactnative.dev/img/tiny_logo.png");
        stock.setLatestPrice(83.15);
        forecast.setStock(stock);

        forecast.setCreatedDate(LocalDate.parse("2020-11-05").toString());
        forecast.setExpirationDate(LocalDate.parse("2021-11-12").toString());
        forecast.setExpirationPrice(94.5);
        forecast.setStrikePrice(95);
        forecast.setForecastType(CALL);
        return forecast;
    }

    private void updateSuccessCoefficient(Forecast forecast) {
        double successCoefficient = 0;

        LocalDate expirationDate = LocalDate.parse(forecast.getExpirationDate());
        LocalDate now = LocalDate.now();
        boolean expired = expirationDate.isAfter(now);

        double expirationPrice = forecast.getExpirationPrice();
        double latestPrice = forecast.getStock().getLatestPrice();
        double strikePrice = forecast.getStrikePrice();

        if (expired) {
            if (CALL == forecast.getForecastType()) {
                successCoefficient = expirationPrice - strikePrice;
            } else {
                successCoefficient = strikePrice - expirationPrice;
            }
            successCoefficient /= expirationPrice;
        } else {
            if (CALL == forecast.getForecastType()) {
                successCoefficient = strikePrice - latestPrice;
            } else {
                successCoefficient = latestPrice - strikePrice;
            }
            successCoefficient /= latestPrice;
        }

        forecast.setSuccessCoefficient(successCoefficient);
    }
}
