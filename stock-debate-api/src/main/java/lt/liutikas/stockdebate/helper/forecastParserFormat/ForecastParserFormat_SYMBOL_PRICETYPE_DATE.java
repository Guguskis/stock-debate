package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ParsedForecast;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForecastParserFormat_SYMBOL_PRICETYPE_DATE implements ForecastParserFormat {

    private final Clock clock;
    private final String symbolPattern;
    private final String forecastTypePattern;
    private final String strikePricePattern;
    private final String expirationDatePattern;

    public ForecastParserFormat_SYMBOL_PRICETYPE_DATE(Clock clock, String symbolPattern, String forecastTypePattern, String strikePricePattern, String expirationDatePattern) {
        this.clock = clock;
        this.symbolPattern = symbolPattern;
        this.forecastTypePattern = forecastTypePattern;
        this.strikePricePattern = strikePricePattern;
        this.expirationDatePattern = expirationDatePattern;
    }

    @Override
    public boolean canParse(String text) {
        return getMatcher(text).find();
    }

    private Matcher getMatcher(String text) {
        Pattern pattern = Pattern.compile(getRegex());
        return pattern.matcher(text);
    }

    @Override
    public ParsedForecast parse(String text, LocalDateTime createdDate) {
        Matcher matcher = getMatcher(text);
        matcher.find();
        String symbol = matcher.group(1);
        String strikePriceString = matcher.group(2);
        String forecastTypeString = matcher.group(3);
        String expirationDateString = matcher.group(4);
        return ForecastParserUtil.getParsedForecast(symbol, strikePriceString, forecastTypeString, expirationDateString, clock, createdDate);
    }

    @Override
    public String removeForecast(String text) {
        Matcher matcher = getMatcher(text);
        matcher.find();
        String forecastString = String.format("\\Q%s\\E", matcher.group(0));
        return text.replaceFirst(forecastString, "");
    }

    private String getRegex() {
        return String.format("(%s) \\$?(%s)(%s) (%s)", symbolPattern, strikePricePattern, forecastTypePattern, expirationDatePattern);
    }
}
