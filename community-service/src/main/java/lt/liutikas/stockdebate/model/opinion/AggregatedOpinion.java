package lt.liutikas.stockdebate.model.opinion;

public class AggregatedOpinion {

    private OpinionType type;
    private int count;

    public OpinionType getType() {
        return type;
    }

    public void setType(OpinionType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
