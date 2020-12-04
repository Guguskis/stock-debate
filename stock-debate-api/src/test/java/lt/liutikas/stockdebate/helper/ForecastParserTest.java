package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.model.ForecastType;
import lt.liutikas.stockdebate.model.ParsedForecast;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
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
        String text = "I have F $9c 10/30. This is either gonna be a big payday on the 29th after their earnings or the \uD83C\uDF08\uD83D\uDC3B are gonna have their way with my ass.";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        ParsedForecast parsedForecast = parsedForecasts.get(0);
        assertParsedForecast(parsedForecast, "F", "2020-10-30T00:00:00", 9, ForecastType.CALL);
    }

    @Test
    public void parse_textGivenInFormat_SYMBOL_PRICETYPE_DATE_returnsParsedForecast() {
        String text = "NIO 50p 11/20";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "NIO", "2020-11-20T00:00:00", 50, ForecastType.PUT);
    }

    @Test
    public void parse_textGivenInFormat_symbol_PRICETYPE_DATE_returnsParsedForecast() {
        String text = "Yep. I'm holding msft 230c 12/18";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "MSFT", "2020-12-18T00:00:00", 230, ForecastType.CALL);
    }

    @Test
    public void parse_textGivenInFormat_SYMBOL_PRICETYPE_DATE_returnsMultipleParsedForecast() {
        String text = "NVDA 605C 11/13 & NVDA 625C 11/20";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(2, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "NVDA", "2020-11-13T00:00:00", 605, ForecastType.CALL);
        assertParsedForecast(parsedForecasts.get(1), "NVDA", "2020-11-20T00:00:00", 625, ForecastType.CALL);
    }

    @Test
    public void parse_textGivenInFormat_$SYMBOL_PRICETYPE_DATE_returnsMultipleParsedForecast() {
        String text = "$MCK 195c 9/15"; // was 1/15 candidate
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "MCK", "2020-09-15T00:00:00", 195, ForecastType.CALL);
    }

    @Test
    public void parse_textGivenInFormat_SYMBOL_DATE_$PRICETYPE_returnsParsedForecast() {
        String text = "Should of loaded up on boomer calls.. T 12/19 $30c take me to the moon with you old timers!";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "T", "2020-12-19T00:00:00", 30, ForecastType.CALL);
    }

    @Test
    public void parse_textGivenInFormat_LONGDATE_SYMBOL_PRICE_TYPE_returnsParsedForecast() {
        String text = "11/9/21 SPY 363 Call";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "SPY", "2021-11-09T00:00:00", 363, ForecastType.CALL);
    }

    @Test
    public void parse_givenTextWithoutForecasts_returnsEmptyList() {
        String text = "======\n" +
                "It means OP thinks that NIO will reach $100 a share by expiration of the call option in January 2021. If it does, he can either exercise that option and purchase 100 shares of stock at that locked in price, or he can sell the option which should have also risen in value. Or not. Apparently there are Greek people who aren’t very nice that might steal his gains.\n" +
                "If it doesn’t reach $100 by that date the option will expire worthless and OP will have to suck dick for a living.\n" +
                "======\n" +
                "Please excuse my combined autistic retardation but can someone please explain what this set of data means ? Is it 100 Calls of Nio expiring 1/15/21 ?\n" +
                "PayPal 11/20 $192.5 c\n" +
                "Currently down 80% total\n" +
                "Can Li hit $40 by 11/27\uD83C\uDD98\uD83D\uDC42\uD83C\uDFFB\n" +
                "\n";
        LocalDateTime createdDate = LocalDateTime.of(2020, 8, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(0, parsedForecasts.size());
    }

    @Test
    public void parse_forecastExpirationIsDayBeforeCreated_returnsParsedForecastOfNextYear() {
        String text = "$MCK 195c 12/14";
        LocalDateTime createdDate = LocalDateTime.of(2020, 12, 15, 0, 0);

        List<ParsedForecast> parsedForecasts = forecastParser.parse(text, createdDate);

        assertEquals(1, parsedForecasts.size());
        assertParsedForecast(parsedForecasts.get(0), "MCK", "2021-12-14T00:00:00", 195, ForecastType.CALL);
    }

    private void assertParsedForecast(ParsedForecast parsedForecast, String stockSymbol, String expirationDate, double strikePrice, ForecastType forecastType) {
        assertEquals(stockSymbol, parsedForecast.getStockSymbol());
        assertEquals(expirationDate, parsedForecast.getExpirationDate());
        assertEquals(strikePrice, parsedForecast.getStrikePrice(), 0.001);
        assertEquals(forecastType, parsedForecast.getForecastType());
    }
}