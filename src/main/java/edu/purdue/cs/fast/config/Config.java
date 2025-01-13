package edu.purdue.cs.fast.config;

import java.io.Serializable;

public class Config implements Serializable {
    public int TRIE_SPLIT_THRESHOLD = 2;
    public int TRIE_OVERALL_MERGE_THRESHOLD = 2;
    public int DEGRADATION_RATIO = 4 * this.TRIE_SPLIT_THRESHOLD;
    public int KNN_DEGRADATION_RATIO = 50;
    public double KNN_DEGRADATION_AR = 25.0;
    public boolean INPLACE_OBJECT_INDEX = false;
    public int CLEANING_INTERVAL = 10000;
    public int MAX_ENTRIES_PER_CLEANING_INTERVAL = 10;
    public CleanMethod CLEAN_METHOD = CleanMethod.NO;
    public boolean INCREMENTAL_DESCENT = false;
    public boolean LAZY_OBJ_SEARCH = false;
    public boolean OBJ_FAST_SEARCH = false;
    public boolean ADAPTIVE_DEG_RATIO = false;
    public boolean PUSH_TO_LOWEST = false;
//    public int RHO = 2;
}
