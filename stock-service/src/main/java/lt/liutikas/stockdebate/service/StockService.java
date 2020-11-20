package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.FullStockDetail;
import lt.liutikas.stockdebate.model.SimpleStockDetail;
import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class StockService {

    private static final String GET_STOCK_IEXAPI = "/stock/%s/quote?token=%s";
    private static final String GET_SINGLE_STOCK_PRICE_IEXAPI = "/stock/%s/chart/date/%s?token=%s&chartLast=1";
    private static final String TOKEN = "Tpk_e8b0d9ec6d4047d6aaeed40d580dbf94";
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
            String url = String.format(GET_STOCK_IEXAPI, symbol, TOKEN);
            liveStock = restTemplate.getForObject(url, Stock.class);
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

        liveStock.setLogoUrl("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fautoconsignmentofsandiego.com%2Fimages%2FTesla_Logo.png");

        LOG.info(String.format("Retrieved stock '%s'", symbol));
        return ResponseEntity.ok(liveStock);
    }

    public ResponseEntity getStockPrice(String symbol, String dateString) {

        ResponseEntity<List<FullStockDetail>> response;
        try {
            String formattedDateString = dateString.replace("-", "");
            String url = String.format(GET_SINGLE_STOCK_PRICE_IEXAPI, symbol, formattedDateString, TOKEN);
            response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve stockDetail for stock '%s' at date '%s'. Reason: '%s'", symbol, dateString, e.getMessage()));
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        if (response.getBody().size() == 0) {
            String message = String.format("Stock price for day '%s' is unavailable, maybe holiday?", dateString);
            LOG.warn(message);
            return ResponseEntity.badRequest().body(message);
        }

        FullStockDetail fullStockDetail = response.getBody().get(0);
        SimpleStockDetail simpleStockDetail = mapToSimpleStockDetail(fullStockDetail, symbol);
        LOG.info(String.format("Retrieved details for stock '%s' at date '%s'", symbol, dateString));
        return ResponseEntity.ok(simpleStockDetail);
    }

    private SimpleStockDetail mapToSimpleStockDetail(FullStockDetail fullStockDetail, String symbol) {
        SimpleStockDetail simpleStockDetail = new SimpleStockDetail();
        simpleStockDetail.setSymbol(symbol.toUpperCase());
        simpleStockDetail.setPrice(fullStockDetail.getClose());
        simpleStockDetail.setDate(fullStockDetail.getDate());
        return simpleStockDetail;
    }
}
