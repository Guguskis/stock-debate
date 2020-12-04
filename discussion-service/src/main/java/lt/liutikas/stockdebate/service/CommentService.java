package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.CommentsParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.RedditUser;
import lt.liutikas.stockdebate.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class CommentService {

    private final Logger LOG = LoggerFactory.getLogger(CommentService.class);

    private final CommentsParser commentsParser;
    private final CommentRepository commentRepository;

    public CommentService(CommentsParser commentsParser, CommentRepository commentRepository) {
        this.commentsParser = commentsParser;
        this.commentRepository = commentRepository;
    }

    public ResponseEntity getComments(String username) {
        String userHtmlPage;
        try {
            userHtmlPage = commentRepository.getRedditUserCommentsHtmlPage(username);
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve comments for user '%s'", username), e);

            int rawStatusCode = ((HttpClientErrorException) e).getRawStatusCode();
            if (rawStatusCode == 429) {
                return ResponseEntity.status(429).body("Reddit request limit reached");
            }
            return ResponseEntity.badRequest().body("User not found or banned");
        }

        List<Comment> comments = commentsParser.parseComments(userHtmlPage);

        LOG.info(String.format("Retrieved %d comments for user '%s'", comments.size(), username));

        RedditUser redditUser = new RedditUser();
        redditUser.setUsername(username);
        redditUser.setComments(comments);

        return ResponseEntity.ok(redditUser);
    }

}
