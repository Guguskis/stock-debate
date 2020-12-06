package lt.liutikas.stockdebate.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public enum DateRange {
    DAY(1, ChronoUnit.DAYS),
    TWO_DAYS(2, ChronoUnit.DAYS),
    FIVE_DAYS(5, ChronoUnit.DAYS),
    TWO_WEEKS(2, ChronoUnit.WEEKS),
    MONTH(1, ChronoUnit.MONTHS),
    THREE_MONTHS(3, ChronoUnit.MONTHS),
    YEAR(1, ChronoUnit.YEARS);

    private long amount;
    private ChronoUnit unit;
    private long timeStepInSeconds;

    DateRange(long amount, ChronoUnit unit) {
        this.amount = amount;
        this.unit = unit;
        this.timeStepInSeconds = unit.getDuration().getSeconds() * amount;
    }

    public LocalDateTime getStartDate(LocalDateTime now) {
        return now.minus(amount, unit);
    }

    public long getTimeStepInSeconds(long stepsCount) {
        return timeStepInSeconds / stepsCount;
    }
}
