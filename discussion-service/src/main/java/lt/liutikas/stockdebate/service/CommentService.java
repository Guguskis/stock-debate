package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.CommentParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.repository.CommentRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentService {

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
        String pageHtmlBody = restTemplate.getForObject(String.format("https://www.reddit.com/user/%s", username), String.class);
        Document document = Jsoup.parse(pageHtmlBody);

        Elements elements = document.getElementsByClass("comment");

        List<Comment> comments = elements.stream().map(element -> {
            Comment comment = new Comment();
            comment.setCreationDate(commentParser.parseCreationDate(element.text()));
            comment.setText(commentParser.parseCommentText(element.text()));
            return comment;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }
}
