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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.structures.*;

public class FAST {
    public static int Trie_SPLIT_THRESHOLD = 2;
    public static int Degradation_Ratio = 2;
    public static int Trie_OVERLALL_MERGE_THRESHOLD = 2;
    public static int CLEANING_INTERVAL = 1000;
    public static int NUMBER_OF_ACTIVE_QUERIES = 1000000;
    public static int MAX_ENTRIES_PER_CLEANING_INTERVAL = 10;
    public static HashMap<String, KeywordFrequency> keywordFrequencyMap;
    public static int totalVisited = 0;
    public static int spatialOverlappingQueries = 0;
    public static int queryTimeStampCounter;
    public static int objectTimeStampCounter;
    public static int debugQueryId = -1;
    public static int queryInsertInvListNodeCounter = 0;
    public static int queryInsertTrieNodeCounter = 0;
    public static int totalQueryInsertionsIncludingReplications = 0;
    public static int objectSearchInvListNodeCounter = 0;
    public static int objectSearchTrieNodeCounter = 0;
    public static int objectSearchInvListHashAccess = 0;
    public static int objectSearchTrieHashAccess = 0;
    public static int numberOfHashEntries = 0;
    public static int numberOfTrieNodes = 0;
    public static int totalTrieAccess = 0;
    public static int objectSearchTrieFinalNodeCounter = 0;
    public double localXstep;
    public double localYstep;
    public Rectangle bounds;
    public int gridGranularity;
    public int maxLevel;
    public SpatialCell cellBeingCleaned;
    public boolean lastCellCleaningDone; //to check if an entireCellHasBeenCleaned
    public int minInsertedLevel;
    public int maxInsertedLevel;
    public int minInsertedLevelInterleaved;
    public int maxInsertedLevelInterleaved;
    public ConcurrentHashMap<Integer, SpatialCell> index;
    public Iterator<Entry<Integer, SpatialCell>> cleaningIterator;//iterates over cells to clean expired entries

    public FAST(Rectangle bounds, Integer xGridGranularity, int maxLevel) {
        this.bounds = bounds;
        Double globalXrange = SpatioTextualConstants.xMaxRange;
        Double globalYrange = SpatioTextualConstants.yMaxRange;
        this.gridGranularity = xGridGranularity;
        this.localXstep = (globalXrange / this.gridGranularity);
        this.localYstep = (globalYrange / this.gridGranularity);
        this.maxLevel = Math.min((int) (Math.log(gridGranularity) / Math.log(2)), maxLevel);
        this.minInsertedLevel = -1;
        this.maxInsertedLevel = -1;
        this.minInsertedLevelInterleaved = -1;
        this.maxInsertedLevelInterleaved = -1;
        index = new ConcurrentHashMap<>();
        keywordFrequencyMap = new HashMap<>();

        queryInsertInvListNodeCounter = 0;
        queryInsertTrieNodeCounter = 0;
        totalQueryInsertionsIncludingReplications = 0;
        objectSearchInvListNodeCounter = 0;
        objectSearchTrieNodeCounter = 0;
        objectSearchTrieFinalNodeCounter = 0;
        objectSearchInvListHashAccess = 0;
        objectSearchTrieHashAccess = 0;
        numberOfHashEntries = 0;
        numberOfTrieNodes = 0;
        totalTrieAccess = 0;
        queryTimeStampCounter = 0;
        objectTimeStampCounter = 0;
        cleaningIterator = null;
        cellBeingCleaned = null;
        lastCellCleaningDone = true;
    }

