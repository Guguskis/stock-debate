package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.PostParser;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.repository.PostRepository;
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
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostService {

    private static final String REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL = "https://old.reddit.com/r/%s/top/?sort=top&t=hour&limit=100";
    private final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final RestTemplate restTemplate;
    private final OAuth2RestOperations oauthRestTemplate;
    private final PostRepository postRepository;
    private final PostParser postParser;

    public PostService(RestTemplate restTemplate, OAuth2RestOperations oauthRestTemplate, PostRepository postRepository, PostParser postParser) {
        this.restTemplate = restTemplate;
        this.oauthRestTemplate = oauthRestTemplate;
        this.postRepository = postRepository;
        this.postParser = postParser;
    }

    public ResponseEntity getPosts(String subreddit) {
        String pageHtmlBody;
        try {
            pageHtmlBody = getSubredditPostsHtmlPage(subreddit);
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

    private String getSubredditPostsHtmlPage(String subreddit) {
        String url = String.format(REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL, subreddit);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.USER_AGENT, "retardedStockBot 0.1"); // Reddit restricts API access for default agents
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return response.getBody();
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
