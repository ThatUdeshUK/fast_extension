package edu.purdue.cs.fast.config;

public class Config {
    public int TRIE_SPLIT_THRESHOLD = 2;
    public int TRIE_OVERALL_MERGE_THRESHOLD = 2;
    public int DEGRADATION_RATIO = 2;
    public int KNN_DEGRADATION_RATIO = 50;
    public double KNN_DEGRADATION_AR = 25.0;
    public int CLEANING_INTERVAL = 1000;
    public int MAX_ENTRIES_PER_CLEANING_INTERVAL = 10;
    public CleanMethod CLEAN_METHOD = CleanMethod.NO;
    public boolean INCREMENTAL_DESCENT = false;
    public boolean PUSH_TO_LOWEST = false;
//    public int RHO = 2;
}
