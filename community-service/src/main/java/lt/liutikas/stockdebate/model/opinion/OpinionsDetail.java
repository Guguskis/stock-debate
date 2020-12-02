package lt.liutikas.stockdebate.model.opinion;

import java.time.LocalDateTime;
import java.util.List;

public class OpinionsDetail {

    private LocalDateTime date;
    private List<AggregatedOpinion> aggregatedOpinions;

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<AggregatedOpinion> getAggregatedOpinions() {
        return aggregatedOpinions;
    }

    public void setAggregatedOpinions(List<AggregatedOpinion> aggregatedOpinions) {
        this.aggregatedOpinions = aggregatedOpinions;
    }
}
