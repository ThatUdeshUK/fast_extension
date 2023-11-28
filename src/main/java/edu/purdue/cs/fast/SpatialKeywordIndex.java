package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.util.List;

public interface SpatialKeywordIndex {
    void addContinuousQuery(Query query);
    List<Query> searchQueries(DataObject dataObject);
}
