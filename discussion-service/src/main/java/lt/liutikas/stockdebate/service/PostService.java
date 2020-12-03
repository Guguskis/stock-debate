package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.PostParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.repository.RedditRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostService {

    private final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final RedditRepository redditRepository;
    private final PostParser postParser;

    public PostService(RedditRepository redditRepository, PostParser postParser) {
        this.redditRepository = redditRepository;
        this.postParser = postParser;
    }

    public ResponseEntity getPosts(String subreddit) {
        String pageHtmlBody;
        try {
            pageHtmlBody = redditRepository.getSubredditPostsHtmlPage(subreddit);
        } catch (RestClientException e) {
            LOG.error(String.format("Failed to retrieve posts for subreddit '%s'", subreddit), e);

            int rawStatusCode = ((HttpClientErrorException) e).getRawStatusCode();
            if (rawStatusCode == 429) {
                return ResponseEntity.status(429).body("Reddit request limit reached");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        Document document = Jsoup.parse(pageHtmlBody);
        Elements postElements = document.getElementsByClass("link");

        List<Post> posts = postElements.stream()
                .map(this::convertElementToPost)
                .collect(Collectors.toList());

        LOG.info(String.format("Retrieved '%s' posts for subreddit '%s'", posts.size(), subreddit));

        return ResponseEntity.ok(posts);
    }

    public ResponseEntity getCommentsForPost(String subreddit, String postId) {
        String commentsHtmlBody = redditRepository.getCommentsHtmlPageForPost(subreddit, postId);

        Document document = Jsoup.parse(commentsHtmlBody);
        Elements commentElements = document.getElementsByClass("md"); // todo parse this differently to contain creation date and maybe score

        List<Comment> comments = commentElements.stream()
                .map(this::convertToComment)
                .collect(Collectors.toList());

        Post post = new Post();
        post.setComments(comments);
        return ResponseEntity.ok(comments);
    }

    private Comment convertToComment(Element element) {
        Comment comment = new Comment();
        comment.setText(element.text());
        return comment;
    }

    private Post convertElementToPost(Element postElement) {
        String title = postElement.select("a.title").text();
        String scoreText = postElement.getElementsByClass("score unvoted").get(0).attr("title");
        String commentCountText = postElement.getElementsByClass("comments").text();
        String creationDateText = postElement.getElementsByClass("live-timestamp").get(0).attr("datetime");
        String link = postElement.getElementsByClass("comments").get(0).attr("href");

        Post post = new Post();
        post.setTitle(title);
        post.setScore(postParser.parseScore(scoreText));
        post.setCommentCount(postParser.parseCommentCount(commentCountText));
        post.setCreationDate(postParser.parseCreationDate(creationDateText));
        post.setLink(link);

        return post;
    }
}
