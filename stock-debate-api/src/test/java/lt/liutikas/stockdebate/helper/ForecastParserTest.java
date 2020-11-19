package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.model.ForecastType;
import lt.liutikas.stockdebate.model.ParsedForecast;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ForecastParserTest {

    private static final String NOW = "2020-11-19T16:45:42.00Z";

    private ForecastParser forecastParser;

    @Before
    public void setUp() {
        Clock clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);
        forecastParser = new ForecastParser(clock);
    }

    @Test
    public void parse_textGivenInFormat_SYMBOL_$PRICETYPE_DATE_returnsParsedForecast() {
        String stockSymbol = "F";
        String expirationDate = "2020-10-30";
        double strikePrice = 9;
        ForecastType forecastType = ForecastType.CALL;

        String text = "I have F $9c 10/30. This is either gonna be a big payday on the 29th after their earnings or the \uD83C\uDF08\uD83D\uDC3B are gonna have their way with my ass.";

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text);

        assertEquals(2, parsedForecasts.size());
        assertEquals(stockSymbol, parsedForecasts.get(0).getStockSymbol());
        assertEquals(expirationDate, parsedForecasts.get(0).getExpirationDate());
        assertEquals(strikePrice, parsedForecasts.get(0).getStrikePrice(), 0.001);
        assertEquals(forecastType, parsedForecasts.get(0).getForecastType());
    }


    @Test
    public void parse_textGivenInFormat_SYMBOL_PRICETYPE_DATE_returnsParsedForecast() {
        String stockSymbol = "NIO";
        String expirationDate = "2020-11-20";
        double strikePrice = 50;
        ForecastType forecastType = ForecastType.PUT;

        String text = "NIO 50p 11/20";

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text);

        assertEquals(1, parsedForecasts.size());
        assertEquals(stockSymbol, parsedForecasts.get(0).getStockSymbol());
        assertEquals(expirationDate, parsedForecasts.get(0).getExpirationDate());
        assertEquals(strikePrice, parsedForecasts.get(0).getStrikePrice(), 0.001);
        assertEquals(forecastType, parsedForecasts.get(0).getForecastType());
    }


    @Test
    public void parse_textGivenInFormat_SYMBOL_DATE_$PRICETYPE_returnsParsedForecast() {
        String stockSymbol = "T";
        String expirationDate = "2020-03-19";
        double strikePrice = 30;
        ForecastType forecastType = ForecastType.CALL;

        String text = "Should of loaded up on boomer calls.. T 3/19 $30c take me to the moon with you old timers!";

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text);

        assertEquals(1, parsedForecasts.size());
        assertEquals(stockSymbol, parsedForecasts.get(0).getStockSymbol());
        assertEquals(expirationDate, parsedForecasts.get(0).getExpirationDate());
        assertEquals(strikePrice, parsedForecasts.get(0).getStrikePrice(), 0.001);
        assertEquals(forecastType, parsedForecasts.get(0).getForecastType());
    }

}