package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.RedditUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository
public class DiscussionRepository {

    private static final String GET_COMMENTS_URL = "/api/reddituser/%s/comments";

    private final RestTemplate discussionTemplate;

    public DiscussionRepository(@Qualifier("discussion") RestTemplate discussionTemplate) {
        this.discussionTemplate = discussionTemplate;
    }

    public List<Comment> getComments(String username) {
        String url = String.format(GET_COMMENTS_URL, username);
        RedditUser redditUser = discussionTemplate.getForObject(url, RedditUser.class);
        return redditUser.getComments();
    }
}
