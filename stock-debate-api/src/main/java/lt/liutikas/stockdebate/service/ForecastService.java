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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static lt.liutikas.stockdebate.model.ForecastType.CALL;

@Component
public class ForecastService {

    private final Logger LOG = LoggerFactory.getLogger(ForecastService.class);

    private final RestTemplate discussionTemplate;
    private final RestTemplate stockTemplate;
    private final ForecastParser forecastParser;

    public ForecastService(@Qualifier("discussion") RestTemplate discussionTemplate,
                           @Qualifier("stock") RestTemplate stockTemplate,
                           ForecastParser forecastParser) {
        this.discussionTemplate = discussionTemplate;
        this.stockTemplate = stockTemplate;
        this.forecastParser = forecastParser;
    }

    public ResponseEntity getForecasts(String username) {
        RedditUser redditUser;

        try {
            String url = String.format("/api/reddituser/%s/comments", username);
            redditUser = discussionTemplate.getForObject(url, RedditUser.class);
        } catch (RestClientException e) {
            String message = parseErrorMessage(e);
            LOG.error(String.format("Failed to retrieve comments for user '%s'. Reason: '%s'", username, message));
            return ResponseEntity.badRequest().body(message);
        }

        List<ParsedForecast> parsedForecasts = getParsedForecasts(redditUser.getComments());
        List<Forecast> forecasts = parsedForecasts.stream()
                .map(this::getForecast)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        redditUser.setUsername(username);
        redditUser.setForecasts(forecasts);
        redditUser.setComments(Collections.EMPTY_LIST);

        LOG.info(String.format("Retrieved %s forecasts for user '%s'", forecasts.size(), username));

        return ResponseEntity.ok(redditUser);
    }

    private Forecast getForecast(ParsedForecast parsedForecast) {
        LocalDate expirationDate = LocalDate.parse(parsedForecast.getExpirationDate());
        LocalDate createdDate = parsedForecast.getCreatedDate();
        if (expirationDate.isBefore(createdDate)) {
            return null;
        }

        try {
            String getStockUrl = String.format("/api/stock/%s", parsedForecast.getStockSymbol());
            Stock stock = stockTemplate.getForObject(getStockUrl, Stock.class);

            Forecast forecast = new Forecast();

            forecast.setStock(stock);
            forecast.setExpirationDate(parsedForecast.getExpirationDate());
            forecast.setStrikePrice(parsedForecast.getStrikePrice());
            forecast.setForecastType(parsedForecast.getForecastType());
            forecast.setCreatedDate(
                    getCreatedDateString(parsedForecast.getCreatedDate())
            );

            if (isExpired(parsedForecast.getExpirationDate())) {
                String getStockDetailUrl = String.format("/api/stock/%s/price/%s", parsedForecast.getStockSymbol(), parsedForecast.getExpirationDate());
                SimpleStockDetail simpleStockDetail = stockTemplate.getForObject(getStockDetailUrl, SimpleStockDetail.class);

                forecast.setExpirationPrice(simpleStockDetail.getPrice());
                forecast.setSuccessCoefficient(getSuccessCoefficientForExpired(
                        simpleStockDetail.getPrice(),
                        parsedForecast.getStrikePrice(),
                        parsedForecast.getForecastType())
                );
            } else {
                forecast.setSuccessCoefficient(getSuccessCoefficientForNotExpired(
                        stock.getLatestPrice(),
                        parsedForecast.getStrikePrice(),
                        parsedForecast.getForecastType()
                ));
            }

            return forecast;
        } catch (Exception exception) {
            LOG.warn(String.format(
                    "Failed to create forecast for stock '%s'. Reason: '%s'",
                    parsedForecast.getStockSymbol(), exception.getMessage()));
            return null;
        }
    }

    private String getCreatedDateString(LocalDate createdDate) {
        return String.format("%s-%s-%s", createdDate.getYear(), createdDate.getMonthValue(), createdDate.getDayOfMonth());
    }

    private List<ParsedForecast> getParsedForecasts(List<Comment> comments) {
        return comments.stream()
                .map(comment -> forecastParser.parse(comment.getText(), comment.getCreationDate()))
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

    private double getSuccessCoefficientForExpired(double expirationPrice, double strikePrice, ForecastType forecastType) {
        double successCoefficient;
        if (CALL == forecastType) {
            successCoefficient = expirationPrice - strikePrice;
        } else {
            successCoefficient = strikePrice - expirationPrice;
        }
        return successCoefficient / expirationPrice;
    }

    private double getSuccessCoefficientForNotExpired(double latestPrice, double strikePrice, ForecastType forecastType) {
        double successCoefficient;
        if (CALL == forecastType) {
            successCoefficient = latestPrice - strikePrice;
        } else {
            successCoefficient = strikePrice - latestPrice;
        }
        return successCoefficient / latestPrice;
    }

    private boolean isExpired(String expirationDateString) {
        LocalDate expirationDate = LocalDate.parse(expirationDateString);
        return LocalDate.now().isAfter(expirationDate);
    }
}
