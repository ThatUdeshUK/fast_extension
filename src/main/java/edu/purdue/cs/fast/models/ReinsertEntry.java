package edu.purdue.cs.fast.models;

public class ReinsertEntry {
    public Rectangle range;
    public Query query;

    public ReinsertEntry(Rectangle range, Query query) {
        super();
        this.range = range;
        this.query = query;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ReinsertEntry entry = (ReinsertEntry) obj;
        return query.equals(entry.query);
    }
}
