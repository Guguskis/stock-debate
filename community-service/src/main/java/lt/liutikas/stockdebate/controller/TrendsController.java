package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.service.TrendsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

@RestController
@RequestMapping("/api")
public class TrendsController {

    private final TrendsService trendsService;

    public TrendsController(TrendsService trendsService) {
        this.trendsService = trendsService;
    }

    @GetMapping("/trends")
    public ResponseEntity getForecast(@PathParam("subreddit") String subreddit) {
        return trendsService.getTrends(subreddit);
    }
}
