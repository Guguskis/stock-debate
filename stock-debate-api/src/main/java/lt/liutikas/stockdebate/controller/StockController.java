package lt.liutikas.stockdebate.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @PostMapping
    public String getStocks(@RequestBody String ticker) {
        return ticker;
    }
}
