package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.ForecastParser;
import lt.liutikas.stockdebate.model.*;
import lt.liutikas.stockdebate.repository.DiscussionRepository;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ForecastServiceTest {

    private static final String NOW = "2020-11-20T18:35:24.00Z";

    private StockRepository stockRepository;
    private DiscussionRepository discussionRepository;

    private ForecastService forecastService;

    @Before
    public void setUp() {
        Clock clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);
        ForecastParser forecastParser = new ForecastParser(clock);
        stockRepository = mock(StockRepository.class);
        discussionRepository = mock(DiscussionRepository.class);
        forecastService = new ForecastService(forecastParser, stockRepository, discussionRepository, clock);
    }

    @Test
    public void getForecasts_userCommentedNotExpiredForecast_ReturnsForecast() {
        String stockSymbol = "PLTR";
        Double latestPrice = 25.;
        String companyName = "Palantir inc.";
        String logoUrl = "logo url";
        LocalDateTime createdDateTime = LocalDateTime.of(2020, 11, 4, 0, 0);
        String createdDateString = "2020-11-04T00:00:00";
        String expirationDateString = "2020-12-04T00:00:00";
        ForecastType forecastType = ForecastType.CALL;
        Double strikePrice = 24.;
        Double successCoefficient = 0.04;

        Comment comment = new Comment();
        comment.setText("YOLO PLTR 24c 12/4");
        comment.setCreationDate(createdDateTime);

        Stock stock = new Stock();
        stock.setSymbol(stockSymbol);
        stock.setLatestPrice(latestPrice);
        stock.setCompanyName(companyName);
        stock.setLogoUrl(logoUrl);

        SimpleStockDetail simpleStockDetail = new SimpleStockDetail();
        simpleStockDetail.setPrice(latestPrice);

        when(discussionRepository.getComments("John"))
                .thenReturn(Collections.singletonList(comment));
        when(stockRepository.getStock(stockSymbol))
                .thenReturn(stock);
        when(stockRepository.getSimpleStockDetail(stockSymbol, expirationDateString))
                .thenReturn(simpleStockDetail);

        ResponseEntity responseEntity = forecastService.getForecasts("John");
        RedditUser redditUser = (RedditUser) responseEntity.getBody();
        List<Forecast> forecasts = redditUser.getForecasts();

        assertEquals(forecasts.size(), 1);
        assertNotExpiredForecast(forecasts.get(0),
                stockSymbol, latestPrice, companyName, logoUrl,
                createdDateString, expirationDateString,
                forecastType, strikePrice, successCoefficient);
    }

    @Test
    public void getForecasts_userCommentedExpiredForecast_ReturnsForecast() {
        String stockSymbol = "PLTR";
        Double latestPrice = 25.;
        String companyName = "Palantir inc.";
        String logoUrl = "logo url";
        LocalDateTime createdDateTime = LocalDateTime.of(2020, 11, 4, 0, 0);
        String createdDateString = "2020-11-04T00:00:00";
        String expirationDateString = "2020-11-16T00:00:00";
        ForecastType forecastType = ForecastType.CALL;
        Double strikePrice = 24.;
        Double expirationPrice = 28.;
        Double successCoefficient = 0.1428;

        Comment comment = new Comment();
        comment.setText("YOLO PLTR 24c 11/16");
        comment.setCreationDate(createdDateTime);

        Stock stock = new Stock();
        stock.setSymbol(stockSymbol);
        stock.setLatestPrice(latestPrice);
        stock.setCompanyName(companyName);
        stock.setLogoUrl(logoUrl);

        SimpleStockDetail simpleStockDetail = new SimpleStockDetail();
        simpleStockDetail.setDate(expirationDateString);
        simpleStockDetail.setSymbol(stockSymbol);
        simpleStockDetail.setPrice(expirationPrice);

        when(discussionRepository.getComments("John"))
                .thenReturn(Collections.singletonList(comment));
        when(stockRepository.getStock(stockSymbol))
                .thenReturn(stock);
        when(stockRepository.getSimpleStockDetail(stockSymbol, expirationDateString))
                .thenReturn(simpleStockDetail);

        ResponseEntity responseEntity = forecastService.getForecasts("John");
        RedditUser redditUser = (RedditUser) responseEntity.getBody();
        List<Forecast> forecasts = redditUser.getForecasts();

        assertEquals(forecasts.size(), 1);
        assertExpiredForecast(forecasts.get(0),
                stockSymbol, latestPrice, companyName, logoUrl,
                createdDateString, expirationDateString,
                forecastType, strikePrice, expirationPrice, successCoefficient);
    }

    @Test
    public void getForecasts_userCommentedNotExistingStock_ReturnsNoForecasts() {
        String stockSymbol = "meme";
        LocalDateTime createdDateTime = LocalDateTime.of(2020, 11, 4, 0, 0);

        Comment comment = new Comment();
        comment.setText("YOLO meme 24c 11/16");
        comment.setCreationDate(createdDateTime);

        when(discussionRepository.getComments("John"))
                .thenReturn(Collections.singletonList(comment));
        when(stockRepository.getStock(stockSymbol))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity responseEntity = forecastService.getForecasts("John");
        RedditUser redditUser = (RedditUser) responseEntity.getBody();
        List<Forecast> forecasts = redditUser.getForecasts();

        assertEquals(forecasts.size(), 0);
    }

    @Test
    public void getForecasts_userIncorrectlyCommentedForecast_ReturnsNoForecasts() {
        String stockSymbol = "PLTR";
        Double latestPrice = 25.;
        String companyName = "Palantir inc.";
        String logoUrl = "logo url";

        Comment comment = new Comment();
        comment.setText("Put 2k in PLTR FD calls 12/4");
        LocalDateTime createdDateTime = LocalDateTime.of(2020, 11, 4, 0, 0);
        comment.setCreationDate(createdDateTime);

        Stock stock = new Stock();
        stock.setSymbol(stockSymbol);
        stock.setLatestPrice(latestPrice);
        stock.setCompanyName(companyName);
        stock.setLogoUrl(logoUrl);

        when(discussionRepository.getComments("John"))
                .thenReturn(Collections.singletonList(comment));

        ResponseEntity responseEntity = forecastService.getForecasts("John");
        RedditUser redditUser = (RedditUser) responseEntity.getBody();
        List<Forecast> forecasts = redditUser.getForecasts();

        assertEquals(forecasts.size(), 0);
    }

    @Test
    public void getForecasts_userNotCommented_ReturnsNoForecasts() {
        when(discussionRepository.getComments("John"))
                .thenReturn(Collections.emptyList());

        ResponseEntity responseEntity = forecastService.getForecasts("John");
        RedditUser redditUser = (RedditUser) responseEntity.getBody();
        List<Forecast> forecasts = redditUser.getForecasts();

        assertEquals(forecasts.size(), 0);
    }

    private void assertNotExpiredForecast(Forecast forecast, String stockSymbol, double latestPrice, String companyName, String logoUrl, String createdDateString, String expirationDateString, ForecastType forecastType, double strikePrice, double successCoefficient) {
        assertEquals(stockSymbol, forecast.getStock().getSymbol());
        assertEquals(latestPrice, forecast.getStock().getLatestPrice(), 0.001);
        assertEquals(companyName, forecast.getStock().getCompanyName());
        assertEquals(logoUrl, forecast.getStock().getLogoUrl());
        assertEquals(createdDateString, forecast.getCreatedDate());
        assertEquals(expirationDateString, forecast.getExpirationDate());
        assertEquals(forecastType, forecast.getForecastType());
        assertEquals(strikePrice, forecast.getStrikePrice(), 0.001);
        assertEquals(successCoefficient, forecast.getSuccessCoefficient(), 0.001);
    }

    private void assertExpiredForecast(Forecast forecast, String stockSymbol, double latestPrice, String companyName, String logoUrl, String createdDateString, String expirationDateString, ForecastType forecastType, double strikePrice, double expirationPrice, double successCoefficient) {
        assertEquals(stockSymbol, forecast.getStock().getSymbol());
        assertEquals(latestPrice, forecast.getStock().getLatestPrice(), 0.001);
        assertEquals(companyName, forecast.getStock().getCompanyName());
        assertEquals(logoUrl, forecast.getStock().getLogoUrl());
        assertEquals(createdDateString, forecast.getCreatedDate());
        assertEquals(expirationDateString, forecast.getExpirationDate());
        assertEquals(expirationPrice, forecast.getExpirationPrice(), 0.001);
        assertEquals(forecastType, forecast.getForecastType());
        assertEquals(strikePrice, forecast.getStrikePrice(), 0.001);
        assertEquals(successCoefficient, forecast.getSuccessCoefficient(), 0.001);
    }

}