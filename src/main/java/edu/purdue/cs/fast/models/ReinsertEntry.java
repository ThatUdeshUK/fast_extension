package edu.purdue.cs.fast.models;

public class ReinsertEntry {
    public Rectangle range;
    public Query query;

    public ReinsertEntry(Rectangle range, Query query) {
        super();
        this.range = range;
        this.query = query;
    }
}
