package edu.purdue.cs.fast.baselines.naive.models;

import edu.purdue.cs.fast.models.MinimalRangeQuery;
import edu.purdue.cs.fast.models.Rectangle;

public class ReinsertEntry {
    public Rectangle range;
    public MinimalRangeQuery query;

    public ReinsertEntry(Rectangle range, MinimalRangeQuery query) {
        super();
        this.range = range;
        this.query = query;
    }
}
