package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.model.ForecastType;
import lt.liutikas.stockdebate.model.ParsedForecast;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ForecastParser {

    private final Clock clock;

    public ForecastParser(Clock clock) {
        this.clock = clock;
    }

    public List<ParsedForecast> parse(String text) {
        String symbolPattern = "[A-Z]{1,4}";
        String forecastTypePattern = "[cCpP]";
        String strikePricePattern = "[\\d\\.]+";
        String expirationDatePattern = "\\d{1,2}/\\d{1,2}";

        String regex = getRegexFor_SYMBOL_$PRICETYPE_DATE(symbolPattern, forecastTypePattern, strikePricePattern, expirationDatePattern);
        Matcher matcher = getMatcher(text, regex);

        ParsedForecast parsedForecast = new ParsedForecast();
        if (matcher.find()) {
            System.out.println(matcher.group(0));
            String symbol = matcher.group(1);
            String strikePriceString = matcher.group(2);
            String forecastTypeString = matcher.group(3);
            String expirationDateString = matcher.group(4);
            parsedForecast = getParsedForecast(symbol, strikePriceString, forecastTypeString, expirationDateString);
        }

        return Arrays.asList(parsedForecast);
    }

    private Matcher getMatcher(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(text);
    }

    private String getRegexFor_SYMBOL_$PRICETYPE_DATE(String symbolPattern, String forecastTypePattern, String strikePricePattern, String expirationDatePattern) {
        return String.format("(%s) \\$?(%s)(%s) (%s)", symbolPattern, strikePricePattern, forecastTypePattern, expirationDatePattern);
    }

    private ParsedForecast getParsedForecast(String symbol, String strikePriceString, String forecastTypeString, String expirationDateString) {
        ParsedForecast parsedForecast = new ParsedForecast();
        parsedForecast.setStockSymbol(symbol);
        parsedForecast.setStrikePrice(Double.parseDouble(strikePriceString));
        parsedForecast.setForecastType(getForecastType(forecastTypeString));
        parsedForecast.setExpirationDate(getExpirationDate(expirationDateString));
        return parsedForecast;
    }

    private String getExpirationDate(String expirationDateString) {
        String[] expirationDateStrings = expirationDateString.split("/");
        String month = expirationDateStrings[0];
        String day = expirationDateStrings[1];
        return String.format("%s-%s-%s", LocalDateTime.now(clock).getYear(), month, day);
    }

    private ForecastType getForecastType(String forecastTypeString) {
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

}
