package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ForecastType;
import lt.liutikas.stockdebate.model.ParsedForecast;

import java.time.Clock;
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

    public static String getExpirationDate(Clock clock, String expirationDateString) {
        String[] expirationDateStrings = expirationDateString.split("/");
        String month = expirationDateStrings[0];
        String day = expirationDateStrings[1];
        return String.format("%s-%s-%s", LocalDateTime.now(clock).getYear(), month, day);
    }

    public static ParsedForecast getParsedForecast(String symbol, String strikePriceString, String forecastTypeString, String expirationDateString, Clock clock) {
        ParsedForecast parsedForecast = new ParsedForecast();
        parsedForecast.setStockSymbol(symbol);
        parsedForecast.setStrikePrice(Double.parseDouble(strikePriceString));
        parsedForecast.setForecastType(getForecastType(forecastTypeString));
        parsedForecast.setExpirationDate(getExpirationDate(clock, expirationDateString));
        return parsedForecast;
    }
}
