package edu.purdue.cs.fast.config;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.Rectangle;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;

public class Context implements Serializable {
    public final Rectangle bounds;
    public final int gridGranularity;
    public final int maxLevel;
    public final double globalXRange;
    public final double globalYRange;
    public final double localXstep;
    public final double localYstep;
    public int totalVisited = 0;
    public int timestamp = 0;
    public int minInsertedLevel;
    public int maxInsertedLevel;
    public int spatialOverlappingQueries = 0;
    public int queryInsertInvListNodeCounter = 0;
    public int queryInsertTrieNodeCounter = 0;
    public int totalQueryInsertionsIncludingReplications = 0;
    public int objectSearchInvListNodeCounter = 0;
    public int objectSearchTrieNodeCounter = 0;
    public int objectSearchInvListHashAccess = 0;
    public int objectSearchTrieHashAccess = 0;
    public int objectSearchTrieFinalNodeCounter = 0;
    public int numberOfHashEntries = 0;
    public int numberOfTrieNodes = 0;
    public int totalTrieAccess = 0;
    public int totalDescendOpts = 0;
//    public Map<String, Integer> cellInsertions = new HashMap<>();
    public static Function<KNNQuery, PriorityQueue<DataObject>> objectSearcher;

    public Context(Rectangle bounds, int gridGranularity, int maxLevel) {
        this.bounds = bounds;
        this.gridGranularity = gridGranularity;
        this.maxLevel = Math.min((int) (Math.log(gridGranularity) / Math.log(2)), maxLevel);
        this.globalXRange = bounds.max.x - bounds.min.x;
        this.globalYRange = bounds.max.y - bounds.min.y;
        this.localXstep = (this.globalXRange / this.gridGranularity);
        this.localYstep = (this.globalYRange / this.gridGranularity);
    }

}
