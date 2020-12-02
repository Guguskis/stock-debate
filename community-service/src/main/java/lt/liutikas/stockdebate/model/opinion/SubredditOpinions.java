package lt.liutikas.stockdebate.model.opinion;

import java.util.List;

public class SubredditOpinions {

    private String subredditName;
    private String stockSymbol;
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

    public List<OpinionsDetail> getOpinionDetails() {
        return opinionsDetails;
    }

    public void setOpinionDetails(List<OpinionsDetail> opinionsDetails) {
        this.opinionsDetails = opinionsDetails;
    }
}
