package lt.liutikas.stockdebate.helper.forecastParserFormat;

import lt.liutikas.stockdebate.model.ParsedForecast;

public interface ForecastParserFormat {

    boolean canParse(String text);

    ParsedForecast parse(String text);

    String removeForecast(String text);
}
