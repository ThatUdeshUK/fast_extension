package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;

import java.util.*;

public class Block {
    String minw;
    String maxw;
    private final LinkedList<CkQuery> queries;
    private final LinkedList<String> keywords;

    public Block() {
        this.queries = new LinkedList<>();
        this.keywords = new LinkedList<>();
    }

    public Block(CkQuery query) {
        if (query.keywords.size() > 2) {
            this.minw = query.keywords.get(2);
            this.maxw = query.keywords.get(query.keywords.size() - 1);
        }

        this.keywords = new LinkedList<>();
        if (query.keywords.size() > 2) {
            this.keywords.add(query.keywords.get(2));
        }

        this.queries = new LinkedList<>();
        this.queries.add(query);
    }

    public void add(CkQuery query) {
        queries.add(query);
    }

    public void add(int i, CkQuery query) {
        queries.add(i, query);
    }

    public double probBVbr(Map<String, Double> probWV) {
        double max = 0;
        double sum = 0;
        for (String wj : keywords) {
            double prob = probWV.get(wj);
            if (prob > max)
                max = prob;

            sum += prob;
        }
        double out = max;
        if (keywords.size() > 1)
            out = max + (1.0 / (keywords.size()-1)) * (sum - max);
        return Math.min(out, 1);
    }

    public double probBVbr_c(Map<String, Double> probWV, String w3) {
        double max = probWV.get(w3);
        double sum = max;
        for (String wj : keywords) {
            double prob = probWV.get(wj);
            if (prob > max)
                max = prob;

            sum += prob;
        }
        double out = max + (1.0 / (keywords.size())) * (sum - max);
        return Math.min(out, 1);
    }

    public LinkedList<String> getKeywords() {
        return keywords;
    }
    public LinkedList<CkQuery> getQueries() {
        return queries;
    }

    public int size() {
        return queries.size();
    }

    @Override
    public String toString() {
        return "Block{" +
                ", minw='" + minw + '\'' +
                ", maxw='" + maxw + '\'' +
                ", queries=" + queries +
                ", keywords=" + keywords +
                '}';
    }
}
