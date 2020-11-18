package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.PostParser;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.repository.PostRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostServiceTest {

    private PostService postService;
    private RestTemplate restTemplate;
    private PostRepository postRepository;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        postRepository = mock(PostRepository.class);
        OAuth2RestOperations oAuth2RestOperations = mock(OAuth2RestOperations.class);
        postService = new PostService(restTemplate, oAuth2RestOperations, postRepository, new PostParser());
    }

    @Test
    public void getPosts_providedExistingSubreddit_returnsAllPosts() {
        Path path = Paths.get("src", "test", "resources", "posts", "wallstreetbets.html");
        String wallstreetbetsPostsHtmlPage = getFileBody(path);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(wallstreetbetsPostsHtmlPage, HttpStatus.OK));

        ResponseEntity response = postService.getPosts("RetardStockBot");
        List<Post> posts = (List) response.getBody();

        assertEquals(25, posts.size());
        assertPost(posts.get(0),
                "Woke up a millionaire thanks to CRSR. Nice", 12354, 37,
                LocalDateTime.of(2020, 11, 18, 17, 36, 50),
                "https://old.reddit.com/r/wallstreetbets/comments/jwk8bh/woke_up_a_millionaire_thanks_to_crsr_nice/");
        assertPost(posts.get(1),
                "[Prediction Market] What will Airbnb's market cap be at close on its 1st day publicly trading?", null, 2,
                LocalDateTime.of(2020, 11, 18, 17, 49, 25),
                "https://old.reddit.com/r/wallstreetbets/comments/jwkgx1/prediction_market_what_will_airbnbs_market_cap_be/");
    }

    @Test
    public void getPosts_providedNotExistingSubreddit_returnsBadRequest() {

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity response = postService.getPosts("RetardStockBot");

        assertEquals(400, response.getStatusCodeValue());
    }


    private void assertPost(Post post, String title, Integer score, Integer commentCount, LocalDateTime creationDate, String link) {
        assertEquals(title, post.getTitle());
        assertEquals(score, post.getScore());
        assertEquals(commentCount, post.getCommentCount());
        assertEquals(creationDate, post.getCreationDate());
        assertEquals(link, post.getLink());
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