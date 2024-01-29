package edu.purdue.cs.fast.baselines.fast.messages;

import java.util.List;

import edu.purdue.cs.fast.baselines.fast.helper.LTextualPredicate;
import edu.purdue.cs.fast.helper.TextualPredicate;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.Rectangle;

public class LMinimalRangeQuery extends Query {
    public final Point location;
    public boolean added;
    public boolean deleted;
    public int k = 5;

    private Rectangle spatialRange;
    public LTextualPredicate textualPredicate;

    public LMinimalRangeQuery(int id, List<String> keywords, Point location, Rectangle spatialRange, TextualPredicate predicate, long st, long et) {
        super(id, keywords, predicate, st, et);
        this.spatialRange = spatialRange;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Rectangle getSpatialRange() {
        return spatialRange;
    }

    public void setSpatialRange(Rectangle spatialRange) {
        this.spatialRange = spatialRange;
    }

    public void update(double sr) {
        spatialRange.max.x = location.x + sr;
        spatialRange.min.x = location.x - sr;
        spatialRange.max.y = location.y + sr;
        spatialRange.min.y = location.y - sr;
    }

    public LTextualPredicate getTextualPredicate() {
        return textualPredicate;
    }

    public void setTextualPredicate(LTextualPredicate textualPredicate) {
        this.textualPredicate = textualPredicate;
    }

    public String toString() {
        String output = "Query[: " + getId() + " , " + "Text: "
                + (getKeywords() == null ? "" : getKeywords().toString()) + " Location:" + spatialRange.toString() + "]";
        return output;
    }

    @Override
    public Rectangle spatialBox() {
        return spatialRange;
    }
}
