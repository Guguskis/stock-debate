package lt.liutikas.stockdebate.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private OpinionType opinionType;
    private LocalDateTime created;
    private String stockSymbol;
    @ManyToOne(optional = false)
    private Subreddit subreddit;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OpinionType getOpinionType() {
        return opinionType;
    }

    public void setOpinionType(OpinionType opinionType) {
        this.opinionType = opinionType;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public Subreddit getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(Subreddit subreddit) {
        this.subreddit = subreddit;
    }
}
