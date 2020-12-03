package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.Event;
import lt.liutikas.stockdebate.model.Post;
import lt.liutikas.stockdebate.model.Subreddit;
import lt.liutikas.stockdebate.repository.DiscussionRepository;
import lt.liutikas.stockdebate.repository.EventRepository;
import lt.liutikas.stockdebate.repository.OpinionRepository;
import lt.liutikas.stockdebate.repository.SubredditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EventService {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final OpinionRepository opinionRepository;
    private final DiscussionRepository discussionRepository;
    private final SubredditRepository subredditRepository;

    public EventService(EventRepository eventRepository, OpinionRepository opinionRepository, DiscussionRepository discussionRepository, SubredditRepository subredditRepository) {
        this.eventRepository = eventRepository;
        this.opinionRepository = opinionRepository;
        this.discussionRepository = discussionRepository;
        this.subredditRepository = subredditRepository;
    }

    @Scheduled(fixedRate = 1000 * 60)
    public void runScrapePosts() {
        List<Subreddit> subreddits = subredditRepository.findAllByCollectOpinionsTrue();

        List<Event> events = subreddits.stream()
                .flatMap(subreddit -> {
                    List<Post> posts = discussionRepository.getPosts(subreddit.getName());
                    return posts.stream()
                            .map(post -> getEventIfNewPost(subreddit, post))
                            .filter(Objects::nonNull);
                }).collect(Collectors.toList());

        LOG.info(String.format("Saved %d new events", events.size()));
        eventRepository.saveAll(events);
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
