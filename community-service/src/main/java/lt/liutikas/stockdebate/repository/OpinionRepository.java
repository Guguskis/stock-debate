package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Opinion;
import lt.liutikas.stockdebate.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OpinionRepository extends JpaRepository<Opinion, Integer> {

    List<Opinion> findAllBySubredditAndCreatedAfter(Subreddit subreddit, LocalDateTime date);
}
