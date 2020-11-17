package lt.liutikas.stockdebate.model;

import java.time.LocalDate;

public class Comment {

    private LocalDate creationDate;
    private String text;

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
