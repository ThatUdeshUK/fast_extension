package edu.purdue.cs.fast.config;

public class Config {
    public int TRIE_SPLIT_THRESHOLD = 2;
    public int TRIE_OVERALL_MERGE_THRESHOLD = 2;
    public int DEGRADATION_RATIO = 2;
    public int CLEANING_INTERVAL = 1000;
    public int MAX_ENTRIES_PER_CLEANING_INTERVAL = 10;
    public CleanMethod CLEAN_METHOD = CleanMethod.NO;
    public boolean PUSH_TO_LOWEST = false;
}
