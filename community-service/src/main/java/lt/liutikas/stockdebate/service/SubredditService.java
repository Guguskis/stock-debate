package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubredditService {

    private final SubredditRepository subredditRepository;

    public SubredditService(SubredditRepository subredditRepository) {
        this.subredditRepository = subredditRepository;
    }

    public ResponseEntity getSubreddits() {
        List<Subreddit> subreddits = subredditRepository.findAll();
        return ResponseEntity.ok(subreddits);
    }

    public ResponseEntity registerOpinionsCollection(String name) {
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(name);

        if (subreddit == null) {
            subreddit = new Subreddit();
            subreddit.setName(name);
            // todo check if subreddit exists
        }

        subreddit.setCollectOpinions(true);
        Subreddit savedSubreddit = subredditRepository.save(subreddit);

        return ResponseEntity.ok(savedSubreddit);
    }

    public ResponseEntity unregisterOpinionsCollection(String name) {
        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(name);

        if (subreddit == null) {
            return ResponseEntity.notFound().build();
        }

        subreddit.setCollectOpinions(false);
        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        return ResponseEntity.ok(savedSubreddit);
    }
}
