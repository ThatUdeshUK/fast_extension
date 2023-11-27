package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.baselines.ckqst.structures.OrderedInvertedIndex;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;

import java.util.List;

public class CkQST {
    private final OrderedInvertedIndex index;

    public CkQST() {
        index = new OrderedInvertedIndex(0);
    }


    public void addContinuousQuery(KNNQuery query) {
        index.insertQueryPL(query);
    }

    public List<KNNQuery> searchQueries(DataObject dataObject) {
        throw new RuntimeException("Not implemented!");
    }

    public void printIndex() {
        System.out.println(index);
    }
}
