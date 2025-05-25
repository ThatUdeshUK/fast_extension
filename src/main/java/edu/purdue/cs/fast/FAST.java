/**
 * Copyright Jul 5, 2015
 * Author : Ahmed Mahmood
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * <p>
 * This version is meant for the fair comparison of with the state of the art
 * index that does not have any other cluster and tornado related attributes
 */
package edu.purdue.cs.fast;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.io.Serializable;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.baselines.ckqst.structures.IQuadTree;
import edu.purdue.cs.fast.baselines.ckqst.structures.ObjectIndex;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.config.Context;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.structures.*;


public class FAST implements SpatialKeywordIndex<Query, DataObject>, Serializable {
    public static Context context;
    public static Config config;
    public static HashMap<String, KeywordFrequency> keywordFrequencyMap;
    private final boolean lastCellCleaningDone; //to check if an entireCellHasBeenCleaned
    public IQuadTree objIndex;
//    public ObjectIndex objIndex;
    public ConcurrentHashMap<Integer, SpatialCell> index;
    public Iterator<Entry<Integer, SpatialCell>> cleaningIterator;//iterates over cells to clean expired entries
    //    public PriorityQueue<DataObject> expiringObjects;
    private SpatialCell cellBeingCleaned;
    public long cleanTime = 0;

    public HashMap<Integer, Integer> queryLevels = new HashMap<>();

    public FAST(Config config, Rectangle bounds, int xGridGranularity, int maxLevel) {
        context = new Context(bounds, xGridGranularity, maxLevel);
        FAST.config = config;

        if (FAST.config.ADAPTIVE_DEG_RATIO) {
            RunCkQST.logger.info("Adaptive KNN degradation is ON! Skipping knn_deg_ratio threshold!");
        }

        index = new ConcurrentHashMap<>();
        objIndex = null;
        keywordFrequencyMap = new HashMap<>();
//        expiringObjects = new PriorityQueue<>(new ExpireTimeComparator());

        context.minInsertedLevel = -1;
        context.maxInsertedLevel = -1;

        cleaningIterator = null;
        cellBeingCleaned = null;
        lastCellCleaningDone = true;

        RunCkQST.logger.info("Index initialized!");
    }

    public static Integer calcCoordinate(int level, int x, int y, int gridGranularity) {
        return (level << 22) + y * gridGranularity + x;
    }

    public static Integer mapDataPointToPartition(int level, Point point, double step, int granularity) {
        double x = point.x;
        double y = point.y;
        int xCell = (int) ((x) / step);
        int yCell = (int) ((y) / step);
        return calcCoordinate(level, xCell, yCell, granularity);
    }

    public void setExternalObjectIndex(int objIdxLeafCapacity, int objIdxTreeHeight) {
        config.INPLACE_OBJECT_INDEX = false;
//        objIndex = new ObjectIndex();
        this.objIndex = new IQuadTree(context.bounds.min.x, context.bounds.min.y,
                context.bounds.max.x, context.bounds.max.y, objIdxLeafCapacity, objIdxTreeHeight);
//        Context.objectSearcher = (query) -> (PriorityQueue<DataObject>) objIndex.search(query);
    }

    @Override
    public void preloadObject(DataObject object) {
        if (config.INPLACE_OBJECT_INDEX) {
            insertObject(object);
        } else if (objIndex != null) {
            objIndex.insert(object);
        }
    }

