package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ParsedForecast;

import java.time.LocalDateTime;

public interface ForecastParserFormat {

    boolean canParse(String text);

    ParsedForecast parse(String text, LocalDateTime createdDate);

    String removeForecast(String text);
}
