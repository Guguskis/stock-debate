package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Integer> {

    List<Opinion> findAllBySubredditAndStockSymbolAndCreatedAfterOrderByCreatedAsc(Subreddit subreddit, String stockSymbol, LocalDateTime date);

    List<Opinion> findAllByStockSymbolAndCreatedAfterOrderByCreatedAsc(String stockSymbols, LocalDateTime date);
}
