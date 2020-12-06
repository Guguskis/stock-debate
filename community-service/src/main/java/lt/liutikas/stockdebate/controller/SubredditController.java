package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.model.DateRange;
import lt.liutikas.stockdebate.service.OpinionService;
import lt.liutikas.stockdebate.service.SubredditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SubredditController {

    private final SubredditService subredditService;
    private final OpinionService opinionService;

    @Autowired
    public SubredditController(SubredditService subredditService, OpinionService opinionService) {
        this.subredditService = subredditService;
        this.opinionService = opinionService;
    }

    @GetMapping("/subreddit/opinions")
    public ResponseEntity getOpinions(@RequestParam String stockSymbol,
                                      @RequestParam DateRange dateRange,
                                      @RequestParam String subreddit
    ) {
        return opinionService.getOpinions(subreddit, stockSymbol, dateRange);
    }

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
