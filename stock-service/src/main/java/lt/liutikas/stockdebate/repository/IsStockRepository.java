package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.IsStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IsStockRepository extends JpaRepository<IsStock, Integer> {

    IsStock findByStockSymbol(String stockSymbol);

}
