package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.model.dto.GetStockRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;

public class StockServiceTest {

    private StockService stockService;
    private RestTemplate restTemplate;


    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
//        stockService = new DefaultStockService(restTemplate, new StockSourceProperties());
    }

    @Test
    public void getStock_TickerProvided_ReturnsStock() {
        GetStockRequest request = new GetStockRequest() {{
            setSymbol("TSLA");
        }};
        Stock teslaStock = getTeslaStock();

//        when(
//                restTemplate.getForObject(eq(String.class), eq(Stock.class), any())
//        ).thenReturn(teslaStock);

        Stock actualStock = stockService.getStock(request);

        assertEquals(actualStock.getCompanyName(), teslaStock.getCompanyName());
        assertEquals(actualStock.getSymbol(), teslaStock.getSymbol());
        assertEquals(actualStock.getLatestPrice(), teslaStock.getLatestPrice());
    }

    private Stock getTeslaStock() {
        return new Stock() {{
            setCompanyName("Tesla");
            setSymbol("TSLA");
            setLatestPrice(420.0);
        }};
    }
}