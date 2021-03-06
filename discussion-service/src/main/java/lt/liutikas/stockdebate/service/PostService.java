package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.CommentsParser;
import lt.liutikas.stockdebate.helper.PostParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.model.PostComments;
import lt.liutikas.stockdebate.model.SubredditPosts;
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

    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final RedditRepository redditRepository;
    private final PostParser postParser;
    private final CommentsParser commentsParser;

    public PostService(RedditRepository redditRepository, PostParser postParser, CommentsParser commentsParser) {
        this.redditRepository = redditRepository;
        this.postParser = postParser;
        this.commentsParser = commentsParser;
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

        SubredditPosts subredditPosts = new SubredditPosts();
        subredditPosts.setPosts(posts);
        return ResponseEntity.ok(subredditPosts);
    }

    public ResponseEntity getCommentsForPost(String subreddit, String postId) {
        String hotPostHtmlPage = redditRepository.getGetPostHtmlPage(subreddit, postId);
        List<Comment> comments = commentsParser.parseComments(hotPostHtmlPage);

        PostComments postComments = new PostComments();
        postComments.setComments(comments);

        return ResponseEntity.ok(postComments);
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
