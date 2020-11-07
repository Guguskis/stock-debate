package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Stock;
import lt.liutikas.stockdebate.model.dto.GetStockRequest;

public interface StockService {
    Stock getStock(GetStockRequest request);
}
