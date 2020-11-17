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
            pageHtmlBody = restTemplate.getForObject(String.format(REDDIT_USER_PROFILE_URL, username), String.class);
        } catch (RestClientException e) {
            int rawStatusCode = ((HttpClientErrorException) e).getRawStatusCode();
            if (rawStatusCode == 429) {
                return ResponseEntity.status(429).body("Reddit request limit reached"); // todo maybe oauth2 can increase limit 5 -> 60 per minute?
            }
            LOG.error(String.format("Failed to retrieve comments for user '%s'", username), e);
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

    private Comment convertElementToComment(Element commentElement) {
        Elements createdDateElement = commentElement.getElementsByTag("time");
        String commentText = commentElement.getElementsByClass("usertext-body").text();

        Comment comment = new Comment();
        comment.setCreationDate(commentParser.parseCreationDate(createdDateElement.text()));
        comment.setText(commentText);
        return comment;
    }
}
