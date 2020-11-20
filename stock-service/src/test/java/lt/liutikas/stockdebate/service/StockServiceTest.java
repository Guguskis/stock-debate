package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    private StockService stockService;
    private RestTemplate restTemplate;
    private StockRepository stockRepository;


    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        stockRepository = mock(StockRepository.class);
        stockService = new StockService(restTemplate, stockRepository);
    }

    @Test
    public void getStock_ExistingSymbolProvidedAndNotStoredInDatabase_ReturnsStock() {
        Stock teslaStock = getTeslaStock();

        when(restTemplate.getForObject(anyString(), eq(Stock.class)))
                .thenReturn(teslaStock);


        ResponseEntity response = stockService.getStock("TSLA");
        Stock stock = (Stock) response.getBody();

        verify(stockRepository, times(1)).save(any());
        assertEquals(stock.getCompanyName(), teslaStock.getCompanyName());
        assertEquals(stock.getSymbol(), teslaStock.getSymbol());
        assertEquals(stock.getLatestPrice(), teslaStock.getLatestPrice());
        assertEquals(stock.getLogoUrl(), teslaStock.getLogoUrl());
    }

    @Test
    public void getStock_NotExistingSymbolProvidedAndNotStoredInDatabase_ReturnsNotFound() {
        when(restTemplate.getForObject(anyString(), eq(Stock.class)))
                .thenThrow(new RestClientException("Not found"));

        ResponseEntity response = stockService.getStock("TSLA");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getStock_ExistingSymbolProvidedAndStoredInDatabase_ReturnsStock() {
        Stock teslaStock = getTeslaStock();
        Stock teslaStockInDatabase = getTeslaStock();
        teslaStockInDatabase.setLogoUrl("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fautoconsignmentofsandiego.com%2Fimages%2FTesla_Logo.png");

        when(restTemplate.getForObject(anyString(), eq(Stock.class)))
                .thenReturn(teslaStock);

        when(stockRepository.findBySymbolIgnoreCase("TSLA"))
                .thenReturn(teslaStockInDatabase);

        ResponseEntity response = stockService.getStock("TSLA");
        Stock stock = (Stock) response.getBody();

        assertEquals(stock.getCompanyName(), teslaStockInDatabase.getCompanyName());
        assertEquals(stock.getSymbol(), teslaStockInDatabase.getSymbol());
        assertEquals(stock.getLatestPrice(), teslaStockInDatabase.getLatestPrice());
        assertEquals(stock.getLogoUrl(), teslaStockInDatabase.getLogoUrl());
    }

    private Stock getTeslaStock() {
        return new Stock() {{
            setCompanyName("Tesla");
            setSymbol("TSLA");
            setLatestPrice(420.0);
            setLogoUrl("");
        }};
    }
}