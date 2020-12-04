package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import lt.liutikas.stockdebate.model.opinion.OpinionType;
import lt.liutikas.stockdebate.repository.StockRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpinionParserTest {

    private OpinionParser opinionParser;
    private StockRepository stockRepository;

    @Before
    public void setUp() {
        stockRepository = mock(StockRepository.class);
        opinionParser = new OpinionParser(stockRepository);
    }

    @Test
    public void parse_commentNoStockSymbols_returnsNoOpinions() {
        Comment comment = createComment("This is not opinion.");

        List<Opinion> opinions = opinionParser.parseComment(comment);

        assertEquals(0, opinions.size());
    }

    @Test
    public void parse_commentContainsPositiveOpinionForOneStock_returnsOneOpinion() {
        Comment comment = createComment("I think that TSLA is going to moon");

        when(stockRepository.isStock("TSLA"))
                .thenReturn(true);

        List<Opinion> opinions = opinionParser.parseComment(comment);

        assertEquals(1, opinions.size());

        assertOpinion("TSLA", OpinionType.BUY, opinions.get(0));
    }

    private void assertOpinion(String stockSymbol, OpinionType opinionType, Opinion opinion) {
        assertEquals(stockSymbol, opinion.getStockSymbol());
        assertEquals(opinionType, opinion.getOpinionType());
    }

    private Comment createComment(String text) {
        Comment comment = new Comment();
        comment.setText(text);
        return comment;
    }
}