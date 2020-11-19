package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ParsedForecast;

import java.time.LocalDate;

public interface ForecastParserFormat {

    boolean canParse(String text);

    ParsedForecast parse(String text, LocalDate createdDate);

    String removeForecast(String text);
}
