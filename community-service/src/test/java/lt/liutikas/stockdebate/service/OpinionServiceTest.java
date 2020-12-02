package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.DateRange;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.model.opinion.AggregatedOpinion;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import lt.liutikas.stockdebate.model.opinion.OpinionType;
import lt.liutikas.stockdebate.model.opinion.SubredditOpinions;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpinionServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2020-12-02T00:00:00.00Z"), ZoneOffset.UTC);
    private static final String EXISTING_SUBREDDIT_NAME = "wallstreetbets";

    private OpinionService opinionService;
    private SubredditRepository subredditRepository;
    private OpinionRepository opinionRepository;

    @Before
    public void setUp() {
        subredditRepository = mock(SubredditRepository.class);
        opinionRepository = mock(OpinionRepository.class);
        opinionService = new OpinionService(subredditRepository, opinionRepository, CLOCK);
    }

    @Test
    public void getSubredditOpinions_providedValidData_returnsMappedData() {

        Subreddit subreddit = createSubreddit(EXISTING_SUBREDDIT_NAME);
        String stockSymbol = "PLTR";

        when(subredditRepository.findByNameIgnoreCase(EXISTING_SUBREDDIT_NAME))
                .thenReturn(subreddit);

        ResponseEntity responseEntity = opinionService.getOpinions(EXISTING_SUBREDDIT_NAME, stockSymbol, DateRange.DAY);
        SubredditOpinions subredditOpinions = (SubredditOpinions) responseEntity.getBody();

        assertEquals(stockSymbol, subredditOpinions.getStockSymbol());
        assertEquals(EXISTING_SUBREDDIT_NAME, subredditOpinions.getSubredditName());
        assertEquals(DateRange.DAY, subredditOpinions.getDateRange());
    }

    @Test
    public void getSubredditOpinions_providedDifferentOpinionTypesSameCreatedDate_returnsMultipleAggregatedOpinions() {

        LocalDateTime createdAfterDate = LocalDateTime.now(CLOCK).minusDays(1);
        Subreddit subreddit = createSubreddit(EXISTING_SUBREDDIT_NAME);
        String stockSymbol = "PLTR";

        List<Opinion> opinions = Arrays.asList(
                createOpinion(stockSymbol, OpinionType.NEUTRAL, LocalDateTime.of(2020, 12, 1, 0, 1)),
                createOpinion(stockSymbol, OpinionType.BUY, LocalDateTime.of(2020, 12, 1, 0, 1)),
                createOpinion(stockSymbol, OpinionType.SELL, LocalDateTime.of(2020, 12, 1, 0, 1))
        );

        when(subredditRepository.findByNameIgnoreCase(EXISTING_SUBREDDIT_NAME))
                .thenReturn(subreddit);
        when(opinionRepository.findAllBySubredditAndStockSymbolAndCreatedAfterOrderByCreatedAsc(subreddit, stockSymbol, createdAfterDate))
                .thenReturn(opinions);

        ResponseEntity responseEntity = opinionService.getOpinions(EXISTING_SUBREDDIT_NAME, stockSymbol, DateRange.DAY);
        SubredditOpinions subredditOpinions = (SubredditOpinions) responseEntity.getBody();

        List<AggregatedOpinion> opinionDetails = subredditOpinions.getOpinionsDetails().get(0).getAggregatedOpinions();
        assertEquals(3, opinionDetails.size());

        assertAggregatedOpinion(opinionDetails.get(0), 1, OpinionType.NEUTRAL);
        assertAggregatedOpinion(opinionDetails.get(1), 1, OpinionType.BUY);
        assertAggregatedOpinion(opinionDetails.get(2), 1, OpinionType.SELL);

    }

    private void assertAggregatedOpinion(AggregatedOpinion aggregatedOpinion, int count, OpinionType opinionType) {
        assertEquals(count, aggregatedOpinion.getCount());
        assertEquals(opinionType, aggregatedOpinion.getType());
    }


    private Opinion createOpinion(String stockSymbol, OpinionType opinionType, LocalDateTime created) {
        Opinion opinion = new Opinion();
        opinion.setCreated(created);
        opinion.setOpinionType(opinionType);
        opinion.setStockSymbol(stockSymbol);
        return opinion;
    }

    private Subreddit createSubreddit(String existingSubredditName) {
        Subreddit subreddit = new Subreddit();
        subreddit.setId(5);
        subreddit.setName(existingSubredditName);
        subreddit.setCollectOpinions(true);

        return subreddit;
    }
}