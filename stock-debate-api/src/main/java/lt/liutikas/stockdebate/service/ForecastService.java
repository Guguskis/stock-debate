package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.ForecastParser;
import lt.liutikas.stockdebate.model.*;
import lt.liutikas.stockdebate.repository.DiscussionRepository;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static lt.liutikas.stockdebate.model.ForecastType.CALL;

@Component
public class ForecastService {

    private final Logger LOG = LoggerFactory.getLogger(ForecastService.class);

    private final ForecastParser forecastParser;
    private final StockRepository stockRepository;
    private final DiscussionRepository discussionRepository;

    public ForecastService(ForecastParser forecastParser,
                           StockRepository stockRepository,
                           DiscussionRepository discussionRepository) {
        this.forecastParser = forecastParser;
        this.stockRepository = stockRepository;
        this.discussionRepository = discussionRepository;
    }

    public ResponseEntity getForecasts(String username) {
        List<Comment> comments;

        try {
            comments = discussionRepository.getComments(username);
        } catch (RestClientException e) {
            String message = parseErrorMessage(e);
            LOG.error(String.format("Failed to retrieve comments for user '%s'. Reason: '%s'", username, message));
            return ResponseEntity.badRequest().body(message);
        }

        List<ParsedForecast> parsedForecasts = getParsedForecasts(comments);
        List<Forecast> forecasts = parsedForecasts.stream()
                .map(this::getForecast)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        RedditUser redditUser = new RedditUser();
        redditUser.setUsername(username);
        redditUser.setForecasts(forecasts);

        LOG.info(String.format("Retrieved %s forecasts for user '%s'", forecasts.size(), username));

        return ResponseEntity.ok(redditUser);
    }

    private Forecast getForecast(ParsedForecast parsedForecast) {
        LocalDateTime expirationDateTime = LocalDateTime.parse(parsedForecast.getExpirationDate());
        LocalDateTime createdDate = parsedForecast.getCreatedDate();
        if (expirationDateTime.isBefore(createdDate)) {
            return null;
        }

        try {
            Stock stock = stockRepository.getStock(parsedForecast.getStockSymbol());

            Forecast forecast = new Forecast();

            forecast.setStock(stock);
            forecast.setExpirationDate(parsedForecast.getExpirationDate());
            forecast.setStrikePrice(parsedForecast.getStrikePrice());
            forecast.setForecastType(parsedForecast.getForecastType());
            forecast.setCreatedDate(
                    getCreatedDateString(parsedForecast.getCreatedDate())
            );

            if (isExpired(parsedForecast.getExpirationDate())) {
                SimpleStockDetail simpleStockDetail = stockRepository.getSimpleStockDetail(
                        parsedForecast.getStockSymbol(),
                        parsedForecast.getExpirationDate());

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

    private String getCreatedDateString(LocalDateTime createdDate) {
        return createdDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
        LocalDateTime expirationDate = LocalDateTime.parse(expirationDateString);
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
