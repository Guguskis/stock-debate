package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/subreddit/{subreddit}/posts")
    public ResponseEntity getPosts(@PathVariable String subreddit) {
        return postService.getPosts(subreddit);
    }

    @GetMapping("/subreddit/{subreddit}/post/{postId}/comments")
    public ResponseEntity getComments(@PathVariable String subreddit, @PathVariable String postId) {
        return postService.getCommentsForPost(subreddit, postId);
    }
}
