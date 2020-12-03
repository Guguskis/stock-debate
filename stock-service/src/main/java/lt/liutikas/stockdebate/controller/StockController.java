package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity getStock(@PathVariable String symbol) {
        return stockService.getStock(symbol);
    }

    @GetMapping("/{symbol}/price/{date}")
    public ResponseEntity getStockPrice(@PathVariable String symbol, @PathVariable String date) {
        return stockService.getStockPrice(symbol, date);
    }

    @GetMapping("/{symbol}/exists")
    public ResponseEntity isStockExists(@PathVariable String symbol) {
        return stockService.isStockExists(symbol);
    }
}
