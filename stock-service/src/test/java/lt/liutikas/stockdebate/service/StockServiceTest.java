package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.model.dto.GetStockRequest;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StockServiceTest {

    private StockService stockService;
    private StockRepository stockRepository;


    @Before
    public void setUp() {
        stockRepository = mock(StockRepository.class);

        this.stockService = new DefaultStockService(
                stockRepository
        );


    }

    @Test
    public void getStock_TickerProvided_ReturnsStock() {
        GetStockRequest request = new GetStockRequest() {{
            setTicker("TSLA");
        }};
        Stock teslaStock = getTeslaStock();

        when(
                stockRepository.findByTicker("TSLA")
        ).thenReturn(teslaStock);

        Stock actualStock = stockService.getStock(request);

        assertEquals(actualStock.getName(), teslaStock.getName());
        assertEquals(actualStock.getTicker(), teslaStock.getTicker());
        assertEquals(actualStock.getPrice(), teslaStock.getPrice());
    }

    private Stock getTeslaStock() {
        return new Stock() {{
            setName("Tesla");
            setTicker("TSLA");
            setPrice(420);
        }};
    }
}