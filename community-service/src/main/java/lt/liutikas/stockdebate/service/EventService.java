package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.helper.OpinionParser;
import lt.liutikas.stockdebate.model.Comment;
import lt.liutikas.stockdebate.model.Event;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.model.opinion.Opinion;
import lt.liutikas.stockdebate.repository.DiscussionRepository;
import lt.liutikas.stockdebate.repository.EventRepository;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EventService {

    private static final int MINUTES_PER_POSTS_SCRAPE_RUN = 5;
    private static final int POST_AGE_THRESHOLD_IN_MINUTES = 50;

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final OpinionRepository opinionRepository;
    private final DiscussionRepository discussionRepository;
    private final SubredditRepository subredditRepository;
    private final OpinionParser opinionParser;
    private final Clock clock;

    public EventService(EventRepository eventRepository, OpinionRepository opinionRepository, DiscussionRepository discussionRepository, SubredditRepository subredditRepository, OpinionParser opinionParser, Clock clock) {
        this.eventRepository = eventRepository;
        this.opinionRepository = opinionRepository;
        this.discussionRepository = discussionRepository;
        this.subredditRepository = subredditRepository;
        this.opinionParser = opinionParser;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 1000 * 60 * MINUTES_PER_POSTS_SCRAPE_RUN)
    public void runScrapePosts() {
        List<Subreddit> subreddits = subredditRepository.findAllByCollectOpinionsTrue();

        List<Event> events = subreddits.stream()
                .map(this::getNewEvents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        LOG.info(String.format("Saved %d new events", events.size()));
        eventRepository.saveAll(events);
    }

    private List<Event> getNewEvents(Subreddit subreddit) {
        List<Post> posts = discussionRepository.getPosts(subreddit.getName());
        return posts.stream()
                .filter(this::ageThresholdReached)
                .map(post -> getEventIfNewPost(subreddit, post))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean ageThresholdReached(Post post) {
        LocalDateTime thresholdDateTime = LocalDateTime.now(clock).minusMinutes(POST_AGE_THRESHOLD_IN_MINUTES);
        LocalDateTime creationDateTime = post.getCreationDate();
        return creationDateTime.isBefore(thresholdDateTime);
    }

    @Scheduled(fixedRate = 1250)
    public void runAnalyzePost() {
        Event event = eventRepository.findTopByAnalyzedFalse();

        if (event == null) {
            return;
        }

        Subreddit subreddit = subredditRepository.findByNameIgnoreCase(event.getSubredditName());
        List<Comment> comments = discussionRepository.getComments(event.getSubredditName(), event.getPostId());

        List<Opinion> opinions = comments.stream()
                .map(opinionParser::parseComment)
                .flatMap(Collection::stream)
                .peek(opinion -> opinion.setSubreddit(subreddit))
                .collect(Collectors.toList());

        LOG.info(String.format("Analyzed r/%s post '%s'. Found %d comments, extracted %d opinions",
                subreddit.getName(), event.getPostId(), comments.size(), opinions.size()));

        event.setAnalyzed(true);
        eventRepository.save(event);
        opinionRepository.saveAll(opinions);
    }

    private Event getEventIfNewPost(Subreddit subreddit, Post post) {
        String link = post.getLink();
        String postId = parsePostId(link);

        if (postId == null) {
            LOG.warn(String.format("Unable to parse postId for link '%s'", link));
            return null;
        }

        String subredditName = subreddit.getName();
        Event existingEvent = eventRepository.findBySubredditNameAndPostId(subredditName, postId);

        if (existingEvent == null) {
            Event event = new Event();
            event.setAnalyzed(false);
            event.setSubredditName(subredditName);
            event.setPostId(postId);
            return event;
        }

        return null;
    }

    private String parsePostId(String link) {
        Pattern pattern = Pattern.compile("/comments/(.+)/.+");
        Matcher matcher = pattern.matcher(link);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
