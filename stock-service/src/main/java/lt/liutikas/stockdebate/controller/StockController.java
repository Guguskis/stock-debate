package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.model.dto.GetStockRequest;
import lt.liutikas.stockdebate.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public Stock getStocks(@RequestBody GetStockRequest request) {
        return stockService.getStock(request);
    }
}
