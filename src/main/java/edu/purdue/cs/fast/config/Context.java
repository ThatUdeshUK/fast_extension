package edu.purdue.cs.fast.config;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.models.Rectangle;

public class Context {
    public final Rectangle bounds;
    public final int gridGranularity;
    public final int maxLevel;
    public final double globalXRange;
    public final double globalYRange;
    public final double localXstep;
    public final double localYstep;
    public int totalVisited = 0;
    public int timestamp = 0;
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
