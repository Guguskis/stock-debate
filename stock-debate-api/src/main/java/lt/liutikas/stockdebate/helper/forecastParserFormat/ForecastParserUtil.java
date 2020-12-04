package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ForecastType;
import lt.liutikas.stockdebate.model.ParsedForecast;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ForecastParserUtil {

    public static ForecastType getForecastType(String forecastTypeString) {
        ForecastType forecastType;
        if (forecastTypeString.equalsIgnoreCase("c") || forecastTypeString.equalsIgnoreCase("call")) {
            forecastType = ForecastType.CALL;
        } else if (forecastTypeString.equalsIgnoreCase("p") || forecastTypeString.equalsIgnoreCase("put")) {
            forecastType = ForecastType.PUT;
        } else {
            throw new RuntimeException(String.format("Unable to parse forecastType %s", forecastTypeString));
        }
        return forecastType;
    }

    public static String getExpirationDate(Clock clock, LocalDateTime createdDate, String expirationDateString) {
        String[] expirationDateStrings = expirationDateString.split("/");
        String date;

        if (expirationDateStrings.length == 2) {
            date = getShortDate(clock, createdDate, expirationDateStrings);
        } else if (expirationDateStrings.length == 3) {
            date = getLongDate(expirationDateStrings);
        } else {
            throw new RuntimeException(String.format("Failed to parse date for '%s'", expirationDateString));
        }

        return date;
    }

    private static String getLongDate(String[] expirationDateStrings) {
        String month = appendMissingTrailingZero(expirationDateStrings[0]);
        String day = appendMissingTrailingZero(expirationDateStrings[1]);
        String year = expirationDateStrings[2];

        return String.format("20%s-%s-%sT00:00:00", year, month, day);
    }

    private static String getShortDate(Clock clock, LocalDateTime createdDate, String[] expirationDateStrings) {
        LocalDate now = LocalDate.now(clock);

        String monthString = appendMissingTrailingZero(expirationDateStrings[0]);
        String dayString = appendMissingTrailingZero(expirationDateStrings[1]);
        int month = Integer.parseInt(monthString);
        int day = Integer.parseInt(dayString);
        int year = now.getYear();

        LocalDateTime expirationDateTime = LocalDateTime.of(year, month, day, 0, 0);

        if (expirationDateTime.isBefore(createdDate)) {
            year = now.getYear() + 1;
        } else {
            year = now.getYear();
        }
        return String.format("%s-%s-%sT00:00:00", year, monthString, dayString);
    }

    private static String appendMissingTrailingZero(String doubleDigitString) {
        if (doubleDigitString.length() == 1) doubleDigitString = "0" + doubleDigitString;
        return doubleDigitString;
    }

    public static ParsedForecast getParsedForecast(String symbol, String strikePriceString, String forecastTypeString, String expirationDateString, Clock clock, LocalDateTime createdDate) {
        ParsedForecast parsedForecast = new ParsedForecast();
        parsedForecast.setStockSymbol(symbol.toUpperCase());
        parsedForecast.setStrikePrice(Double.parseDouble(strikePriceString));
        parsedForecast.setForecastType(getForecastType(forecastTypeString));
        parsedForecast.setExpirationDate(getExpirationDate(clock, createdDate, expirationDateString));
        return parsedForecast;
    }
}
