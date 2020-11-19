package lt.liutikas.stockdebate.helper;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommentParser {

    private final Clock clock;

    public CommentParser(Clock clock) {
        this.clock = clock;
    }

    public LocalDate parseCreationDate(String creationDateString) {
        LocalDateTime creationDateTime = LocalDateTime.parse(creationDateString, DateTimeFormatter.ISO_DATE_TIME);
        return LocalDate.from(creationDateTime);
    }

    public int parseScore(String text) {
        Pattern pattern = Pattern.compile("(-?\\d+) point");
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("String does not contain score '%s'", text));
        }

        String scoreString = matcher.group(1);
        int score;
        try {
            score = Integer.parseInt(scoreString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Unable to parse score from number '%s'", scoreString));
        }

        return score;
    }
}