    public static Integer calcCoordinate(int level, int x, int y, int gridGranularity) {
        return (level << 22) + y * gridGranularity + x;
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

    public void addContinuousQuery(Query query) {
        if (query instanceof MinimalRangeQuery) {
            addContinuousMinimalRangeQuery((MinimalRangeQuery) query);
        } else if (query instanceof KNNQuery) {
            addContinuousKNNQuery((KNNQuery) query);
        }
    }

    public void addContinuousKNNQuery(KNNQuery query) {
        if (minInsertedLevel == -1) {
            maxInsertedLevel = maxLevel;
            minInsertedLevel = maxLevel;
        }

        String minKeyword = getMinKeyword(maxLevel, query);

        int coordinate = calcCoordinate(maxLevel, 0, 0, 1);
        if (!index.containsKey(coordinate)) {
            Rectangle bounds = SpatialCell.getBounds(0, 0, SpatioTextualConstants.xMaxRange);
            index.put(coordinate, new SpatialCell(bounds, coordinate, maxLevel));
        }

        ArrayList<ReinsertEntry> nextLevelQueries = new ArrayList<>();
        SpatialCell cell = index.get(coordinate);
        if (SpatialHelper.overlapsSpatially(query.location, cell.bounds)) {
            cell.addInternalQueryNoShare(minKeyword, query, null, nextLevelQueries);
        }
        if (cell.textualIndex == null || cell.textualIndex.isEmpty()) {
            index.remove(coordinate);
        }
        reinsertContinuous(nextLevelQueries, maxLevel - 1);
    }

    public void addContinuousMinimalRangeQuery(MinimalRangeQuery query) {
        ArrayList<ReinsertEntry> currentLevelQueries = new ArrayList<>();
        currentLevelQueries.add(new ReinsertEntry(query.spatialRange, query));
        reinsertContinuous(currentLevelQueries, maxLevel);
    }

    private void reinsertContinuous(ArrayList<ReinsertEntry> currentLevelQueries, int level) {
        queryTimeStampCounter++;

        while (level >= 0 && !currentLevelQueries.isEmpty()) {
            ArrayList<ReinsertEntry> insertNextLevelQueries = new ArrayList<>();
            int levelGranularity = (int) (gridGranularity / Math.pow(2, level));
            double levelStep = (SpatioTextualConstants.xMaxRange / levelGranularity);
            for (ReinsertEntry entry : currentLevelQueries) {

                int levelxMinCell = (int) (entry.query.spatialRange.min.x / levelStep);
                int levelyMinCell = (int) (entry.query.spatialRange.min.y / levelStep);
                int levelxMaxCell = (int) (entry.query.spatialRange.max.x / levelStep);
                int levelyMaxCell = (int) (entry.query.spatialRange.max.y / levelStep);

                if (entry.range != null) {
                    if (entry.query.id == debugQueryId)
                        System.out.println("reinsert" + entry.query);

                    levelxMinCell = (int) (entry.range.min.x / levelStep);
                    levelyMinCell = (int) (entry.range.min.y / levelStep);
                    levelxMaxCell = (int) (entry.range.max.x / levelStep);
                    levelyMaxCell = (int) (entry.range.max.y / levelStep);
                }

                if (minInsertedLevel == -1)
                    minInsertedLevel = maxInsertedLevel = level;
                if (level < minInsertedLevel)
                    minInsertedLevel = level;
                if (level > maxInsertedLevel)
                    maxInsertedLevel = level;
                String minKeyword = getMinKeyword(level, entry.query);

                int coodinate;
                QueryListNode sharedQueries = null;
                for (int i = levelxMinCell; i <= levelxMaxCell; i++) {
                    for (int j = levelyMinCell; j <= levelyMaxCell; j++) {
                        totalQueryInsertionsIncludingReplications++;
                        coodinate = calcCoordinate(level, i, j, levelGranularity);
                        if (!index.containsKey(coodinate)) {
                            Rectangle bounds = SpatialCell.getBounds(i, j, levelStep);
                            if (bounds.min.x >= SpatioTextualConstants.xMaxRange || bounds.min.y >= SpatioTextualConstants.yMaxRange)
                                continue;
                            index.put(coodinate, new SpatialCell(bounds, coodinate, level));
                        }
                        SpatialCell spatialCell = index.get(coodinate);
                        if (SpatialHelper.overlapsSpatially(entry.query.spatialRange, spatialCell.bounds)) {
                            if (i == levelxMinCell && j == levelyMinCell) {
                                sharedQueries = spatialCell.addInternalQueryNoShare(minKeyword, entry.query, null, insertNextLevelQueries);
                            } else if (sharedQueries != null) {
                                spatialCell.addInternalQuery(minKeyword, entry.query, sharedQueries, insertNextLevelQueries);
                            } else
                                spatialCell.addInternalQueryNoShare(minKeyword, entry.query, null, insertNextLevelQueries);
                        }
                        if (spatialCell.textualIndex == null) {
                            index.remove(coodinate);
                        }

                    }
                }
            }
            currentLevelQueries = insertNextLevelQueries;
            level--;
        }
    }

    public void cleanNextSetOfEntries() {
        if (cleaningIterator == null || !cleaningIterator.hasNext())
            cleaningIterator = index.entrySet().iterator();
        SpatialCell cell;
        if (lastCellCleaningDone)
            cell = cleaningIterator.next().getValue();
        else
            cell = cellBeingCleaned;
        boolean cleaningDone = cell.clean();
        if (!cleaningDone) {
            cellBeingCleaned = cell;
        }
        if (cell.textualIndex == null)
            cleaningIterator.remove();
    }

    public Integer getCountPerKeywordsAll(ArrayList<String> keywords) {
        return 0;
    }

    public Integer mapDataPointToPartition(int level, Point point, double step, int granularity) {
        double x = point.x;
        double y = point.y;
        int xCell = (int) ((x) / step);
        int yCell = (int) ((y) / step);
        return calcCoordinate(level, xCell, yCell, granularity);
    }

    public List<Query> searchQueries(DataObject dataObject) {
        objectTimeStampCounter++;
        List<Query> result = new LinkedList<>();
        if (minInsertedLevel == -1)
            return result;
        double step = (maxInsertedLevel == 0) ? localXstep : (localXstep * (2 << (maxInsertedLevel - 1)));
        int granualrity = this.gridGranularity >> maxInsertedLevel;
        List<String> keywords = dataObject.keywords;
        for (int level = maxInsertedLevel; level >= minInsertedLevel && keywords != null && !keywords.isEmpty(); level--) {
            Integer cellCoordinates = mapDataPointToPartition(level, dataObject.location, step, granualrity);
            SpatialCell spatialCellOptimized = index.get(cellCoordinates);
            if (spatialCellOptimized != null) {
                keywords = spatialCellOptimized.searchQueries(dataObject, keywords, result);
            }
            step /= 2;
            granualrity <<= 1;
        }

        return result;
    }

    public String getMinKeyword(int level, Query query) {
        String minkeyword = null;
        int minCount = Integer.MAX_VALUE;
        for (String keyword : query.keywords) {
            KeywordFrequency stats = keywordFrequencyMap.get(keyword);
            if (stats == null) {
                stats = new KeywordFrequency(1, 1, objectTimeStampCounter);
                keywordFrequencyMap.put(keyword, stats);
                minkeyword = keyword;
                minCount = 0;
            } else {
                if (level == maxLevel)
                    stats.queryCount++;
                if (stats.queryCount < minCount) {

                    minCount = stats.queryCount;
                    minkeyword = keyword;
                }
            }
        }
        return minkeyword;
    }

    public void printFrequencies() {
        System.out.println(keywordFrequencyMap.toString());
    }

    public void printIndex() {
        System.out.println("Bounds=" + bounds.toString());
        index.forEach((key, v) -> {
            System.out.println("Level: " + v.level + ", Key: " + v.coordinate + " -->");
            v.textualIndex.forEach((keyword, node) -> {
                printTextualNode(keyword, node, 1);
            });
        });
    }

    private void printTextualNode(String keyword, TextualNode node, int level) {
        if (level == 4)
            return;

        System.out.print("|" + "──".repeat(level) + keyword + " -> ");

        if (node instanceof QueryNode) {
            System.out.println(((QueryNode) node).query.id);
        } else if (node instanceof QueryListNode) {
            List<Query> queries = ((QueryListNode) node).queries.allQueries();
            System.out.println(String.join(", ", queries.stream().map((Query q) -> q.id + "").toList()));
        } else if (node instanceof QueryTrieNode) {
            if (((QueryTrieNode) node).queries != null) {
                List<Query> queries = ((QueryTrieNode) node).queries.allQueries();
                System.out.print(String.join(", ", queries.stream().map((Query q) -> q.id + "").toList()));
            }
            System.out.println();
            if (((QueryTrieNode) node).subtree != null)
                ((QueryTrieNode) node).subtree.forEach((t, u) -> printTextualNode(t, u, level + 1));
        }
    }

}
