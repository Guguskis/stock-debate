package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.DateRange;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.model.opinion.*;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpinionService {

    private final Logger LOG = LoggerFactory.getLogger(OpinionService.class);

    private final SubredditRepository subredditRepository;
    private final OpinionRepository opinionRepository;
    private final Clock clock;

    public OpinionService(SubredditRepository subredditRepository, OpinionRepository opinionRepository, Clock clock) {
        this.subredditRepository = subredditRepository;
        this.opinionRepository = opinionRepository;
        this.clock = clock;
    }

    public ResponseEntity getOpinions(String subredditName, String stockSymbol, DateRange dateRange) {

        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(subredditName);
        if (subreddit == null) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime startDateTime = dateRange.getStartDate(LocalDateTime.now(clock));
        List<Opinion> opinions = opinionRepository.
                findAllBySubredditAndStockSymbolAndCreatedAfterOrderByCreatedAsc(
                        subreddit, stockSymbol, startDateTime);

        ArrayList<OpinionDetail> opinionDetails = getOpinionDetails(dateRange, opinions);

        SubredditOpinions subredditOpinions = new SubredditOpinions();
        subredditOpinions.setStockSymbol(stockSymbol);
        subredditOpinions.setSubredditName(subredditName);
        subredditOpinions.setOpinionDetails(opinionDetails);

        return ResponseEntity.ok(subredditOpinions);
    }

    private ArrayList<OpinionDetail> getOpinionDetails(DateRange dateRange, List<Opinion> opinions) {
        ArrayList<OpinionDetail> opinionDetails = new ArrayList<>();


        // assuming oldest opinions are in the beginning of list
        int steps = 100;
        long timeStepInSeconds = dateRange.getTimeStepInSeconds(steps);
        LocalDateTime startDate = dateRange.getStartDate(LocalDateTime.now(clock));

        for (int i = 1; i < steps; i++) {

            LocalDateTime fromDate = startDate.plusSeconds((i - 1) * timeStepInSeconds);
            LocalDateTime toDate = startDate.plusSeconds((i) * timeStepInSeconds);

            List<Opinion> opinionsInCurrentStepRange = opinions.stream()
                    .filter(opinion ->
                            opinion.getCreated().isAfter(fromDate) &&
                                    opinion.getCreated().isBefore(toDate))
                    .collect(Collectors.toList());

            OpinionDetail opinionDetail = new OpinionDetail();
            opinionDetail.setDate(toDate);

            List<AggregatedOpinion> aggregatedOpinions = Arrays.stream(OpinionType.values()).map(opinionType -> {
                AggregatedOpinion aggregatedOpinion = new AggregatedOpinion();

                List<Opinion> opinionsOfType = opinionsInCurrentStepRange.stream()
                        .filter(opinion -> opinion.getOpinionType() == opinionType)
                        .collect(Collectors.toList());
                aggregatedOpinion.setType(opinionType);
                aggregatedOpinion.setCount(opinionsOfType.size());
                return aggregatedOpinion;
            }).collect(Collectors.toList());

            opinionDetail.setAggregatedOpinions(aggregatedOpinions);

            opinionDetails.add(opinionDetail);
        }

        return opinionDetails;
    }
}