    @Override
    public Collection<DataObject> insertQuery(Query query) {
        context.timestamp++;
        if (query instanceof MinimalRangeQuery) {
            Stopwatch insWatch = Stopwatch.createStarted();
            addContinuousBoundedQuery(query);
            insWatch.stop();
            queryStats.add(new QueryStat(query.id, 0,
                    insWatch.elapsed(TimeUnit.NANOSECONDS), 0, 0,
                    0, QueryStat.Stage.INSERT));

        } else if (query instanceof KNNQuery) {
            long eagerObjSearchTime = 0;
            if (objIndex != null && !config.LAZY_OBJ_SEARCH) {
                FAST.context.objectSearchCount++;
                Stopwatch objSearchWatch = Stopwatch.createStarted();
                PriorityQueue<DataObject> objResults = (PriorityQueue<DataObject>) objIndex.search(query);
                eagerObjSearchTime = objSearchWatch.elapsed(TimeUnit.NANOSECONDS);

                if (objResults.size() >= ((KNNQuery) query).k) {
                    DataObject o = objResults.peek();
//                    assert o != null;
                    ((KNNQuery) query).ar = SpatialHelper.getDistanceInBetween(((KNNQuery) query).location, o.location);
                }
                objSearchWatch.stop();
            }

            Stopwatch insWatch = Stopwatch.createStarted();
            if (((KNNQuery) query).ar <= FAST.context.globalXRange) {
                // Query with bounded range.
                addContinuousBoundedQuery(query);
                FAST.context.initBounded++;
            } else {
                // Inf. range KNN query.
                ((KNNQuery) query).currentLevel = context.maxLevel;
                queryLevels.put(query.id, context.maxLevel);
                addContinuousUnboundQuery((KNNQuery) query);
                FAST.context.initUnbounded++;
            }
            insWatch.stop();

            FAST.context.objIdxSearchTime += eagerObjSearchTime;
            FAST.context.indexingTime += insWatch.elapsed(TimeUnit.NANOSECONDS);
            FAST.context.creationTime += eagerObjSearchTime + insWatch.elapsed(TimeUnit.NANOSECONDS);
            queryStats.add(new QueryStat(query.id, eagerObjSearchTime,
                    insWatch.elapsed(TimeUnit.NANOSECONDS), ((KNNQuery) query).ar, ((KNNQuery) query).descended,
                    ((KNNQuery) query).currentLevel, QueryStat.Stage.INSERT));
        }

		// if (FAST.config.CLEAN_METHOD != CleanMethod.NO && context.timestamp % FAST.config.CLEANING_INTERVAL == 0)
		// 	cleanNextSetOfEntries();
        return null;
    }

    @Override
    public List<Query> insertObject(DataObject dataObject) {
        context.timestamp++;
//        expiringObjects.add(dataObject);
        Stopwatch insWatch = Stopwatch.createStarted();
        if (config.INPLACE_OBJECT_INDEX) {
//            throw new RuntimeException("THIS IS COMPLETELY FINE!!! SADGE");
            addContinuousBoundedQuery(dataObject);
        } else if (objIndex != null) {
            objIndex.insert(dataObject);
        }
        insWatch.stop();

        Stopwatch searchTime = Stopwatch.createStarted();
        List<Query> results = internalSearchQueries(dataObject, false);
        searchTime.stop();

        queryStats.add(new QueryStat(dataObject.id, searchTime.elapsed(TimeUnit.NANOSECONDS),
                insWatch.elapsed(TimeUnit.NANOSECONDS), 0, -1, -1, QueryStat.Stage.SEARCH));

        // Vacuum cleaning
        if (config.CLEAN_METHOD != CleanMethod.NO && context.timestamp % config.CLEANING_INTERVAL == 0)
            cleanNextSetOfEntries();

        FAST.context.searchTime += insWatch.elapsed(TimeUnit.NANOSECONDS) + searchTime.elapsed(TimeUnit.NANOSECONDS);

        // Object expiring
//        while (expiringObjects.peek() != null && expiringObjects.peek().et <= context.timestamp) {
//            expireQueries(expiringObjects.poll());
//        }

        return results;
    }

    private void addContinuousUnboundQuery(KNNQuery query) {
        if (context.minInsertedLevel == -1) {
            context.maxInsertedLevel = context.maxLevel;
            context.minInsertedLevel = context.maxLevel;
        }

        String minKeyword = getMinKeyword(context.maxLevel, query);

        int coordinate = calcCoordinate(context.maxLevel, 0, 0, 1);
        if (!index.containsKey(coordinate)) {
            Rectangle bounds = SpatialCell.getBounds(0, 0, context.globalXRange);
            index.put(coordinate, new SpatialCell(bounds, coordinate, context.maxLevel));
        }

        ArrayList<ReinsertEntry> nextLevelQueries = new ArrayList<>();
        SpatialCell cell = index.get(coordinate);
        if (SpatialHelper.overlapsSpatially(query.location, cell.bounds)) {
            cell.addInternalQueryNoShare(minKeyword, query, null, nextLevelQueries);
        }
        if (cell.textualIndex == null || cell.textualIndex.isEmpty()) {
            index.remove(coordinate);
        }
        reinsertContinuous(nextLevelQueries, context.maxLevel - 1);
    }

    private void addContinuousBoundedQuery(Query query) {
        ArrayList<ReinsertEntry> currentLevelQueries = new ArrayList<>();
        currentLevelQueries.add(new ReinsertEntry(query.spatialBox(), query));
        reinsertContinuous(currentLevelQueries, context.maxLevel);
    }

