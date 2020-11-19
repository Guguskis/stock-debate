package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.helper.forecastParserFormat.ForecastParserFormat;
import lt.liutikas.stockdebate.helper.forecastParserFormat.ForecastParserFormat_SYMBOL_PRICETYPE_DATE;
import lt.liutikas.stockdebate.model.ParsedForecast;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ForecastParser {

    private final List<ForecastParserFormat> forecastParserFormats;

    public ForecastParser(Clock clock) {
        this.forecastParserFormats = Arrays.asList(
                new ForecastParserFormat_SYMBOL_PRICETYPE_DATE(clock)
        );
    }

    public List<ParsedForecast> parse(String text) {
        List<ParsedForecast> parsedForecasts = new ArrayList<>();

        for (int i = 0; i < forecastParserFormats.size(); i++) {
            ForecastParserFormat parserFormat = forecastParserFormats.get(i);
            if (parserFormat.canParse(text)) {
                ParsedForecast parsedForecast = parserFormat.parse(text);
                parsedForecasts.add(parsedForecast);
                text = parserFormat.removeForecast(text);
                i = -1;
            }
        }

        return parsedForecasts;
    }

}
