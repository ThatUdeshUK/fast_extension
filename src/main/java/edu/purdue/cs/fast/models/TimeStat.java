package edu.purdue.cs.fast.models;

public class TimeStat {
    public int index;
    public long objSearchTime;
    public long insertTime;
    public double ar;

    public TimeStat(int index, long objSearchTime, long insertTime, double ar) {
        this.index = index;
        this.objSearchTime = objSearchTime;
        this.insertTime = insertTime;
        this.ar = ar;
    }
}
