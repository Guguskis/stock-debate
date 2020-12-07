package lt.liutikas.stockdebate.model;

public class Trend {

    private Stock stock;
    private int opinionsTotal;
    private int opinionsLastDay;

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public int getOpinionsTotal() {
        return opinionsTotal;
    }

    public void setOpinionsTotal(int opinionsTotal) {
        this.opinionsTotal = opinionsTotal;
    }

    public int getOpinionsLastDay() {
        return opinionsLastDay;
    }

    public void setOpinionsLastDay(int opinionsLastDay) {
        this.opinionsLastDay = opinionsLastDay;
    }
}
