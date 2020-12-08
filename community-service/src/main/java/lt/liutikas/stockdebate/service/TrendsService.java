package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.MapUtil;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.model.Trend;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.StockRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TrendsService {

    private final static Logger LOG = LoggerFactory.getLogger(TrendsService.class);
    private final OpinionRepository opinionRepository;
    private final StockRepository stockRepository;
    private final SubredditRepository subredditRepository;
    private final Clock clock;

    public TrendsService(OpinionRepository opinionRepository, StockRepository stockRepository, SubredditRepository subredditRepository, Clock clock) {
        this.opinionRepository = opinionRepository;
        this.stockRepository = stockRepository;
        this.subredditRepository = subredditRepository;
        this.clock = clock;
    }

    public ResponseEntity getTrends(String subredditName) {

        List<Opinion> opinions;

        if (selectedAllSubreddits(subredditName)) {
            opinions = opinionRepository.findAll();
        } else {
            Subreddit subreddit = subredditRepository.findByNameIgnoreCase(subredditName);

            if (subreddit == null) {
                String message = String.format("Subreddit r/%s not found", subredditName);
                LOG.error(message);
                return ResponseEntity.badRequest().body(message);
            }
            opinions = opinionRepository.findAllBySubreddit(subreddit);
        }

        Map<String, Long> stockSymbolsByCount = opinions.stream()
                .collect(Collectors.groupingBy(Opinion::getStockSymbol, Collectors.counting()));

        List<Trend> trends = MapUtil.sortByValueDesc(stockSymbolsByCount)
                .entrySet().stream()
                .limit(25)
                .map(item -> assembleTrend(subredditName, item.getKey(), item.getValue()))
                .collect(Collectors.toList());

        LOG.info(String.format("Retrieved trends for r/%s", Strings.isBlank(subredditName) ? "all" : subredditName));

        return ResponseEntity.ok(trends);
    }

    private Trend assembleTrend(String subredditName, String stockSymbol, Long opinionsTotal) {
        LocalDateTime dayBefore = LocalDateTime.now(clock).minusDays(1);

        List<Opinion> opinionsSinceLastDay = opinionRepository.findAllByStockSymbolAndCreatedAfter(stockSymbol, dayBefore);

        if (!selectedAllSubreddits(subredditName)) {
            opinionsSinceLastDay = opinionsSinceLastDay.stream()
                    .filter(opinion -> opinion.getSubreddit().getName().equalsIgnoreCase(subredditName))
                    .collect(Collectors.toList());
        }

        Trend trend = new Trend();

        trend.setStock(stockRepository.getStock(stockSymbol));
        trend.setOpinionsTotal(opinionsTotal.intValue());
        trend.setOpinionsLastDay(5);
        trend.setOpinionsLastDay(opinionsSinceLastDay.size());

        return trend;
    }

    private boolean selectedAllSubreddits(String subredditName) {
        return Strings.isBlank(subredditName) || subredditName.equalsIgnoreCase("all");
    }

}