    private void reinsertContinuous(List<ReinsertEntry> currentLevelQueries, int level) {
        while (level >= 0 && !currentLevelQueries.isEmpty()) {
            ArrayList<ReinsertEntry> insertNextLevelQueries = new ArrayList<>();
            int levelGranularity = (int) (context.gridGranularity / Math.pow(2, level));
            double levelStep = ((context.globalXRange) / levelGranularity);
            for (ReinsertEntry entry : currentLevelQueries) {
                entry.query.currentLevel = level;
                queryLevels.put(entry.query.id, level);
                singleQueryInsert(level, entry, levelStep, levelGranularity, insertNextLevelQueries);
            }
            currentLevelQueries = insertNextLevelQueries;
            level--;
        }
    }

    private void singleQueryInsert(int level, ReinsertEntry entry, double levelStep, int levelGranularity, ArrayList<ReinsertEntry> insertNextLevelQueries) {
        int levelXMinCell = (int) Math.max(0, (entry.range.min.x / levelStep));
        int levelYMinCell = (int) Math.max(0, (entry.range.min.y / levelStep));
        int levelXMaxCell = (int) Math.min(context.bounds.max.x / (levelStep + .001), ((entry.range.max.x - .001) / levelStep));
        int levelYMaxCell = (int) Math.min(context.bounds.max.y / (levelStep + .001), ((entry.range.max.y - .001) / levelStep));
        if (level == 9 && (levelXMaxCell == 1 || levelXMinCell == 1 || levelYMaxCell == 1 || levelYMinCell == 1)) {
            System.out.println(entry.range);
            System.exit(1);
        }

        if (context.minInsertedLevel == -1) context.minInsertedLevel = context.maxInsertedLevel = level;
        if (level < context.minInsertedLevel) context.minInsertedLevel = level;
        if (level > context.maxInsertedLevel) context.maxInsertedLevel = level;
        String minKeyword = getMinKeyword(level, entry.query);

        int coodinate;
        QueryListNode sharedQueries = null;
        for (int i = levelXMinCell; i <= levelXMaxCell; i++) {
            for (int j = levelYMinCell; j <= levelYMaxCell; j++) {
                String statKey = level + "," + levelStep + "," + i + "," + j;
//                FAST.context.cellInsertions.put(statKey, FAST.context.cellInsertions.getOrDefault(statKey, 0) + 1);
                if (entry.query instanceof KNNQuery && (levelXMinCell != levelXMaxCell && levelYMinCell != levelYMaxCell)) {
                    double x = (((KNNQuery) entry.query).location.x / levelStep);
                    double y = (((KNNQuery) entry.query).location.y / levelStep);
                    double r = (((KNNQuery) entry.query).ar / levelStep);

                    double si = i;
                    if (i + 1 < x) si += 1;

                    double sj = j;
                    if (j + 1 < y) sj += 1;

                    double d = Math.sqrt((si - x) * (si - x) + (sj - y) * (sj - y));
                    if (!(((i < x || j < y) && d - 1 < r) || d < r)) continue;
                }
                context.totalQueryInsertionsIncludingReplications++;
                coodinate = calcCoordinate(level, i, j, levelGranularity);
                if (!index.containsKey(coodinate)) {
                    Rectangle bounds = SpatialCell.getBounds(i, j, levelStep);
                    if (bounds.min.x >= context.globalXRange || bounds.min.y >= context.globalYRange) continue;
                    index.put(coodinate, new SpatialCell(bounds, coodinate, level));
                }
                SpatialCell spatialCell = index.get(coodinate);
                if (SpatialHelper.overlapsSpatially(entry.query.spatialBox(), spatialCell.bounds)) {
                    if (i == levelXMinCell && j == levelYMinCell) {
                        sharedQueries = spatialCell.addInternalQueryNoShare(minKeyword, entry.query, null, insertNextLevelQueries);
                    } else if (sharedQueries != null && entry.query instanceof MinimalRangeQuery) {
                        spatialCell.addInternalQuery(minKeyword, (MinimalRangeQuery) entry.query, sharedQueries, insertNextLevelQueries);
                    } else spatialCell.addInternalQueryNoShare(minKeyword, entry.query, null, insertNextLevelQueries);
                }
                if (spatialCell.textualIndex == null) {
                    index.remove(coodinate);
                }

            }
        }
    }

