package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.CommentsParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.RedditUser;
import lt.liutikas.stockdebate.repository.CommentRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommentServiceTest {

    private static final String NOW = "2020-11-17T18:35:24.00Z";

    private CommentService commentService;
    private CommentRepository commentRepository;

    @Before
    public void setUp() {
        commentRepository = mock(CommentRepository.class);
        commentService = new CommentService(new CommentsParser(), commentRepository);
    }

    @Test
    public void getComments_providedExistingUsername_returnsAllComments() {
        Path path = Paths.get("src", "test", "resources", "comments", "retardStockBot.html");
        String retardStockBotProfileHtmlPage = getFileBody(path);
        String username = "RetardStockBot";

        when(commentRepository.getRedditUserCommentsHtmlPage(username))
                .thenReturn(retardStockBotProfileHtmlPage);

        ResponseEntity response = commentService.getComments(username);
        RedditUser redditUser = (RedditUser) response.getBody();
        List<Comment> comments = redditUser.getComments();

        assertEquals(3, comments.size());
        assertComment(comments.get(0), "2020-11-10", "Mac mini 5xc faster than top desktop pc xddddd");
        assertComment(comments.get(1), "2020-10-17", "Dumb question, but how did you calculate 70?");
        assertComment(comments.get(2), "2020-10-10", "Nah, no place in particular. I'm just curious if there's a way to cheat a little and have all these problems solved by website/app");
    }

    @Test
    public void getComments_providedNotExistingUsername_returnsBadRequest() {
        when(commentRepository.getRedditUserCommentsHtmlPage(anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity response = commentService.getComments("RetardStockBot");

        assertEquals(400, response.getStatusCodeValue());
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