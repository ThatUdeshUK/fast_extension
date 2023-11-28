package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.ckqst.structures.OrderedInvertedIndex;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.Query;

import java.util.List;

public class CkQST implements SpatialKeywordIndex {
    private final OrderedInvertedIndex index;
    private int timestamp = 0;

    public CkQST() {
        index = new OrderedInvertedIndex(0);
    }


    public void addContinuousQuery(KNNQuery query) {

    }

    @Override
    public void addContinuousQuery(Query query) {
        timestamp++;
        if (query.getClass() == KNNQuery.class)
            index.insertQueryPL((KNNQuery) query);
        else
            throw new RuntimeException("CkQST only support KNNQueries!");
    }

    @Override
    public List<Query> searchQueries(DataObject dataObject) {
        timestamp++;
        throw new RuntimeException("Not implemented!");
    }

    public void printIndex() {
        System.out.println(index);
    }
}
