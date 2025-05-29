package edu.purdue.cs.fast.baselines.ckqst;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.ckqst.structures.CostBasedQuadTree;
import edu.purdue.cs.fast.baselines.ckqst.structures.IQuadTree;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.QueryStat;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Reproduction of CkQST
 */
public class CkQST implements SpatialKeywordIndex<Query, DataObject> {
    public static int xRange = 10;
    public static int yRange = 10;
    public static int maxHeight = 9;
    public static int maxLeafCapacity = 512;

    public static long creationTime = 0;
    public static long indexingTime = 0;
    public static long searchTime = 0;
    public static long objectSearchCount = 0;
    public static long objIdxSearchTime = 0;

    public static double thetaU;
    public final IQuadTree objectIndex;
    public final CostBasedQuadTree queryIndex;
    private int timestamp = 0;

    public CkQST() {
        objectIndex = new IQuadTree(0, 0, xRange, yRange, maxLeafCapacity, maxHeight);
        queryIndex = new CostBasedQuadTree(0, 0, xRange, yRange, maxHeight);
        thetaU = 0.5;
    }

    public CkQST(int xRange, int yRange, int maxHeight) {
        CkQST.xRange = xRange;
        CkQST.yRange = yRange;
        CkQST.maxHeight = maxHeight;
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
            CkQST.objectSearchCount++;
            Stopwatch objSearchWatch = Stopwatch.createStarted();
            PriorityQueue<DataObject> objResults = (PriorityQueue<DataObject>) objectIndex.search(query);
            objSearchWatch.stop();

            if (objResults.size() >= ((CkQuery) query).k) {
                DataObject o = objResults.peek();
//                assert o != null;
                ((CkQuery) query).sr = SpatialHelper.getDistanceInBetween(((CkQuery) query).location, o.location);
            }

            Stopwatch insWatch = Stopwatch.createStarted();
            queryIndex.insert(query);
            insWatch.stop();

            objIdxSearchTime += objSearchWatch.elapsed(TimeUnit.NANOSECONDS);
            indexingTime += insWatch.elapsed(TimeUnit.NANOSECONDS);
            creationTime += objSearchWatch.elapsed(TimeUnit.NANOSECONDS) + insWatch.elapsed(TimeUnit.NANOSECONDS);

            queryStats.add(new QueryStat(query.id, objSearchWatch.elapsed(TimeUnit.NANOSECONDS),
                    insWatch.elapsed(TimeUnit.NANOSECONDS), ((CkQuery) query).sr, 0, 0, QueryStat.Stage.INSERT));
        } else if (query.getClass() == LMinimalRangeQuery.class) {
            queryIndex.insert(query);
        } else
            throw new RuntimeException("CkQST only support KNNQueries!");
        return null;
    }

    @Override
    public Collection<Query> insertObject(DataObject dataObject) {
        timestamp++;

        Stopwatch insWatch = Stopwatch.createStarted();
        objectIndex.insert(dataObject);
        insWatch.stop();

        Stopwatch sTime = Stopwatch.createStarted();
        Collection<Query> queryResults = queryIndex.search(dataObject);
        sTime.stop();

        searchTime += insWatch.elapsed(TimeUnit.NANOSECONDS) + sTime.elapsed(TimeUnit.NANOSECONDS);
//        for (Query query : queryResults) {
//            if (query instanceof CkQuery) {
//                PriorityQueue<DataObject> objResults = (PriorityQueue<DataObject>) objectIndex.search(query);
//
//                if (objResults.size() >= ((CkQuery) query).k) {
//                    DataObject o = objResults.peek();
//                    assert o != null;
//                    ((CkQuery) query).sr = SpatialHelper.getDistanceInBetween(((CkQuery) query).location, o.location);
//                }
//            }
//        }
        return queryResults;
    }

    public void printIndex() {
        System.out.println(queryIndex);
    }
}
