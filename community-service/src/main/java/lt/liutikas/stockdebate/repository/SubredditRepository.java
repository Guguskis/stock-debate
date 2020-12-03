package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Integer> {

    Subreddit findByNameIgnoreCase(String name);

    List<Subreddit> findAllByCollectOpinionsTrue();
}
