package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.model.SubredditPosts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Repository
public class DiscussionRepository {

    private static final String GET_COMMENTS_URL = "/api/subreddit/%s/post/%s/comments";
    private static final String GET_POSTS_PAST_HOUR = "/api/subreddit/%s/posts";

    private final RestTemplate discussionTemplate;

    public DiscussionRepository(@Qualifier("discussion") RestTemplate discussionTemplate) {
        this.discussionTemplate = discussionTemplate;
    }

    public List<Comment> getComments(String subreddit, String postId) {
        String url = String.format(GET_COMMENTS_URL, subreddit, postId);
        Post post = discussionTemplate.getForObject(url, Post.class);
        return post.getComments();
    }

    public List<Post> getPosts(String subreddit) {
        String url = String.format(GET_POSTS_PAST_HOUR, subreddit);
        SubredditPosts subredditPosts = discussionTemplate.getForObject(url, SubredditPosts.class);
        return subredditPosts.getPosts();
    }
}
