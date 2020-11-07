package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface StockRepository extends JpaRepository<Stock, Integer> {
    Stock findByTicker(String ticker);
}
