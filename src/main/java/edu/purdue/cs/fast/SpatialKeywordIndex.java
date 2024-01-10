package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.util.Collection;

public interface SpatialKeywordIndex {
    default void preloadObject(DataObject object) {};
    default void preloadQuery(Query query) {};
    void addContinuousQuery(Query query);
    Collection<Query> searchQueries(DataObject dataObject);
}
