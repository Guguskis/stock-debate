package lt.liutikas.stockdebate.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Stock {

    @Id
    public String symbol;
    public String companyName;
    public Double latestPrice;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String name) {
        this.companyName = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String ticker) {
        this.symbol = ticker;
    }

    public Double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(Double price) {
        this.latestPrice = price;
    }
}
