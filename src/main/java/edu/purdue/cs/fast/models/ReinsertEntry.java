package edu.purdue.cs.fast.models;

public class ReinsertEntry {
    public Rectangle range;
    public Query query;

    // Attributes to support KNN reinsert
//    public int level = -1;
//    public double levelStep = -1;
//    public int levelGranularity = -1;

    public ReinsertEntry(Rectangle range, Query query) {
        super();
        this.range = range;
        this.query = query;
    }

//    public ReinsertEntry(Rectangle range, Query query, int level, double levelStep, int levelGranularity) {
//        super();
//        this.range = range;
//        this.query = query;
////        this.level = level;
////        this.levelStep = levelStep;
////        this.levelGranularity = levelGranularity;
//    }
}
