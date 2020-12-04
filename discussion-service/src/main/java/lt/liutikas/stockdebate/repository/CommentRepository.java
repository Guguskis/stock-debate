package lt.liutikas.stockdebate.repository;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentRepository {
    private static final String REDDIT_USER_PROFILE_URL = "https://old.reddit.com/user/%s?limit=100";

    private final RestTemplate restTemplate;

    public CommentRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getRedditUserCommentsHtmlPage(String username) {
        String url = String.format(REDDIT_USER_PROFILE_URL, username);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.USER_AGENT, "retardedStockBot 0.1"); // Reddit restricts API access for default agents
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        return response.getBody();
    }
}
