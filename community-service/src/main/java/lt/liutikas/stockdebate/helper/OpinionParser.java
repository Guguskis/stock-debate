package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import lt.liutikas.stockdebate.model.opinion.OpinionType;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class OpinionParser {

    private static final String STOCK_SYMBOL_PATTERN = "[A-Z]{2,4}";

    private final List<String> positiveWords = Arrays.asList("bull", "buy", "cheap", "coming back", "don't worry", "good", "great", "long", "moon", "nothing to worry", "rally", "up");
    private final List<String> negativeWords = Arrays.asList("FOMO", "bad", "bear", "down", "lose", "lost", "panic", "plummet", "pump and dump", "sell", "shit", "short", "tank", "underperform", "worry");

    private final StockRepository stockRepository;

    public OpinionParser(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<Opinion> parseComment(Comment comment) {
        String text = comment.getText();

        List<String> stockSymbols = getStockSymbols(text);

        if (stockSymbols.size() == 0) {
            return Collections.emptyList();
        }

        // fixme parse multiple opinions per comment
        String stockSymbol = stockSymbols.get(0);

        int positiveWordsCount = getOccurrenceCount(text, positiveWords);
        int negativeWordsCount = getOccurrenceCount(text, negativeWords);

        Opinion opinion = new Opinion();

        opinion.setOpinionType(getOpinionType(positiveWordsCount, negativeWordsCount));
        opinion.setStockSymbol(stockSymbol);
        opinion.setCreated(comment.getCreationDate());

        return Arrays.asList(opinion);
    }

    private OpinionType getOpinionType(int positiveWordsCount, int negativeWordsCount) {
        OpinionType opinionType;
        if (positiveWordsCount == negativeWordsCount) {
            opinionType = OpinionType.NEUTRAL;
        } else if (positiveWordsCount > negativeWordsCount) {
            opinionType = OpinionType.BUY;
        } else {
            opinionType = OpinionType.SELL;
        }
        return opinionType;
    }

    private int getOccurrenceCount(String text, List<String> wordsToCount) {
        String wordsPattern = String.join("|", wordsToCount);

        int count = 0;
        Pattern pattern = Pattern.compile(wordsPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            count++;
        }

        return count;
    }

    private List<String> getStockSymbols(String text) {
        Pattern pattern = Pattern.compile(OpinionParser.STOCK_SYMBOL_PATTERN);
        Matcher matcher = pattern.matcher(text);

        List<String> unverifiedStockSymbols = new ArrayList<>();

        while (matcher.find()) {
            String stockSymbol = matcher.group(0);
            unverifiedStockSymbols.add(stockSymbol);
        }

        return unverifiedStockSymbols.stream()
                .filter(stockRepository::isStock)
                .collect(Collectors.toList());
    }
}
