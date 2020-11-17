package lt.liutikas.stockdebate.model;

import java.util.List;

public class RedditUser {

    private String username;
    private List<Forecast> forecasts;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }
}
