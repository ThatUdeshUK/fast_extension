package edu.purdue.cs.fast.baselines.naive.structures;

import edu.purdue.cs.fast.baselines.naive.models.NaiveKNNQuery;
import edu.purdue.cs.fast.models.MinimalRangeQuery;
import edu.purdue.cs.fast.models.Query;

import java.util.ArrayList;
import java.util.List;

public class HybridList {
    private final ArrayList<MinimalRangeQuery> mbrQueries;
    private final ArrayList<NaiveKNNQuery> kNNQueries;

    // TODO - Remove eager init
    public HybridList() {
        this.mbrQueries = new ArrayList<>();
        this.kNNQueries = new ArrayList<>();
    }

    public List<MinimalRangeQuery> mbrQueries() {
        return this.mbrQueries;
    }

    public List<NaiveKNNQuery> kNNQueries() {
        return this.kNNQueries;
    }

    public List<Query> allQueries() {
        ArrayList<Query> all = new ArrayList<>(this.mbrQueries);
        all.addAll(this.kNNQueries);
        return all;
    }

    public void add(Query query) {
        if (query instanceof MinimalRangeQuery) {
            this.mbrQueries.add((MinimalRangeQuery) query);
        } else if (query instanceof NaiveKNNQuery) {
            this.kNNQueries.add((NaiveKNNQuery) query);
        }
    }

    public void addAll(List<Query> queries) {
        for (Query q: queries) {
            add(q);
        }
    }

    public void remove(Query query) {
        if (query instanceof MinimalRangeQuery) {
            this.mbrQueries.remove((MinimalRangeQuery) query);
        } else if (query instanceof NaiveKNNQuery) {
            this.kNNQueries.remove((NaiveKNNQuery) query);
        }
    }

    public boolean contains(Query query) {
        if (query instanceof MinimalRangeQuery) {
            return this.mbrQueries.contains((MinimalRangeQuery) query);
        } else if (query instanceof NaiveKNNQuery) {
            return this.kNNQueries.contains((NaiveKNNQuery) query);
        }
        return false;
    }

    public boolean isEmpty() {
        return this.kNNQueries.isEmpty() && this.mbrQueries.isEmpty();
    }

    public int size() {
        return this.mbrQueries.size() + this.kNNQueries.size();
    }
}
