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
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostService {

    private static final String REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL = "https://old.reddit.com/r/%s/top/?sort=top&t=hour";
    private final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final RestTemplate restTemplate;
    private final PostRepository postRepository;
    private final PostParser postParser;

    public PostService(RestTemplate restTemplate, PostRepository postRepository, PostParser postParser) {
        this.restTemplate = restTemplate;
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
                return ResponseEntity.status(429).body("Reddit request limit reached"); // todo maybe oauth2 can increase limit 5 -> 60 per minute?
            }
            return ResponseEntity.badRequest().body("Check logs");
        }

        Document document = Jsoup.parse(pageHtmlBody);
        Elements postElements = document.getElementsByClass("link");

        List<Post> comments = postElements.stream()
                .map(this::convertElementToComment)
                .collect(Collectors.toList());

        LOG.info(String.format("Retrieved comments for user '%s'", subreddit));

        return ResponseEntity.ok(comments);
    }

    private String getSubredditPostsHtmlPage(String subreddit) {
        String url = String.format(REDDIT_SUBREDDIT_TOP_PAST_HOUR_URL, subreddit);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Cookie", "loid=00000000007xzgo7uu.2.1599044240000.Z0FBQUFBQmZnSldKbldKRGU5R0M5SkRqRHI5d1VFQzlaMW1KcTFQRmo4ZFpZVGZla1FYeXNtZ2plX2w4VUVnOFEzNlNGem9NTUlNM1ZOblgtdWdjNmU0N25LSExXQV8wVFJRb2FHYktkcGRDYzJSN2YybEx5TXZCMk02eWZqWEJITHk5QU9tVnUtOHg; eu_cookie_v2=3; edgebucket=pFaU0YQafqtq2eVRYD; recent_srs=t5_2th52%2Ct5_3q2m6%2Ct5_3pzw2%2Ct5_32fdw7%2Ct5_2s7v0%2Ct5_2qhd7%2Ct5_2t0th%2Ct5_2sgjv%2Ct5_2s1me%2Ct5_3361s; reddaid=6TQ2NNHZZLA4JUAB; csv=1; listingsignupbar_dismiss=1; token_v2=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAi…c3NUb2tlbiI6IjYyMjUyNzI4NTcwMi1za0c3Vk5JX3B2ZzA4Mi1zZkpfVWpLMTVJVnciLCJleHBpcmVzIjoiMjAyMC0xMS0xMFQxOToyNDo0Ny4wMDBaIiwibG9nZ2VkT3V0IjpmYWxzZSwic2NvcGVzIjpbIioiLCJlbWFpbCJdfQ==; RetardStockBot_recentclicks2=t3_jwk9hx%2Ct3_jwiu7j%2Ct3_jwirsb%2Ct3_jrv5xm%2Ct3_jtivs1; session_tracker=jmnqhklhbjhkpbifgr.0.1605721865093.Z0FBQUFBQmZ0VjhKYUJXLUVQekhfZU5hYk5tb2l4eld3ZTQwNFVnd1pVR2hVS29mN0VJYVk2Z0xFS09rOGNBX2VIVkpxZ1FyZTZnb0E5Qkx2QVhfMFRmMzcyWUpGX0RwQTR2UU9UeWQ4UlNaZHNoR3BEaEtFZTFiRUFBOEtUMWN5a0NZNVROUl9Nb3g; pc=tu");
        HttpEntity request = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        return response.getBody();
//        return restTemplate.getForObject(url, String.class);
    }

    private Post convertElementToComment(Element postElement) {
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
