package lt.liutikas.stockdebate.helper;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommentParser {

    public LocalDate parseCreationDate(String creationDateString) {
        LocalDateTime creationDateTime = LocalDateTime.parse(creationDateString, DateTimeFormatter.ISO_DATE_TIME);
        return LocalDate.from(creationDateTime);
    }

    public Integer parseScore(String text) {
        Pattern pattern = Pattern.compile("(-?\\d+) point");
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        String scoreString = matcher.group(1);
        int score;
        try {
            score = Integer.parseInt(scoreString);
        } catch (NumberFormatException e) {
            return null;
        }

        return score;
    }
}
