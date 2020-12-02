package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.service.SubredditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SubredditController {

    private final SubredditService subredditService;

    @Autowired
    public SubredditController(SubredditService subredditService) {
        this.subredditService = subredditService;
    }

//    @GetMapping("/subreddit/{subreddit}/opinions")
//    public ResponseEntity getOpinions(@PathVariable String subreddit,
//                                      @RequestParam("dateRange") DateRange dateRange) {
//        return opinionService.getOpinions(subreddit, dateRange);
//    }

    @GetMapping("/subreddits")
    public ResponseEntity getSubreddits() {
        return subredditService.getSubreddits();
    }

    @PostMapping("/subreddit/{name}/collectOpinions")
    public ResponseEntity registerOpinionsCollection(@PathVariable String name) {
        return subredditService.registerOpinionsCollection(name);
    }

    @DeleteMapping("/subreddit/{name}/collectOpinions")
    public ResponseEntity unregisterOpinionsCollection(@PathVariable String name) {
        return subredditService.unregisterOpinionsCollection(name);
    }

}
