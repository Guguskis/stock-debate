package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.MapUtil;
import lt.liutikas.stockdebate.model.Trend;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.StockRepository;
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

    private final OpinionRepository opinionRepository;
    private final StockRepository stockRepository;
    private final Clock clock;

    private final static Logger LOG = LoggerFactory.getLogger(TrendsService.class);

    public TrendsService(OpinionRepository opinionRepository, StockRepository stockRepository, Clock clock) {
        this.opinionRepository = opinionRepository;
        this.stockRepository = stockRepository;
        this.clock = clock;
    }

    public ResponseEntity getTrends() {

        List<Opinion> opinions = opinionRepository.findAll();

        Map<String, Long> stockSymbolsByCount = opinions.stream()
                .collect(Collectors.groupingBy(Opinion::getStockSymbol, Collectors.counting()));

        List<Trend> trends = MapUtil.sortByValueDesc(stockSymbolsByCount)
                .entrySet().stream()
                .limit(25)
                .map(this::assembleTrend)
                .collect(Collectors.toList());

        return ResponseEntity.ok(trends);
    }

    private Trend assembleTrend(Map.Entry<String, Long> item) {
        String stockSymbol = item.getKey();
        Long opinionsTotal = item.getValue();
        LocalDateTime dayBefore = LocalDateTime.now(clock).minusDays(1);
        List<Opinion> opinionsSinceLastDay = opinionRepository.findAllByStockSymbolAndCreatedAfter(stockSymbol, dayBefore);

        Trend trend = new Trend();

        trend.setStock(stockRepository.getStock(stockSymbol));
        trend.setOpinionsTotal(opinionsTotal.intValue());
        trend.setOpinionsLastDay(5);
        trend.setOpinionsLastDay(opinionsSinceLastDay.size());

        return trend;
    }
}