    private void reinsertKNNQueries(List<ReinsertEntry> descendingKNNQueries) {
        throw new RuntimeException("Not implemented");
//        for (ReinsertEntry entry : descendingKNNQueries) {
//            KNNQuery query = ((KNNQuery) entry.query);
////            if (entry.query.id == 65 && query.currentLevel == 8) {
////                System.out.println("debug!");
////            }
//            int level = 0;
//            if (!FAST.config.PUSH_TO_LOWEST)
//                level = Math.min(query.calcMinSpatialLevel(), FAST.context.maxLevel);
//            query.currentLevel = level;
//            ArrayList<ReinsertEntry> insertNextLevelQueries = new ArrayList<>();
//            int levelGranularity = (int) (FAST.context.gridGranularity / Math.pow(2, level));
//            double levelStep = ((FAST.context.bounds.max.x - FAST.context.bounds.min.x) / levelGranularity);
//            if (query.id == 65) {
//                System.out.println("Reinserting: " + query + ", area: " + entry.range);
//            }
//            singleQueryInsert(level, entry, levelStep, levelGranularity, insertNextLevelQueries);
//            assert insertNextLevelQueries.isEmpty();
//        }
    }

    private List<Query> internalSearchQueries(DataObject dataObject, boolean isExpiry) {
        List<Query> result = new LinkedList<>();
        List<ReinsertEntry> descendingKNNQueries = null;
        if (!isExpiry) descendingKNNQueries = new LinkedList<>();

//        if (dataObject.id == 3657 + 10000)
//            System.out.println("Debug!");

        if (context.minInsertedLevel == -1) return result;
        double step = (context.maxInsertedLevel == 0) ? context.localXstep : (context.localXstep * (2 << (context.maxInsertedLevel - 1)));
        int granualrity = context.gridGranularity >> context.maxInsertedLevel;
        List<String> keywords = dataObject.keywords;
        for (int level = context.maxInsertedLevel; level >= context.minInsertedLevel && keywords != null && !keywords.isEmpty(); level--) {
            Integer cellCoordinates = mapDataPointToPartition(level, dataObject.location, step, granualrity);
            SpatialCell spatialCellOptimized = index.get(cellCoordinates);
            if (spatialCellOptimized != null) {
                List<String> tempKeywords = spatialCellOptimized.searchQueries(dataObject, keywords, result, descendingKNNQueries, isExpiry);
                if (config.INCREMENTAL_DESCENT) {
                    keywords = tempKeywords;
                }
            }
            step /= 2;
            granualrity <<= 1;
        }
        if (descendingKNNQueries != null && !descendingKNNQueries.isEmpty()) {
            if (FAST.config.INCREMENTAL_DESCENT) {
                reinsertContinuous(descendingKNNQueries, FAST.context.maxLevel - 1);
            } else {
                reinsertKNNQueries(descendingKNNQueries);
            }
        }

//        if (dataObject.id == 3657 + 10000) {
//            System.out.println("Debug!");
//            result.forEach(q -> System.out.println("Res: " + q));
//        }
        return result;
    }

    public void cleanNextSetOfEntries() {
        RunCkQST.logger.debug("Cleaning!");
        Stopwatch cleanWatch = Stopwatch.createStarted();
        if (cleaningIterator == null || !cleaningIterator.hasNext()) cleaningIterator = index.entrySet().iterator();
        SpatialCell cell;
        if (lastCellCleaningDone) 
            cell = cleaningIterator.next().getValue();
        else 
            cell = cellBeingCleaned;
        boolean cleaningDone = cell.clean();
        if (!cleaningDone) {
            cellBeingCleaned = cell;
        }
        if (cell.textualIndex == null) cleaningIterator.remove();
        cleanWatch.stop();
        this.cleanTime += cleanWatch.elapsed(TimeUnit.NANOSECONDS);
        RunCkQST.logger.debug("Cleaning cell: " + cell.coordinate + " at level: " + cell.level);
    }

//    public void expireQueries(DataObject dataObject) {
////        Run.logger.debug("Obj expire: " + dataObject.id);
////        if (dataObject.id == 114) {
////            System.out.println("DEBUG!");
////        }
//        List<Query> results = internalSearchQueries(dataObject, true);
////        if (!results.isEmpty() && dataObject.id == 114) {
////            Run.logger.debug(context.timestamp + ", " + dataObject);
////            System.out.println(results);
////        }
//        List<KNNQuery> ascending = new ArrayList<>();
//        results.forEach(query -> {
//            if (query instanceof KNNQuery && ((KNNQuery) query).currentLevel != context.maxLevel) {
//                boolean before = ((KNNQuery) query).kFilled();
//                ((KNNQuery) query).getMonitoredObjects().remove(dataObject);
//                assert before != ((KNNQuery) query).kFilled();
//                ((KNNQuery) query).ar = Double.MAX_VALUE;
//                ascending.add((KNNQuery) query);
//            }
//        });
//////        Run.logger.debug("Ascending queries: " + ascending);
//        reinsertKNNQueries(ascending);
//
//        for (KNNQuery query : ascending) {
//            assert query.currentLevel == context.maxLevel;
//        }
//    }

