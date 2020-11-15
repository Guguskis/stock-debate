package lt.liutikas.stockdebate.model;

public class Forecast {
    private Stock stock;
    private String username;
    private double expirationPrice;
    private double targetPrice;
    private double successCoefficient;
    private String expirationDate;
    private String createdDate;
    private ForecastType forecastType;

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getExpirationPrice() {
        return expirationPrice;
    }

    public void setExpirationPrice(double expirationPrice) {
        this.expirationPrice = expirationPrice;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public double getSuccessCoefficient() {
        return successCoefficient;
    }

    public void setSuccessCoefficient(double successCoefficient) {
        this.successCoefficient = successCoefficient;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public ForecastType getForecastType() {
        return forecastType;
    }

    public void setForecastType(ForecastType forecastType) {
        this.forecastType = forecastType;
    }
}
