package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.DateRange;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.model.opinion.*;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class OpinionService {

    private static final Logger LOG = LoggerFactory.getLogger(OpinionService.class);
    private static final int STEPS_FOR_OPINION_AGGREGATION = 40;
    private final SubredditRepository subredditRepository;
    private final OpinionRepository opinionRepository;
    private final Clock clock;

    public OpinionService(SubredditRepository subredditRepository, OpinionRepository opinionRepository, Clock clock) {
        this.subredditRepository = subredditRepository;
        this.opinionRepository = opinionRepository;
        this.clock = clock;
    }

    public ResponseEntity getOpinions(String subredditName, String stockSymbol, DateRange dateRange) {

        LocalDateTime startDateTime = dateRange.getStartDate(LocalDateTime.now(clock));
        List<Opinion> opinions;

        if (selectedAllSubreddits(subredditName)) {
            opinions = opinionRepository.findAllByStockSymbolAndCreatedAfterOrderByCreatedAsc(
                    stockSymbol.toUpperCase(),
                    startDateTime
            );
        } else {
            Subreddit subreddit = subredditRepository.findByNameIgnoreCase(subredditName);

            if (subreddit == null) {
                String errorMessage = String.format("Subreddit r/%s not found", subredditName);
                LOG.error(errorMessage);
                return ResponseEntity.badRequest().body(errorMessage);
            }

            opinions = opinionRepository.
                    findAllBySubredditAndStockSymbolAndCreatedAfterOrderByCreatedAsc(
                            subreddit, stockSymbol.toUpperCase(), startDateTime);
        }
        ArrayList<OpinionsDetail> opinionsDetails = getOpinionDetails(dateRange, opinions);

        SubredditOpinions subredditOpinions = new SubredditOpinions();
        subredditOpinions.setStockSymbol(stockSymbol);
        subredditOpinions.setSubredditName(subredditName);
        subredditOpinions.setDateRange(dateRange);
        subredditOpinions.setOpinionsDetails(opinionsDetails);

        LOG.info(String.format("Retrieved '%s' opinions for subreddit r/%s of '%s'", dateRange.toString(), Strings.isBlank(subredditName) ? "all" : subredditName, stockSymbol));

        return ResponseEntity.ok(subredditOpinions);
    }

    private boolean selectedAllSubreddits(String subredditName) {
        return Strings.isBlank(subredditName) || subredditName.equalsIgnoreCase("all");
    }

    private ArrayList<OpinionsDetail> getOpinionDetails(DateRange dateRange, List<Opinion> opinions) {
        ArrayList<OpinionsDetail> opinionsDetails = new ArrayList<>();

        // assuming oldest opinions are in the beginning of list
        long timeStepInSeconds = dateRange.getTimeStepInSeconds(STEPS_FOR_OPINION_AGGREGATION);
        LocalDateTime startDate = dateRange.getStartDate(LocalDateTime.now(clock));

        for (int i = 1; i <= STEPS_FOR_OPINION_AGGREGATION; i++) {
            LocalDateTime fromDate = startDate.plusSeconds((i - 1) * timeStepInSeconds);
            LocalDateTime toDate = i == STEPS_FOR_OPINION_AGGREGATION ? LocalDateTime.now(clock) : startDate.plusSeconds((i) * timeStepInSeconds);

            opinionsDetails.add(getOpinionDetail(opinions, fromDate, toDate));
        }

        return opinionsDetails;
    }

    private OpinionsDetail getOpinionDetail(List<Opinion> opinions, LocalDateTime fromDate, LocalDateTime toDate) {

        List<Opinion> opinionsInCurrentStepRange = opinions.stream()
                .filter(isOpinionInRange(fromDate, toDate))
                .collect(Collectors.toList());

        List<AggregatedOpinion> aggregatedOpinions = Arrays.stream(OpinionType.values())
                .map(opinionType -> getAggregatedOpinion(opinionsInCurrentStepRange, opinionType))
                .collect(Collectors.toList());

        OpinionsDetail opinionsDetail = new OpinionsDetail();

        opinionsDetail.setDate(toDate);
        opinionsDetail.setAggregatedOpinions(aggregatedOpinions);

        return opinionsDetail;
    }

    private Predicate<Opinion> isOpinionInRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return opinion -> opinion.getCreated().isAfter(fromDate) && opinion.getCreated().isBefore(toDate);
    }

    private AggregatedOpinion getAggregatedOpinion(List<Opinion> opinions, OpinionType opinionType) {
        AggregatedOpinion aggregatedOpinion = new AggregatedOpinion();

        List<Opinion> opinionsOfSameType = opinions.stream()
                .filter(opinion -> opinion.getOpinionType() == opinionType)
                .collect(Collectors.toList());

        aggregatedOpinion.setType(opinionType);
        aggregatedOpinion.setCount(opinionsOfSameType.size());

        return aggregatedOpinion;
    }
}
