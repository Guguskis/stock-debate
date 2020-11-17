package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.CommentParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.repository.CommentRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommentServiceTest {

    private static final String NOW = "2020-11-17T18:35:24.00Z";

    private CommentService commentService;
    private RestTemplate restTemplate;
    private CommentRepository commentRepository;
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);
        restTemplate = mock(RestTemplate.class);
        commentRepository = mock(CommentRepository.class);
        commentService = new CommentService(restTemplate, commentRepository, new CommentParser(clock));
    }

    @Test
    public void test() {
        Path path = Paths.get("src", "test", "resources", "comments", "retardStockBot.html");
        String retardStockBotProfileHtmlPage = getFileBody(path);

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(retardStockBotProfileHtmlPage);

        ResponseEntity response = commentService.getComments("RetardStockBot");
        List<Comment> comments = (List) response.getBody();

        assertEquals(5, comments.size());
        assertComment(comments.get(0), "2020-11-10", "Mac mini 5xc faster than top desktop pc xddddd");
        assertComment(comments.get(1), "2020-10-17", "Dumb question, but how did you calculate 70?");
    }

    private void assertComment(Comment comment, String date, String text) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String commentDateString = comment.getCreationDate().format(dateTimeFormatter);
        assertEquals(date, commentDateString);
        assertEquals(text, comment.getText());
    }

    private String getFileBody(Path filePath) {
        StringBuilder stringBuilder = new StringBuilder();

        Charset charset = StandardCharsets.UTF_8;

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath, charset)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException ex) {
            System.out.format("I/O exception: %s", ex);
        }

        return stringBuilder.toString();
    }
}