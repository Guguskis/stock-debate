package lt.liutikas.stockdebate.model;

public class Forecast {
    private Stock stock;
    private double expirationPrice;
    private double strikePrice;
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

    public double getExpirationPrice() {
        return expirationPrice;
    }

    public void setExpirationPrice(double expirationPrice) {
        this.expirationPrice = expirationPrice;
    }

    public double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(double strikePrice) {
        this.strikePrice = strikePrice;
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
