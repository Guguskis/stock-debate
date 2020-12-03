package lt.liutikas.stockdebate.repository;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RedditRepository {


    private static final String REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL = "https://old.reddit.com/r/%s/top/?sort=top&t=hour&limit=100";
    private static final String GET_POST_COMMENTS_URL = "https://old.reddit.com/r/%s/comments/%s/";

    private final RestTemplate restTemplate;

    public RedditRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getSubredditPostsHtmlPage(String subreddit) {
        String url = String.format(REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL, subreddit);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityWithHeaders(), String.class);
        return response.getBody();
    }

    private HttpEntity<Object> getHttpEntityWithHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.USER_AGENT, "retardedStockBot 0.1"); // Reddit restricts API access for default agents
        return new HttpEntity<>(httpHeaders);
    }

    public String getCommentsHtmlPageForPost(String subreddit, String postId) {
        String url = String.format(GET_POST_COMMENTS_URL, subreddit, postId);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntityWithHeaders(), String.class);
        return response.getBody();
    }
}
