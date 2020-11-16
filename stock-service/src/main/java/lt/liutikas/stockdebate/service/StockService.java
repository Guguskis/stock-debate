package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class StockService {

    public static final String IEXAPIS = "https://sandbox.iexapis.com/stable/stock/%s/quote?token=%s";
    public static final String TOKEN = "Tpk_e8b0d9ec6d4047d6aaeed40d580dbf94";
    private final Logger LOG = LoggerFactory.getLogger(StockService.class);

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;

    public StockService(RestTemplate restTemplate, StockRepository stockRepository) {
        this.restTemplate = restTemplate;
        this.stockRepository = stockRepository;
    }

    public ResponseEntity getStock(String symbol) {
        Stock liveStock;
        try {
            liveStock = restTemplate.getForObject(String.format(IEXAPIS, symbol, TOKEN), Stock.class);
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve stock '%s', reason: %s", symbol, e.getMessage()));
            return ResponseEntity.notFound().build();
        }

        Stock databaseStock = stockRepository.findBySymbolIgnoreCase(symbol);
        if (databaseStock == null) {
            // todo retrieve logoUrl from web
            stockRepository.save(liveStock);
        } else {
            liveStock.setLogoUrl(databaseStock.getLogoUrl());
        }

        LOG.info(String.format("Retrieved stock '%s'", symbol));
        return ResponseEntity.ok(liveStock);
    }
}
