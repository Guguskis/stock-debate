package lt.liutikas.stockdebate.model.dto;

import org.springframework.stereotype.Component;

@Component
public class GetStockRequest {
    public String ticker;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
}
