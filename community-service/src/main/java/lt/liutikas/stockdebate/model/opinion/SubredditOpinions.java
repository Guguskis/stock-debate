package lt.liutikas.stockdebate.model.opinion;

import lt.liutikas.stockdebate.model.DateRange;

import java.util.List;

public class SubredditOpinions {

    private String subredditName;
    private String stockSymbol;
    private DateRange dateRange;
    private List<OpinionsDetail> opinionsDetails;

    public String getSubredditName() {
        return subredditName;
    }

    public void setSubredditName(String subredditName) {
        this.subredditName = subredditName;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public List<OpinionsDetail> getOpinionsDetails() {
        return opinionsDetails;
    }

    public void setOpinionsDetails(List<OpinionsDetail> opinionsDetails) {
        this.opinionsDetails = opinionsDetails;
    }
}
