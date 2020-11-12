package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.configuration.StockSourceProperties;
import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.model.dto.GetStockRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class DefaultStockService implements StockService {

    private static final String IEXAPIS = "https://sandbox.iexapis.com/stable/stock/{symbol}/quote?token={token}";
    private final Logger LOG = LoggerFactory.getLogger(DefaultStockService.class);
    private final RestTemplate restTemplate;
    private final StockSourceProperties stockSourceProperties;

    public DefaultStockService(RestTemplate restTemplate, StockSourceProperties stockSourceProperties) {
        this.restTemplate = restTemplate;
        this.stockSourceProperties = stockSourceProperties;
    }

    @Override
    public Stock getStock(GetStockRequest request) {
        LOG.info(String.format("Retrieving stock '%s'", request.getSymbol()));
        Stock result = null;
        try {
            result = restTemplate.getForObject(IEXAPIS, Stock.class, request.getSymbol(), stockSourceProperties.getToken());
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve stock '%s', reason: %s", request.getSymbol(), e.getMessage()));
        }
        return result;
    }
}
