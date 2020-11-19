package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.CommentParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.repository.CommentRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentService {

    private static final String REDDIT_USER_PROFILE_URL = "https://old.reddit.com/user/%s";
    private final Logger LOG = LoggerFactory.getLogger(CommentService.class);

    private final RestTemplate restTemplate;
    private final CommentRepository commentRepository;
    private final CommentParser commentParser;

    public CommentService(RestTemplate restTemplate, CommentRepository commentRepository, CommentParser commentParser) {
        this.restTemplate = restTemplate;
        this.commentRepository = commentRepository;
        this.commentParser = commentParser;
    }

    public ResponseEntity getComments(String username) {
        String pageHtmlBody;
        try {
            pageHtmlBody = getRedditUserCommentsHtmlPage(username);
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve comments for user '%s'", username), e);

            int rawStatusCode = ((HttpClientErrorException) e).getRawStatusCode();
            if (rawStatusCode == 429) {
                return ResponseEntity.status(429).body("Reddit request limit reached"); // todo maybe oauth2 can increase limit 5 -> 60 per minute?
            }
            return ResponseEntity.badRequest().body("User not found or banned");
        }

        Document document = Jsoup.parse(pageHtmlBody);
        Elements commentElements = document.getElementsByClass("comment");

        List<Comment> comments = commentElements.stream()
                .map(this::convertElementToComment)
                .collect(Collectors.toList());

        LOG.info(String.format("Retrieved comments for user '%s'", username));

        return ResponseEntity.ok(comments);
    }

    private String getRedditUserCommentsHtmlPage(String username) {
        String url = String.format(REDDIT_USER_PROFILE_URL, username);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.USER_AGENT, "retardedStockBot 0.1"); // Reddit restricts API access for default agents
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return response.getBody();
    }

    private Comment convertElementToComment(Element commentElement) {
        Elements createdDateElement = commentElement.getElementsByTag("time");
        String commentText = commentElement.getElementsByClass("usertext-body").text();
        String scoreText = commentElement.getElementsByClass("score unvoted").text();

        Comment comment = new Comment();
        comment.setCreationDate(commentParser.parseCreationDate(createdDateElement.text()));
        comment.setText(commentText);
        comment.setScore(commentParser.parseScore(scoreText));
        return comment;
    }
}
