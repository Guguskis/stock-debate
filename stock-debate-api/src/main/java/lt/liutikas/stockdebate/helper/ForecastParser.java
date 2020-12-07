package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.helper.forecastParserFormat.*;
import lt.liutikas.stockdebate.model.ParsedForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ForecastParser {

    private final static Logger LOG = LoggerFactory.getLogger(ForecastParser.class);

    private final List<ForecastParserFormat> forecastParserFormats;

    public ForecastParser(Clock clock) {
        String symbolPattern = "[A-Za-z]{1,4}";
        String forecastTypePattern = "call|put|CALL|PUT|[cCpP]";
        String strikePricePattern = "[\\d\\.]+";
        String expirationDateShortPattern = "\\d{1,2}/\\d{1,2}";
        String expirationDateLongPattern = "\\d{1,2}/\\d{1,2}/\\d{1,2}";
        String expirationDatePattern = expirationDateLongPattern + "|" + expirationDateShortPattern;

        this.forecastParserFormats = Arrays.asList(
                new ForecastParserFormat_SYMBOL_PRICETYPE_DATE(clock, symbolPattern, forecastTypePattern, strikePricePattern, expirationDatePattern),
                new ForecastParserFormat_SYMBOL_DATE_PRICETYPE(clock, symbolPattern, forecastTypePattern, strikePricePattern, expirationDatePattern),
                new ForecastParserFormat_DATE_SYMBOL_PRICE_TYPE(clock, symbolPattern, forecastTypePattern, strikePricePattern, expirationDatePattern),
                new ForecastParserFormat_PRICE_DATE_SYMBOL_TYPE(clock, symbolPattern, forecastTypePattern, strikePricePattern, expirationDatePattern)
        );
    }

    public List<ParsedForecast> parse(String text, LocalDateTime createdDate) {
        List<ParsedForecast> parsedForecasts = new ArrayList<>();

        for (int i = 0; i < forecastParserFormats.size(); i++) {
            ForecastParserFormat parserFormat = forecastParserFormats.get(i);
            if (parserFormat.canParse(text)) {
                try {
                    ParsedForecast parsedForecast = parserFormat.parse(text, createdDate);
                    parsedForecast.setCreatedDate(createdDate);
                    parsedForecasts.add(parsedForecast);
                } catch (Exception e) {
                    LOG.warn("Failed to parse forecast", e);
                }
                text = parserFormat.removeForecast(text);
                i = -1;
            }
        }

        return parsedForecasts;
    }

}
