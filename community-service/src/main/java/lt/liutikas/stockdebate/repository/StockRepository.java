package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.IsStock;
import lt.liutikas.stockdebate.model.Stock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class StockRepository {

    private static final String GET_STOCK_URL = "/api/stock/%s";
    private static final String GET_IS_STOCK_URL = "/api/stock/%s/exists";

    private final RestTemplate stockTemplate;

    public StockRepository(@Qualifier("stock") RestTemplate stockTemplate) {
        this.stockTemplate = stockTemplate;
    }

    public Stock getStock(String stockSymbol) {
        String getStockUrl = String.format(GET_STOCK_URL, stockSymbol);
        Stock stock = stockTemplate.getForObject(getStockUrl, Stock.class);
        return stock;
    }

    public boolean isStock(String stockSymbol) {
        String getStockUrl = String.format(GET_IS_STOCK_URL, stockSymbol);
        IsStock stock = stockTemplate.getForObject(getStockUrl, IsStock.class); // todo implement in discussion service
        return stock.isStock();
    }

}
