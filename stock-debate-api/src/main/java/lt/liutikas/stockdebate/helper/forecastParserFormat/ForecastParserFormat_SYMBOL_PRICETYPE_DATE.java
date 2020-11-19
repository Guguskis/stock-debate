package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ParsedForecast;

import java.time.Clock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForecastParserFormat_SYMBOL_PRICETYPE_DATE implements ForecastParserFormat {

    private final Clock clock;

    public ForecastParserFormat_SYMBOL_PRICETYPE_DATE(Clock clock) {
        this.clock = clock;
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
    public ParsedForecast parse(String text) {
        Matcher matcher = getMatcher(text);
        matcher.find();
        String symbol = matcher.group(1);
        String strikePriceString = matcher.group(2);
        String forecastTypeString = matcher.group(3);
        String expirationDateString = matcher.group(4);
        return ForecastParserUtil.getParsedForecast(symbol, strikePriceString, forecastTypeString, expirationDateString, clock);
    }

    @Override
    public String removeForecast(String text) {
        Matcher matcher = getMatcher(text);
        matcher.find();
        String forecastString = String.format("\\Q%s\\E", matcher.group(0));
        return text.replaceFirst(forecastString, "");
    }

    private String getRegex() {
        String symbolPattern = "[A-Z]{1,4}";
        String forecastTypePattern = "[cCpP]";
        String strikePricePattern = "[\\d\\.]+";
        String expirationDatePattern = "\\d{1,2}/\\d{1,2}";

        return String.format("(%s) \\$?(%s)(%s) (%s)", symbolPattern, strikePricePattern, forecastTypePattern, expirationDatePattern);
    }
}
