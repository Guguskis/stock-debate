package lt.liutikas.stockdebate.helper;

import lt.liutikas.stockdebate.model.TimePeriod;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommentParser {

    private final Clock clock;

    public CommentParser(Clock clock) {
        this.clock = clock;
    }

    public LocalDate parseCreationDate(String text) {
        Pattern pattern = Pattern.compile("· (\\d+) (.+) ago");
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("String does not contain number or data '%s'", text));
        }

        String numberString = matcher.group(1);
        String timePeriodString = matcher.group(2);
        int number;
        TimePeriod timePeriod = null;

        try {
            number = Integer.parseInt(numberString);
            for (TimePeriod period : TimePeriod.values()) {
                if (timePeriodString.contains(period.toString().toLowerCase())) {
                    timePeriod = period;
                    break;
                }
            }
            if (timePeriod == null) {
                throw new Exception();
            }

        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("Failed to parse number '%s' or time period '%s'", numberString, timePeriodString));
        }

        return getCreationDate(number, timePeriod);
    }

    private LocalDate getCreationDate(int number, TimePeriod timePeriod) {
        LocalDate now = LocalDate.now(clock);
        LocalDate creationDate = null;

        switch (timePeriod) {
            case MINUTE:
                creationDate = now.minus(number, ChronoUnit.MINUTES);
                break;
            case DAY:
                creationDate = now.minus(number, ChronoUnit.DAYS);
                break;
            case WEEK:
                creationDate = now.minus(number, ChronoUnit.WEEKS);
                break;
            case MONTH:
                creationDate = now.minus(number, ChronoUnit.MONTHS);
                break;
            case YEAR:
                creationDate = now.minus(number, ChronoUnit.YEARS);
                break;
        }
        return creationDate;
    }

    public String parseCommentText(String text) {
        Pattern pattern = Pattern.compile("ago (.+)");
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("String does not contain comment '%s'", text));
        }

        String commentText = matcher.group(1);
        return commentText;
    }
}
