package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.QueryStat;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public interface SpatialKeywordIndex<Q extends Query, O extends DataObject> {
    default void preloadObject(O object) {};
    default void preloadQuery(Q query) {};
    Collection<O> insertQuery(Q query);
    Collection<Q> insertObject(O dataObject);
    LinkedList<QueryStat> queryStats = new LinkedList<>();
}
