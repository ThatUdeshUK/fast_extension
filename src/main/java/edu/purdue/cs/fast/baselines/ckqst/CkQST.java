package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.ckqst.structures.CostBasedQuadTree;
import edu.purdue.cs.fast.baselines.ckqst.structures.ObjectIndex;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

public class CkQST implements SpatialKeywordIndex<Query, DataObject> {
    public static int xRange = 10;
    public static int yRange = 10;
    public static int maxHeight = 9;

    public static double thetaU;
    private ObjectIndex objectIndex;
    private final CostBasedQuadTree queryIndex;
    private int timestamp = 0;

    public CkQST() {
        queryIndex = new CostBasedQuadTree(0, 0, xRange, yRange, maxHeight);
        thetaU = 0.5;
    }

    public CkQST(int xRange, int yRange, int maxHeight) {
        CkQST.xRange = xRange;
        CkQST.yRange = yRange;
        CkQST.maxHeight = maxHeight;
        queryIndex = new CostBasedQuadTree(0, 0, xRange, yRange, maxHeight);
        thetaU = 0.5;
    }

    @Override
    public void preloadObject(DataObject object) {
        if (objectIndex == null)
            objectIndex = new ObjectIndex();

        objectIndex.insertObject(object);
    }

    @Override
    public Collection<DataObject> insertQuery(Query query) {
        timestamp++;
        if (query.getClass() == CkQuery.class) {
            if (objectIndex != null) {
                objectIndex.insertQuery(query).forEach(object -> {
                    if (new HashSet<>(object.keywords).containsAll(query.keywords))
                        ((CkQuery) query).updateSR(object);
                });
            }
            queryIndex.insert((CkQuery) query);
        } else
            throw new RuntimeException("CkQST only support KNNQueries!");
        return null;
    }

    @Override
    public Collection<Query> insertObject(DataObject dataObject) {
        timestamp++;
//        if (dataObject.id == 1000 + 91) {
//            System.out.println("DEBUG!");
//        }
        return queryIndex.searchObject(dataObject);
    }

    public void printIndex() {
        System.out.println(queryIndex);
    }
}
