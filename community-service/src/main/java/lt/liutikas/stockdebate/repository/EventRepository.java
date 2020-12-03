package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    Event findTopByAnalyzedFalse();

    Event findBySubredditNameAndPostId(String subreddit, String postId);

}
