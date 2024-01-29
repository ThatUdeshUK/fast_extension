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

import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.config.Context;
import edu.purdue.cs.fast.helper.ExpireTimeComparator;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.structures.*;


public class FAST implements SpatialKeywordIndex<Query, DataObject> {
    public static Context context;
    public static Config config;
    public static HashMap<String, KeywordFrequency> keywordFrequencyMap;
    private final boolean lastCellCleaningDone; //to check if an entireCellHasBeenCleaned
    public ConcurrentHashMap<Integer, SpatialCell> index;
    public Iterator<Entry<Integer, SpatialCell>> cleaningIterator;//iterates over cells to clean expired entries
    public PriorityQueue<DataObject> expiringObjects;
    private SpatialCell cellBeingCleaned;

    public FAST(Rectangle bounds, Integer xGridGranularity, int maxLevel) {
        context = new Context(bounds, xGridGranularity, maxLevel);
        config = new Config();
        index = new ConcurrentHashMap<>();
        keywordFrequencyMap = new HashMap<>();
        expiringObjects = new PriorityQueue<>(new ExpireTimeComparator());

        context.minInsertedLevel = -1;
        context.maxInsertedLevel = -1;

        cleaningIterator = null;
        cellBeingCleaned = null;
        lastCellCleaningDone = true;

        Run.logger.info("Index initialized!");
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

    public double getAverageRankedInvListSize() {
        double sum = 0.0, count = 0.0;
        ConcurrentHashMap<Integer, SpatialCell> levelIndex = index;
        for (SpatialCell cell : levelIndex.values()) {
            if (cell.textualIndex != null) {
                for (TextualNode node : cell.textualIndex.values()) {
                    if (node instanceof QueryListNode && !((QueryListNode) node).queries.isEmpty()) {
                        count++;
                        sum += ((QueryListNode) node).queries.size();
                    }
                    if (node instanceof QueryTrieNode && ((QueryTrieNode) node).queries != null && !((QueryTrieNode) node).queries.isEmpty()) {
                        count++;
                        sum += ((QueryTrieNode) node).queries.size();
                    }
                }
            }
        }
        return sum / count;
    }

    @Override
    public void preloadObject(DataObject object) {
        insertObject(object);
    }

    @Override
    public Collection<DataObject> insertQuery(Query query) {
        context.timestamp++;
        if (query instanceof MinimalRangeQuery) {
            addContinuousMinimalRangeQuery((MinimalRangeQuery) query);
        } else if (query instanceof KNNQuery) {
            ((KNNQuery) query).currentLevel = context.maxLevel;
            addContinuousKNNQuery((KNNQuery) query);
        }

//		if (FAST.cleanMethod != CleanMethod.NO && context.timestamp % CLEANING_INTERVAL == 0)
//			cleanNextSetOfEntries();
        return null;
    }

    public void addContinuousKNNQuery(KNNQuery query) {
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

    public void addContinuousMinimalRangeQuery(MinimalRangeQuery query) {
        ArrayList<ReinsertEntry> currentLevelQueries = new ArrayList<>();
        currentLevelQueries.add(new ReinsertEntry(query.spatialRange, query));
        reinsertContinuous(currentLevelQueries, context.maxLevel);
    }

    private void reinsertContinuous(List<ReinsertEntry> currentLevelQueries, int level) {
        while (level >= 0 && !currentLevelQueries.isEmpty()) {
            ArrayList<ReinsertEntry> insertNextLevelQueries = new ArrayList<>();
            int levelGranularity = (int) (context.gridGranularity / Math.pow(2, level));
            double levelStep = ((context.globalXRange) / levelGranularity);
            for (ReinsertEntry entry : currentLevelQueries) {
                if (entry.query.id == 31) {
                    System.out.println("DEBUG");
                    System.out.println("Inserting descending: " + entry.query + ", to level:" + level);
                }
                ((KNNQuery) entry.query).currentLevel = level;
                singleQueryInsert(level, entry, levelStep, levelGranularity, insertNextLevelQueries);
            }
            currentLevelQueries = insertNextLevelQueries;
            level--;
        }
    }

    private void singleQueryInsert(int level, ReinsertEntry entry, double levelStep, int levelGranularity, ArrayList<ReinsertEntry> insertNextLevelQueries) {
        int levelXMinCell = (int) (entry.range.min.x / levelStep);
        int levelYMinCell = (int) (entry.range.min.y / levelStep);
        int levelXMaxCell = (int) ((entry.range.max.x - .001) / levelStep);
        int levelYMaxCell = (int) ((entry.range.max.y - .001) / levelStep);

        if (context.minInsertedLevel == -1)
            context.minInsertedLevel = context.maxInsertedLevel = level;
        if (level < context.minInsertedLevel)
            context.minInsertedLevel = level;
        if (level > context.maxInsertedLevel)
            context.maxInsertedLevel = level;
        String minKeyword = getMinKeyword(level, entry.query);

        int coodinate;
        QueryListNode sharedQueries = null;
        for (int i = levelXMinCell; i <= levelXMaxCell; i++) {
            for (int j = levelYMinCell; j <= levelYMaxCell; j++) {
                if (entry.query instanceof KNNQuery && (levelXMinCell != levelXMaxCell && levelYMinCell != levelYMaxCell)) {
                    double x = (((KNNQuery) entry.query).location.x / levelStep);
                    double y = (((KNNQuery) entry.query).location.y / levelStep);
                    double r = (((KNNQuery) entry.query).ar / levelStep);

                    double si = i;
                    if (i + 1 < x)
                        si += 1;

                    double sj = j;
                    if (j + 1 < y)
                        sj += 1;

                    double d = Math.sqrt((si - x) * (si - x) + (sj - y) * (sj - y));
                    if (!(((i < x || j < y) && d - 1 < r) || d < r))
                        continue;
                }
                context.totalQueryInsertionsIncludingReplications++;
                coodinate = calcCoordinate(level, i, j, levelGranularity);
                if (!index.containsKey(coodinate)) {
                    Rectangle bounds = SpatialCell.getBounds(i, j, levelStep);
                    if (bounds.min.x >= context.globalXRange || bounds.min.y >= context.globalYRange)
                        continue;
                    index.put(coodinate, new SpatialCell(bounds, coodinate, level));
                }
                SpatialCell spatialCell = index.get(coodinate);
                if (SpatialHelper.overlapsSpatially(entry.query.spatialBox(), spatialCell.bounds)) {
                    if (i == levelXMinCell && j == levelYMinCell) {
                        sharedQueries = spatialCell.addInternalQueryNoShare(minKeyword, entry.query, null, insertNextLevelQueries);
                    } else if (sharedQueries != null && entry.query instanceof MinimalRangeQuery) {
                        spatialCell.addInternalQuery(minKeyword, (MinimalRangeQuery) entry.query, sharedQueries, insertNextLevelQueries);
                    } else
                        spatialCell.addInternalQueryNoShare(minKeyword, entry.query, null, insertNextLevelQueries);
                }
                if (spatialCell.textualIndex == null) {
                    index.remove(coodinate);
                }

            }
        }
    }

    private void reinsertKNNQueries(List<KNNQuery> descendingKNNQueries) {

        for (KNNQuery query : descendingKNNQueries) {
            if (query.id == 65 && query.currentLevel == 8) {
                System.out.println("debug!");
            }
            int level = 0;
            if (FAST.config.INCREMENTAL_DESCENT) {
                query.currentLevel--;
                level = query.currentLevel;
            } else {
                if (!FAST.config.PUSH_TO_LOWEST)
                    level = Math.min(query.calcMinSpatialLevel(), FAST.context.maxLevel);
                query.currentLevel = level;
            }
            ArrayList<ReinsertEntry> insertNextLevelQueries = new ArrayList<>();
            int levelGranularity = (int) (FAST.context.gridGranularity / Math.pow(2, level));
            double levelStep = ((FAST.context.bounds.max.x - FAST.context.bounds.min.x) / levelGranularity);
            ReinsertEntry entry = new ReinsertEntry(SpatialHelper.spatialIntersect(FAST.context.bounds, query.spatialBox()), query);
            if (entry.query.id == 65) {
                System.out.println("Reinserting: " + query + ", area: " + entry.range);
            }
            singleQueryInsert(level, entry, levelStep, levelGranularity, insertNextLevelQueries);
            assert insertNextLevelQueries.isEmpty();
        }
    }

    @Override
    public List<Query> insertObject(DataObject dataObject) {
        context.timestamp++;
        expiringObjects.add(dataObject);
        List<Query> results = internalSearchQueries(dataObject, false);

        // Vacuum cleaning
        if (config.CLEAN_METHOD != CleanMethod.NO && context.timestamp % config.CLEANING_INTERVAL == 0)
            cleanNextSetOfEntries();

        // Object expiring
//        while (expiringObjects.peek() != null && expiringObjects.peek().et <= context.timestamp) {
//            expireQueries(expiringObjects.poll());
//        }

        return results;
    }

    private List<Query> internalSearchQueries(DataObject dataObject, boolean isExpiry) {
        if (dataObject.id == 231) {
            System.out.println("DEBUG!");
            System.out.println("Streaming obj:" + dataObject);
        }
        List<Query> result = new LinkedList<>();
        List<ReinsertEntry> descendingKNNQueries = null;
        if (!isExpiry)
            descendingKNNQueries = new LinkedList<>();

        if (context.minInsertedLevel == -1)
            return result;
        double step = (context.maxInsertedLevel == 0) ? context.localXstep : (context.localXstep * (2 << (context.maxInsertedLevel - 1)));
        int granualrity = context.gridGranularity >> context.maxInsertedLevel;
        List<String> keywords = dataObject.keywords;
        for (int level = context.maxInsertedLevel; level >= context.minInsertedLevel && keywords != null && !keywords.isEmpty(); level--) {
            Integer cellCoordinates = mapDataPointToPartition(level, dataObject.location, step, granualrity);
            SpatialCell spatialCellOptimized = index.get(cellCoordinates);
            if (spatialCellOptimized != null) {
                // TODO - DANGER you removed `keywords =`
                List<String> tempKeywords = spatialCellOptimized.searchQueries(dataObject, keywords, result, descendingKNNQueries, isExpiry);
                if (config.INCREMENTAL_DESCENT) {
                    keywords = tempKeywords;
                }
            }
            step /= 2;
            granualrity <<= 1;
        }
        if (descendingKNNQueries != null && !descendingKNNQueries.isEmpty()) {
//            reinsertKNNQueries(descendingKNNQueries);
            if (dataObject.id == 231) {
                System.out.println("DEBUG");
            }
            reinsertContinuous(descendingKNNQueries, FAST.context.maxLevel - 1);
        }

        return result;
    }

    public void expireQueries(DataObject dataObject) {
//        Run.logger.debug("Obj expire: " + dataObject.id);
//        if (dataObject.id == 114) {
//            System.out.println("DEBUG!");
//        }
        List<Query> results = internalSearchQueries(dataObject, true);
//        if (!results.isEmpty() && dataObject.id == 114) {
//            Run.logger.debug(context.timestamp + ", " + dataObject);
//            System.out.println(results);
//        }
        List<KNNQuery> ascending = new ArrayList<>();
        results.forEach(query -> {
            if (query instanceof KNNQuery && ((KNNQuery) query).currentLevel != context.maxLevel) {
                boolean before = ((KNNQuery) query).kFilled();
                ((KNNQuery) query).getMonitoredObjects().remove(dataObject);
                assert before != ((KNNQuery) query).kFilled();
                ((KNNQuery) query).ar = Double.MAX_VALUE;
                ascending.add((KNNQuery) query);
            }
        });
////        Run.logger.debug("Ascending queries: " + ascending);
        reinsertKNNQueries(ascending);

        for (KNNQuery query : ascending) {
            assert query.currentLevel == context.maxLevel;
        }
    }

    public void cleanNextSetOfEntries() {
        Run.logger.debug("Cleaning!");
        if (cleaningIterator == null || !cleaningIterator.hasNext())
            cleaningIterator = index.entrySet().iterator();
        SpatialCell cell;
        if (lastCellCleaningDone)
            cell = cleaningIterator.next().getValue();
        else
            cell = cellBeingCleaned;
        Run.logger.debug("Cleaning cell: " + cell.coordinate + " at level: " + cell.level);
        boolean cleaningDone = cell.clean();
        if (!cleaningDone) {
            cellBeingCleaned = cell;
        }
        if (cell.textualIndex == null)
            cleaningIterator.remove();
    }

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
                if (level == context.maxLevel)
                    stats.queryCount++;
                if (stats.queryCount < minCount) {

                    minCount = stats.queryCount;
                    minkeyword = keyword;
                }
            }
        }
        return minkeyword;
    }

    public void setPushToLowest(boolean pushToLowest) {
        config.PUSH_TO_LOWEST = pushToLowest;
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
        if (level == 4)
            return;

        System.out.print("|");
        for (int i = 0; i < level; i++) {
            System.out.print("──");
        }
        System.out.print(keyword + " -> ");

        if (node instanceof QueryNode) {
            System.out.println(((QueryNode) node).query.id);
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
            s.append(query.id).append(", ");
        }
        System.out.println(s);
    }

    public HashSet<KNNQuery> allKNNQueries() {
        HashSet<KNNQuery> allKNN = new HashSet<>();

        index.forEach((key, cell) -> {
            addKNNQueriesFromTextualNodes(cell.textualIndex, allKNN);
        });
        return allKNN;
    }

    private void addKNNQueriesFromTextualNodes(Map<String, TextualNode> textualIndex, HashSet<KNNQuery> result) {
        if (textualIndex == null)
            return;

        textualIndex.forEach((keyword, node) -> {

            if (node instanceof QueryNode) {
                Query query = ((QueryNode) node).query;
                if (query instanceof KNNQuery) {
                    result.add((KNNQuery) query);
                }
            } else if (node instanceof QueryListNode) {
                if (((QueryListNode) node).queries != null) {
                    result.addAll(((QueryListNode) node).queries.kNNQueries());
                }
            } else if (node instanceof QueryTrieNode) {
                if (((QueryTrieNode) node).queries != null) {
                    result.addAll(((QueryTrieNode) node).queries.kNNQueries());
                    addKNNQueriesFromTextualNodes(((QueryTrieNode) node).subtree, result);
                }
            }
        });
    }
}

