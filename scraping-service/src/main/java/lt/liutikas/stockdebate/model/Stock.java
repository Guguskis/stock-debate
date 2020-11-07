package lt.liutikas.stockdebate.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Stock {

    @Id
    public String ticker;
    public String name;
    public Integer price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
