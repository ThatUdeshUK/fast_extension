package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.ckqst.structures.CostBasedQuadTree;
import edu.purdue.cs.fast.baselines.ckqst.structures.IQuadTree;
import edu.purdue.cs.fast.baselines.ckqst.structures.ObjectIndex;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

public class CkQST implements SpatialKeywordIndex<Query, DataObject> {
    public static int xRange = 10;
    public static int yRange = 10;
    public static int maxHeight = 9;
    public static int maxLeafCapacity = 5;

    public static double thetaU;
    private final IQuadTree objectIndex;
    //    private final ObjectIndex objectIndex;
    private final CostBasedQuadTree queryIndex;
    private int timestamp = 0;

    public CkQST() {
//        objectIndex = new ObjectIndex();
        objectIndex = new IQuadTree(0, 0, xRange, yRange, maxLeafCapacity, maxHeight);
        queryIndex = new CostBasedQuadTree(0, 0, xRange, yRange, maxHeight);
        thetaU = 0.5;
    }

    public CkQST(int xRange, int yRange, int maxHeight) {
        CkQST.xRange = xRange;
        CkQST.yRange = yRange;
        CkQST.maxHeight = maxHeight;
//        objectIndex = new ObjectIndex();
        objectIndex = new IQuadTree(0, 0, xRange, yRange, maxLeafCapacity, maxHeight);
        queryIndex = new CostBasedQuadTree(0, 0, xRange, yRange, maxHeight);
        thetaU = 0.5;
    }

    @Override
    public void preloadObject(DataObject object) {
        objectIndex.insert(object);
    }

    @Override
    public Collection<DataObject> insertQuery(Query query) {
        timestamp++;
        if (query.getClass() == CkQuery.class) {
//            if (query.id == 179) {
//                System.out.println("Debug!");
//            }
            PriorityQueue<DataObject> objResults = (PriorityQueue<DataObject>) objectIndex.search(query);

//            if (query.id == 6474) {
//                objResults.forEach((o) -> {
//                            System.out.println("obj:" + o.id + ", loc:" + o.location + ", ar:" + ((CkQuery) query).sr + ", keys:" + query.keywords + ", dist: " + SpatialHelper.getDistanceInBetween(((CkQuery) query).location, o.location));
//                        }
//                );
//            }
            if (objResults.size() >= ((CkQuery) query).k) {
                DataObject o = objResults.peek();
                assert o != null;
                ((CkQuery) query).sr = SpatialHelper.getDistanceInBetween(((CkQuery) query).location, o.location);
            }
//            objResults.forEach(object -> {
//
//                if (new HashSet<>(object.keywords).containsAll(query.keywords))
//                    ((CkQuery) query).updateSR(object);
//                if (query.id == 414) {
//                    System.out.println("SR from loop: " + ((CkQuery) query).sr);
//                }
//            });
            queryIndex.insert(query);
        } else
            throw new RuntimeException("CkQST only support KNNQueries!");
        return null;
    }

    @Override
    public Collection<Query> insertObject(DataObject dataObject) {
        timestamp++;
//        if (dataObject.id == 10000) {
//            System.out.println("DEBUG!");
//            System.out.println("Streaming obj:" + dataObject);
//        }
        objectIndex.insert(dataObject);
        Collection<Query> queryResults = queryIndex.search(dataObject);
        for (Query query : queryResults) {
            PriorityQueue<DataObject> objResults = (PriorityQueue<DataObject>) objectIndex.search(query);

            if (objResults.size() >= ((CkQuery) query).k) {
                DataObject o = objResults.peek();
                assert o != null;
                ((CkQuery) query).sr = SpatialHelper.getDistanceInBetween(((CkQuery) query).location, o.location);
            }
        }
        return queryResults;
    }

    public void printIndex() {
        System.out.println(queryIndex);
    }
}
