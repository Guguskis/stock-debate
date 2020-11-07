package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.model.dto.GetStockRequest;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.springframework.stereotype.Component;

@Component
public class DefaultStockService implements StockService {

    private final StockRepository stockRepository;

    public DefaultStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Stock getStock(GetStockRequest request) {
        return stockRepository.findByTicker(request.getTicker());
    }
}