    public String getMinKeyword(int level, Query query) {
        String minkeyword = null;
        int minCount = Integer.MAX_VALUE;
        for (String keyword : query.keywords) {
            KeywordFrequency stats = keywordFrequencyMap.get(keyword);
            if (stats == null) {
                stats = new KeywordFrequency(1, 1, context.timestamp);
                keywordFrequencyMap.put(keyword, stats);
                minkeyword = keyword;
                minCount = 0;
            } else {
                if (level == context.maxLevel) stats.queryCount++;
                if (stats.queryCount < minCount) {

                    minCount = stats.queryCount;
                    minkeyword = keyword;
                }
            }
        }
        return minkeyword;
    }

    public void setCleaning(CleanMethod method) {
        config.CLEAN_METHOD = method;
    }

    public void printFrequencies() {
        System.out.println(keywordFrequencyMap.toString());
    }

    public void printIndex() {
        System.out.println("Bounds=" + context.bounds.toString());
        index.forEach((key, v) -> {
            System.out.println("Level: " + v.level + ", Key: " + v.coordinate + " -->");
            v.textualIndex.forEach((keyword, node) -> printTextualNode(keyword, node, 1));
        });
    }

    private void printTextualNode(String keyword, TextualNode node, int level) {
        if (level == 4) return;

        System.out.print("|");
        for (int i = 0; i < level; i++) {
            System.out.print("──");
        }
        System.out.print(keyword + " -> ");

        if (node instanceof QueryNode) {
            System.out.println(((QueryNode) node).query.id + ":" + ((KNNQuery) ((QueryNode) node).query).ar);
        } else if (node instanceof QueryListNode) {
            printQueryList(((QueryListNode) node).queries);
        } else if (node instanceof QueryTrieNode) {
            if (((QueryTrieNode) node).queries != null) {
                printQueryList(((QueryTrieNode) node).queries);
            } else {
                System.out.println();
            }

            if (((QueryTrieNode) node).subtree != null && !((QueryTrieNode) node).subtree.isEmpty()) {
                ((QueryTrieNode) node).subtree.forEach((t, u) -> printTextualNode(t, u, level + 1));
            }
        }
    }

    private void printQueryList(Iterable<Query> queries) {
        StringBuilder s = new StringBuilder();
        for (Query query : queries) {
            s.append(query.id).append(":").append(((KNNQuery) query).ar).append(", ");
        }
        System.out.println(s);
    }

    public ArrayList<KNNQuery> allKNNQueries(boolean onlyTrie) {
        ArrayList<KNNQuery> allKNN = new ArrayList<>();

        index.forEach((key, cell) -> {
//            if (cell.level == context.maxLevel) {
//                System.out.println("Key: " + key);
                addKNNQueriesFromTextualNodes(cell.textualIndex, allKNN, onlyTrie);
//            }
        });
        return allKNN;
    }

    private void addKNNQueriesFromTextualNodes(
            Map<String, TextualNode> textualIndex, ArrayList<KNNQuery> result,
            boolean onlyTrie
    ) {
        if (textualIndex == null) return;

        textualIndex.forEach((keyword, node) -> {

            if (!onlyTrie && node instanceof QueryNode) {
                Query query = ((QueryNode) node).query;
                if (query instanceof KNNQuery) {
                    result.add((KNNQuery) query);
                } else {
                    throw new RuntimeException("Unknown query type");
                }
            } else if (!onlyTrie && node instanceof QueryListNode) {
                if (((QueryListNode) node).queries != null) {
                    result.addAll(((QueryListNode) node).queries.kNNQueries());
                }
            } else if (node instanceof QueryTrieNode) {
                if (((QueryTrieNode) node).unboundedQueries != null) {
                    result.addAll(((QueryTrieNode) node).unboundedQueries);
                }
                if (((QueryTrieNode) node).queries != null) {
                    result.addAll(((QueryTrieNode) node).queries.kNNQueries());
                }
                addKNNQueriesFromTextualNodes(((QueryTrieNode) node).subtree, result, onlyTrie);
            } else {
                throw new RuntimeException("Unknown error");
            }
        });
    }
}
