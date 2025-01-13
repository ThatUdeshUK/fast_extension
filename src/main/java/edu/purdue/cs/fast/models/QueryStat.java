package edu.purdue.cs.fast.models;

public class QueryStat {
    public int index;
    public long searchTime;
    public long insertTime;
    public double ar;
    public int descendCount;
    public int totalDescendCount;
    public int level;
    public Stage stage;

    public QueryStat(int index, long searchTime, long insertTime, double ar,
                     int descendCount, int level, Stage stage) {
        this.index = index;
        this.searchTime = searchTime;
        this.insertTime = insertTime;
        this.ar = ar;
        this.descendCount = descendCount;
        this.level = level;
        this.stage = stage;
        this.totalDescendCount = 0;
        // this.totalDescendCount = FAST.context.totalDescendOpts;
    }

    public String toJson() {
        return "{" +
                "\"index\": " + index + ", " +
                "\"search_time\": " + searchTime + ", " +
                "\"insert_time\": " + insertTime + ", " +
                "\"ar\": " + ar + ", " +
                "\"descendCount\": " + descendCount + ", " +
                "\"totalDescendCount\": " + totalDescendCount + ", " +
                "\"level\": " + level + ", " +
                "\"stage\": \"" + stage.name() + "\"" +
                "}";
    }

    public enum Stage {
        INSERT,
        SEARCH
    }
}
