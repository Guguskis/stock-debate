package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.SimpleStockDetail;
import lt.liutikas.stockdebate.model.Stock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class StockRepository {

    private final RestTemplate stockTemplate;

    public StockRepository(@Qualifier("stock") RestTemplate stockTemplate) {
        this.stockTemplate = stockTemplate;
    }

    public Stock getStock(String stockSymbol) {
        String getStockUrl = String.format("/api/stock/%s", stockSymbol);
        Stock stock = stockTemplate.getForObject(getStockUrl, Stock.class);
        return stock;
    }

    public SimpleStockDetail getSimpleStockDetail(String stockSymbol, String expirationDate) {
        // expirationDate string format 2020-11-20
        String getStockDetailUrl = String.format("/api/stock/%s/price/%s", stockSymbol, expirationDate);
        SimpleStockDetail simpleStockDetail = stockTemplate.getForObject(getStockDetailUrl, SimpleStockDetail.class);
        return simpleStockDetail;
    }
}
