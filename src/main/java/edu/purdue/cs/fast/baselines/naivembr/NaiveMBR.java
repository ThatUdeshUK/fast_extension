package edu.purdue.cs.fast.baselines.naivembr;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.fast.helper.LSpatialHelper;
import edu.purdue.cs.fast.baselines.fast.messages.LDataObject;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.helper.TextHelpers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class NaiveMBR implements SpatialKeywordIndex<LMinimalRangeQuery, LDataObject> {
    List<LMinimalRangeQuery> queries = new LinkedList<>();

    @Override
    public Collection<LDataObject> insertQuery(LMinimalRangeQuery query) {
        queries.add(query);
        return null;
    }

    @Override
    public Collection<LMinimalRangeQuery> insertObject(LDataObject dataObject) {
//        if (dataObject.id == 2) {
//            System.out.println("Naive Obj: " + dataObject);
//        }
        List<LMinimalRangeQuery> results = new LinkedList<>();
        for (LMinimalRangeQuery query : queries) {
            if (LSpatialHelper.overlapsSpatially(dataObject.location, query.spatialBox()) &&
                    TextHelpers.containsTextually(dataObject.keywords, query.keywords)) {
//                if (dataObject.id == 2) {
//                    System.out.println("Naive: " + query);
//                }
                results.add(query);
            }
        }
        return results;
    }
}
