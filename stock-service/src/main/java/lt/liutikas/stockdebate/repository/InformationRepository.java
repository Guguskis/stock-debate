package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.FullStockDetail;
import lt.liutikas.stockdebate.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository
public class InformationRepository {
    private static final Logger LOG = LoggerFactory.getLogger(InformationRepository.class);
    public static final String GET_STOCK_IEXAPI = "/stock/%s/quote?token=%s";
    public static final String GET_SINGLE_STOCK_PRICE_IEXAPI = "/stock/%s/chart/date/%s?token=%s&chartLast=1";
    public static final String TOKEN = "Tpk_e8b0d9ec6d4047d6aaeed40d580dbf94";
    private final RestTemplate restTemplate;

    public InformationRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Stock getStock(String symbol) {
        Stock liveStock;
        try {
            String url = String.format(GET_STOCK_IEXAPI, symbol, TOKEN);
            liveStock = restTemplate.getForObject(url, Stock.class);
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve stock '%s', reason: %s", symbol, e.getMessage()));
            return null;
        }
        return liveStock;
    }

    public List<FullStockDetail> getFullStockDetails(String symbol, String dateString) {
        ResponseEntity<List<FullStockDetail>> response;
        try {
            String formattedDateString = dateString.replace("-", "");
            String url = String.format(GET_SINGLE_STOCK_PRICE_IEXAPI, symbol, formattedDateString, TOKEN);
            response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                    });
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve stockDetail for stock '%s' at date '%s'. Reason: '%s'", symbol, dateString, e.getMessage()));
            return null;
        }
        return response.getBody();
    }
}
