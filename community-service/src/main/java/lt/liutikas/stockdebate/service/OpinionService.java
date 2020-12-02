package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.DateRange;
import lt.liutikas.stockdebate.model.Opinion;
import lt.liutikas.stockdebate.model.OpinionType;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OpinionService {

    private final Logger LOG = LoggerFactory.getLogger(OpinionService.class);

    private final SubredditRepository subredditRepository;
    private final OpinionRepository opinionRepository;

    public OpinionService(SubredditRepository subredditRepository, OpinionRepository opinionRepository) {
        this.subredditRepository = subredditRepository;
        this.opinionRepository = opinionRepository;
    }

    public ResponseEntity getOpinions(String subredditName, DateRange dateRange) {

        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(subredditName);
        if (subreddit == null) {
            return ResponseEntity.notFound().build();
        }

        Opinion opinion = new Opinion();
        opinion.setCreated(LocalDateTime.now());
        opinion.setOpinionType(OpinionType.NEUTRAL);
        opinion.setStockSymbol("PLTR");
        opinion.setSubreddit(subreddit);
        opinionRepository.save(opinion);

        LocalDateTime startDateTime = dateRange.getDateTime();
        List<Opinion> subredditOpinions = opinionRepository.findAllBySubredditAndCreatedAfter(subreddit, startDateTime);

        return ResponseEntity.ok(subredditOpinions);
    }
}
