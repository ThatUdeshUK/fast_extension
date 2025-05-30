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
 */
package edu.purdue.cs.fast.structures;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.RunCkQST;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.experiments.Experiment;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;

public class SpatialCell implements Serializable {
    public ConcurrentHashMap<String, TextualNode> textualIndex;
    public Rectangle bounds;
    public int coordinate;
    public int level;

    Iterator<Entry<String, TextualNode>> cleaningIterator;

    public SpatialCell(Rectangle bounds, Integer coordinate, int level) {
        this.bounds = bounds;
        this.bounds.max.x -= .001;
        this.bounds.max.y -= .001;
        this.coordinate = coordinate;
        this.level = level;
    }

    public static Rectangle getBounds(int i, int j, double step) {
        return new Rectangle(new Point(i * step, j * step), new Point((i + 1) * step, (j + 1) * step));
    }

    public void addInternalQuery(String keyword, MinimalRangeQuery query,
                                 QueryListNode sharedQueries, ArrayList<ReinsertEntry> insertNextLevelQueries) {
        if (textualIndex == null) {
            textualIndex = new ConcurrentHashMap<>();
        }

        if (!textualIndex.containsKey(keyword) && sharedQueries != null) {
            FAST.context.numberOfHashEntries++;
            textualIndex.put(keyword, sharedQueries);
        } else {
            if (sharedQueries == null)
                return;

            TextualNode node = textualIndex.get(keyword);
            if (node instanceof QueryNode) {
                Query exitingQuery = ((QueryNode) node).query;
                if (exitingQuery.et > FAST.context.timestamp) { //checking for the support of the query
                    if (sharedQueries.queries.contains(exitingQuery)) {
                        textualIndex.put(keyword, sharedQueries);
                    } else if (sharedQueries.queries.mbrQueries().size() < FAST.config.TRIE_SPLIT_THRESHOLD) {
                        FAST.context.queryInsertInvListNodeCounter++;
                        sharedQueries.queries.add(exitingQuery);
                        textualIndex.put(keyword, sharedQueries);
                    } else
                        addInternalQueryNoShare(keyword, query, sharedQueries, insertNextLevelQueries);
                } else {
                    deleteQueryFromStats(query);
                    textualIndex.put(keyword, sharedQueries);
                }
            } else if (node instanceof QueryListNode && node == sharedQueries) {
                //already inserted do nothing;
            } else if (node instanceof QueryListNode) {
                ArrayList<Query> nonSharedQueries = new ArrayList<>();
                for (Query q : ((QueryListNode) node).queries) {
                    if (!sharedQueries.queries.contains(q))
                        nonSharedQueries.add(q);
                }
                if (!nonSharedQueries.isEmpty() && nonSharedQueries.size() + sharedQueries.queries.size() <= FAST.config.TRIE_SPLIT_THRESHOLD) {
                    sharedQueries.queries.addAll(nonSharedQueries);
                    textualIndex.put(keyword, sharedQueries);
                } else {
                    addInternalQueryNoShare(keyword, query, sharedQueries, insertNextLevelQueries);
                }
            } else {
                addInternalQueryNoShare(keyword, query, sharedQueries, insertNextLevelQueries);
            }
        }
    }

    public QueryListNode addInternalQueryNoShare(String keyword, Query query, QueryListNode sharedQueries,
                                                 ArrayList<ReinsertEntry> insertNextLevelQueries) {
        if (textualIndex == null) {
            textualIndex = new ConcurrentHashMap<>();
        }
        Queue<Query> queue = new LinkedList<>();
        boolean inserted = insertAtKeyWord(keyword, query, sharedQueries);
        if (inserted) {
            FAST.context.unboundedCounter++;
            if (textualIndex.get(keyword) instanceof QueryListNode)
                return (QueryListNode) textualIndex.get(keyword);
            else
                return null;
        } else {
            queue.add(query);
        }

        while (!queue.isEmpty()) {
            query = queue.remove();
            inserted = false;

            String otherKeyword = getOtherKeywordToInsert(query);
            if (otherKeyword != null) {
                inserted = insertAtKeyWord(otherKeyword, query, null);
                FAST.context.unboundedCounter2++;
            }
            if (inserted) {
                continue;
            } else {
                for (String term : query.keywords) {
                    //mark all these keywords as tries
                    if (textualIndex.get(term) instanceof QueryListNode) {
                        queue.addAll(((QueryListNode) textualIndex.get(term)).queries.mbrQueries());
                        List<KNNQuery> leaving = ((QueryListNode) textualIndex.get(term)).queries.kNNQueries();
                        FAST.context.removed += leaving.size();
                        queue.addAll(leaving);
                        textualIndex.put(term, new QueryTrieNode(level));
                        FAST.context.numberOfTries++;
                    }
                }
            }

            QueryTrieNode trieNode = (QueryTrieNode) textualIndex.get(query.keywords.get(0));
            for (int j = 1; j < query.keywords.size() & !inserted; j++) {
                keyword = query.keywords.get(j);
                if (trieNode.subtree == null)
                    trieNode.subtree = new HashMap<>();

                TextualNode node = trieNode.subtree.get(keyword);
                if (node == null) {
                    trieNode.subtree.put(keyword, new QueryNode(query));
                    FAST.context.insertions++;
                    FAST.context.unboundedCounter4++;
                    FAST.context.unboundedCounter5++;
                    inserted = true;
                } else if (node instanceof QueryNode) {
                    if (((QueryNode) node).query.et > FAST.context.timestamp) {
                        QueryListNode newNode = new QueryListNode(((QueryNode) node).query);
                        newNode.queries.add(query);
                        trieNode.subtree.put(keyword, newNode);
                        FAST.context.nodeToList++;
                        FAST.context.insertions++;
                    } else {
                        throw new RuntimeException("Can't expire");
//                        deleteQueryFromStats(((QueryNode) node).query);
//                        trieNode.subtree.put(keyword, new QueryNode(query));
                    }
                    inserted = true;
                } else if (node instanceof QueryListNode &&
                        ((QueryListNode) node).queries.size() <= FAST.config.TRIE_SPLIT_THRESHOLD) {

                    ((QueryListNode) node).queries.add(query);
                    FAST.context.unboundedCounter3++;
                    FAST.context.insertions++;
                    inserted = true;
                } else if (node instanceof QueryListNode &&
                        ((QueryListNode) node).queries.size() > FAST.config.TRIE_SPLIT_THRESHOLD) {
                    QueryTrieNode newCell = new QueryTrieNode(level);
                    FAST.context.numberOfTries++;
                    FAST.context.unboundedCounter4++;
                    ((QueryListNode) node).queries.add(query);
                    newCell.subtree = new HashMap<>();
                    newCell.queries = new HybridList();
                    trieNode.subtree.put(keyword, newCell);
                    for (Query otherQuery : ((QueryListNode) node).queries) {
                        if (otherQuery.et > FAST.context.timestamp) {
                            if (otherQuery.keywords.size() > (j + 1)) {
                                QueryListNode otherCell = (QueryListNode) newCell.subtree.get(otherQuery.keywords.get(j + 1));
                                if (otherCell == null) {
                                    otherCell = new QueryListNode();
                                }
                                otherCell.queries.add(otherQuery);
                                newCell.subtree.put(otherQuery.keywords.get(j + 1), otherCell);
                                FAST.context.listToTrie++;
                                FAST.context.insertions++;
                            } else {
                                newCell.queries.add(otherQuery);
                                FAST.context.listToTrie++;
                                FAST.context.insertions++;
                            }
                        } else {
                            throw new RuntimeException("Can't expire");
//                            deleteQueryFromStats(otherQuery);
                        }
                    }
                    if (newCell.queries != null && newCell.queries.isEmpty())
                        newCell.queries = null;
                    inserted = true;
                } else if (node instanceof QueryTrieNode) {
                    if (j < (query.keywords.size() - 1)) {
                        trieNode = (QueryTrieNode) node;
                    } else {
                        if (((KNNQuery) query).ar != Double.MAX_VALUE && (level == 0 || checkSpanForForceInsertFinal(query))) {
                            if (((QueryTrieNode) node).finalQueries == null)
                                ((QueryTrieNode) node).finalQueries = new HybridList();
                            ((QueryTrieNode) node).finalQueries.add(query);
                            FAST.context.unboundedCounter4++;
                            FAST.context.finalQueries++;
                        } else if (FAST.config.INCREMENTAL_DESCENT && query instanceof KNNQuery &&
                                level == FAST.context.maxLevel && ((KNNQuery) query).ar == Double.MAX_VALUE &&
                                !FAST.config.LAZY_OBJ_SEARCH) {
                            if (((QueryTrieNode) node).unboundedQueries == null)
                                ((QueryTrieNode) node).unboundedQueries = new LinkedList<>();
                            ((QueryTrieNode) node).unboundedQueries.add((KNNQuery) query);
                            FAST.context.unboundedInserts++;
                            FAST.context.unboundedCounter4++;
                            FAST.context.insertions++;
                        } else {
                            if (((QueryTrieNode) node).queries == null)
                                ((QueryTrieNode) node).queries = new HybridList();

                            ((QueryTrieNode) node).queries.add(query);
                            FAST.context.unboundedCounter4++;
                            FAST.context.insertions++;

                            if (((QueryTrieNode) node).queries.mbrQueries().size() > ((QueryTrieNode) node).degRatio) {
                                findMBRQueriesToReinsert(((QueryTrieNode) node).queries.mbrQueries(), insertNextLevelQueries);
                            }

                            if (FAST.config.INCREMENTAL_DESCENT && level > 0 &&
                                    ((QueryTrieNode) node).queries.kNNQueries().size() > ((QueryTrieNode) node).knnDegRatio)
                                findKNNQueriesToReinsert(level, ((QueryTrieNode) node), insertNextLevelQueries);
                        }
                        inserted = true;
                    }
                }
            }
            if (!inserted) {
                if (((KNNQuery) query).ar != Double.MAX_VALUE && (level == 0 || checkSpanForForceInsertFinal(query))) {
                    if (trieNode.finalQueries == null)
                        trieNode.finalQueries = new HybridList();
                    trieNode.finalQueries.add(query);
                    FAST.context.finalQueries++;
                } else if (FAST.config.INCREMENTAL_DESCENT && query instanceof KNNQuery &&
                        level == FAST.context.maxLevel && ((KNNQuery) query).ar == Double.MAX_VALUE &&
                        !FAST.config.LAZY_OBJ_SEARCH) {
                    if (trieNode.unboundedQueries == null)
                        trieNode.unboundedQueries = new LinkedList<>();
                    trieNode.unboundedQueries.add((KNNQuery) query);
                    FAST.context.unboundedInserts++;
                    FAST.context.unboundedCounter4++;
                    FAST.context.insertions++;
                } else {
                    if (trieNode.queries == null)
                        trieNode.queries = new HybridList();

                    trieNode.queries.add(query);
                    FAST.context.unboundedCounter4++;
                    FAST.context.insertions++;

                    if (trieNode.queries.mbrQueries().size() > trieNode.degRatio)
                        findMBRQueriesToReinsert(trieNode.queries.mbrQueries(), insertNextLevelQueries);

                    if (FAST.config.INCREMENTAL_DESCENT && level > 0 &&
                            trieNode.queries.kNNQueries().size() > trieNode.knnDegRatio) {
                        findKNNQueriesToReinsert(level, trieNode, insertNextLevelQueries);
                    }
                }
            }
        }
        return null;
    }

    public void deleteQueryFromStats(Query query) {
        if (!query.deleted) {
            query.deleted = true;
            for (String keyword : query.keywords) {
                FAST.keywordFrequencyMap.get(keyword).queryCount--;
            }
        }
    }

    public boolean checkSpanForForceInsertFinal(Query query) {
        if (query instanceof MinimalRangeQuery) {
            double queryRange = ((MinimalRangeQuery) query).spatialRange.max.x - ((MinimalRangeQuery) query).spatialRange.min.x;
            double cellRange = bounds.max.x - bounds.min.x;
            return queryRange > (cellRange / 2);
        } else if (query instanceof KNNQuery) {
            double queryRange = ((KNNQuery) query).ar;
            double cellRange = bounds.max.x - bounds.min.x;
            return queryRange > (cellRange * FAST.config.KNN_OMEGA);
        }
            // TODO - KNN - Can skip for now
        return false;
    }

    public boolean insertAtKeyWord(String keyword, Query query, QueryListNode sharedQueries) {
        if (!textualIndex.containsKey(keyword)) {
            FAST.context.numberOfHashEntries++;
            FAST.context.queryInsertInvListNodeCounter++;

            textualIndex.put(keyword, new QueryNode(query));
            FAST.context.insertions++;
            return true;
        } else { //this keyword already exists in the index
            TextualNode node = textualIndex.get(keyword);
            if (node == null) {
                FAST.context.queryInsertInvListNodeCounter++;
                textualIndex.put(keyword, new QueryNode(query));
                FAST.context.insertions++;
                return true;
            }
            if (node instanceof QueryNode) { //single query
                Query exitingQuery = ((QueryNode) node).query;
                if (exitingQuery.et > FAST.context.timestamp) { //checking for the support of the query
                    QueryListNode rareQueries = new QueryListNode(exitingQuery);
                    FAST.context.queryInsertInvListNodeCounter++;
                    rareQueries.queries.add(query);
                    textualIndex.put(keyword, rareQueries);
                    FAST.context.insertions++;
                } else {
                    throw new RuntimeException("Queries shouldn't expire");
//                    deleteQueryFromStats(exitingQuery);
//                    textualIndex.put(keyword, new QueryNode(query));
                }
                return true;
            } else if ((node instanceof QueryListNode) &&
                    ((QueryListNode) node).queries.size() < FAST.config.TRIE_SPLIT_THRESHOLD) { // this keyword is rare
                if ((node) != sharedQueries)
                    if (!((QueryListNode) node).queries.contains(query)) {
                        ((QueryListNode) node).queries.add(query);
                        FAST.context.queryInsertInvListNodeCounter++;
                        FAST.context.insertions++;
                    }
                return true;
            } else if ((node instanceof QueryListNode) &&
                    ((QueryListNode) node).queries.size() >= FAST.config.TRIE_SPLIT_THRESHOLD) { // this keyword is not rare
                if (node != sharedQueries) {
                    return ((QueryListNode) node).queries.contains(query);
                } else {
                    return true;
                }
            } else if (node instanceof QueryTrieNode) {
                return false;
            } else {
                RunCkQST.logger.error("This is an error you should never be here");
            }
        }
        return false;
    }

    public String getOtherKeywordToInsert(Query query) {
        int minSize = Integer.MAX_VALUE;
        String minKeyword = null;
        for (String term : query.keywords) {
            if (!textualIndex.containsKey(term)) {
                int size = 0;
                if (size < minSize) {
                    minSize = size;
                    minKeyword = term;
                }
            } else if (textualIndex.containsKey(term) && textualIndex.get(term) instanceof QueryNode) {
                int size = 1;
                if (size < minSize) {
                    minSize = size;
                    minKeyword = term;
                }
            } else if (textualIndex.containsKey(term) && textualIndex.get(term) instanceof QueryListNode) {
                int size = ((QueryListNode) textualIndex.get(term)).queries.size();
                if (size < minSize) {
                    minSize = size;
                    minKeyword = term;
                }
            }
        }
        return minKeyword;
    }

    public List<String> searchQueries(DataObject obj, List<String> keywords, List<Query> results,
                                      List<ReinsertEntry> descendingKNNQueries, boolean isExpiry) {
        ArrayList<String> remainingKeywords = new ArrayList<>();
//        if (obj.id == 3657 + 10000) {
//            System.out.println("Debug!");
//        }
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            TextualNode node = textualIndex.get(keyword);
            FAST.context.objectSearchInvListHashAccess++;
            if (node != null) {
                if (node instanceof QueryNode) {
                    if (((QueryNode) node).query instanceof MinimalRangeQuery) {
                        MinimalRangeQuery query = (MinimalRangeQuery) ((QueryNode) node).query;
                        if (query.et > FAST.context.timestamp && keywords.size() >= query.keywords.size() &&
                                SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) &&
                                TextHelpers.containsTextually(keywords, query.keywords))
                            results.add(query);
                    } else if (((QueryNode) node).query instanceof KNNQuery) {
                        KNNQuery query = (KNNQuery) ((QueryNode) node).query;
                        if (keywords.size() >= query.keywords.size() && (query.et > FAST.context.timestamp || isExpiry) &&
                                (FAST.config.INCREMENTAL_DESCENT || query.currentLevel == level) &&
                                SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) &&
                                TextHelpers.containsTextually(keywords, query.keywords)) {
                            results.add(query);
                            if (!isExpiry) {
                                query.pushUntilKHat(obj);
                            }
                        }
                    }
                } else if (node instanceof QueryListNode) {
                    for (MinimalRangeQuery query : ((QueryListNode) node).queries.mbrQueries()) {
                        FAST.context.objectSearchInvListNodeCounter++;
                        if (query.et > FAST.context.timestamp && SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) &&
                                TextHelpers.containsTextually(keywords, query.keywords))
                            results.add(query);
                    }
                    for (KNNQuery query : ((QueryListNode) node).queries.kNNQueries()) {
                        FAST.context.objectSearchInvListNodeCounter++;
                        if ((query.et > FAST.context.timestamp || isExpiry) && (FAST.config.INCREMENTAL_DESCENT || query.currentLevel == level) &&
                                SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) &&
                                TextHelpers.containsTextually(keywords, query.keywords)) {
                            results.add(query);
                            if (!isExpiry) {
                                query.pushUntilKHat(obj);
                            }
                        }
                    }
                } else if (node instanceof QueryTrieNode) {
                    remainingKeywords.add(keyword);
                }
            }
        }

        for (int i = 0; i < remainingKeywords.size(); i++) {
            String keyword = remainingKeywords.get(i);
            Object keyWordIndex = textualIndex.get(keyword);
            FAST.context.totalTrieAccess++;
            ((QueryTrieNode) keyWordIndex).find(this, obj, remainingKeywords, i + 1, results, descendingKNNQueries, isExpiry);
        }
        return remainingKeywords;
    }

    public void findMBRQueriesToReinsert(List<MinimalRangeQuery> queries, ArrayList<ReinsertEntry> insertNextLevelQueries) {
        SpatialOverlapComparator spatialOverlapComparator = new SpatialOverlapComparator(bounds);
        queries.sort(spatialOverlapComparator);
        int queriesSize = queries.size();
        for (int i = queriesSize - 1; i > queriesSize / 2; i--) {
            MinimalRangeQuery query = queries.remove(i);
            query.descended++;
            FAST.context.totalDescendOpts++;
            insertNextLevelQueries.add(new ReinsertEntry(SpatialHelper.spatialIntersect(bounds, query.spatialRange), query));
        }
    }

    public void findKNNQueriesToReinsert(
            int fromLevel,
            QueryTrieNode node,
//            List<KNNQuery> queries,
            ArrayList<ReinsertEntry> insertNextLevelQueries
    ) {
        List<KNNQuery> queries = node.queries.kNNQueries();
        // TODO: if ar = inf -> Query object index and determine size
        // TODO: but why? remove it
//        queries.iterator();
//        Iterator<KNNQuery> it = queries.iterator();
//        HashMap<Integer, Collection<DataObject>> kthObjects = new HashMap<>();
//        while(it.hasNext()) {
//            KNNQuery query = it.next();
//            if (query.ar >= Double.MAX_VALUE) {
//                PriorityQueue<DataObject> objResults = FAST.context.objectSearcher.apply(query);
//
//                if (objResults.size() >= query.k) {
////                    kthObjects.put(query.k, objResults);
//                    DataObject o = objResults.peek();
//                    assert o != null;
//                    query.ar = SpatialHelper.getDistanceInBetween(query.location, o.location);
//                }
//
//                // Unbounded after object search
//                if (query.ar >= Double.MAX_VALUE) {
//                    if (node.unboundedQueries == null)
//                        node.unboundedQueries = new LinkedList<>();
//                    node.unboundedQueries.add(query);
//                    it.remove();
//                }
//            }
//        }

        SpatialOverlapComparator spatialOverlapComparator = new SpatialOverlapComparator(bounds);
        queries.sort(spatialOverlapComparator);
        int queriesSize = queries.size();
        for (int i = queriesSize - 1; i > queriesSize / 2; i--) {
            if (queries.get(i).ar >= Double.MAX_VALUE)
                continue;

            KNNQuery query = queries.remove(i);
            FAST.context.removed++;
            query.descended++;
            FAST.context.totalDescendOpts++;
            insertNextLevelQueries.add(new ReinsertEntry(SpatialHelper.spatialIntersect(bounds, query.spatialBox()), query));
        }

        if (FAST.config.ADAPTIVE_DEG_RATIO) {
            node.knnDegRatio = Math.min(FAST.config.KNN_DEGRADATION_RATIO, 2 * node.knnDegRatio);
        }
    }

    public boolean clean() {
        if (cleaningIterator == null || !cleaningIterator.hasNext())
            cleaningIterator = textualIndex.entrySet().iterator();
        int numberOfVisitedEntries = 0;
        while (cleaningIterator.hasNext() && numberOfVisitedEntries < FAST.config.MAX_ENTRIES_PER_CLEANING_INTERVAL) {
            Entry<String, TextualNode> nextNode = cleaningIterator.next();
            TextualNode node = nextNode.getValue();
            String keyword = nextNode.getKey();
            if (node instanceof QueryNode) {
                numberOfVisitedEntries++;
                Query q = ((QueryNode) node).query;
                if (q.et < FAST.context.timestamp || (FAST.config.CLEAN_METHOD == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
                        (((KNNQuery) q).currentLevel != level || !SpatialHelper.overlapsSpatially(bounds, ((KNNQuery) q).location, ((KNNQuery) q).ar)))) {
                    node = null;
                }
            } else if (node instanceof QueryListNode) {
                Iterator<Query> queriesIterator = ((QueryListNode) node).queries.iterator();
                while (queriesIterator.hasNext()) {
                    Query q = queriesIterator.next();
                    if (q.et < FAST.context.timestamp || (FAST.config.CLEAN_METHOD == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
                            (((KNNQuery) q).currentLevel != level || !SpatialHelper.overlapsSpatially(bounds, ((KNNQuery) q).location, ((KNNQuery) q).ar))))
                        queriesIterator.remove();
                    numberOfVisitedEntries++;
                }
                if (((QueryListNode) node).queries.isEmpty())
                    node = null;
                else if (((QueryListNode) node).queries.size() == 1) {
                    Query singleQuery = null;
                    if (!((QueryListNode) node).queries.mbrQueries().isEmpty())
                        singleQuery = ((QueryListNode) node).queries.mbrQueries().get(0);
                    if (!((QueryListNode) node).queries.kNNQueries().isEmpty())
                        singleQuery = ((QueryListNode) node).queries.kNNQueries().get(0);
                    textualIndex.put(keyword, new QueryNode(singleQuery));
                }
            } else if (node instanceof QueryTrieNode) {
                QueryListNode combinedQueries = new QueryListNode();
                numberOfVisitedEntries += ((QueryTrieNode) node).clean(level, bounds, combinedQueries);
                // System.out.println(FAST.keywordFrequencyMap);
                // System.out.println(FAST.keywordFrequencyMap.get(keyword));
                if (((QueryTrieNode) node).queries == null && ((QueryTrieNode) node).subtree == null
                        && FAST.keywordFrequencyMap.get(keyword).queryCount <= FAST.config.TRIE_OVERALL_MERGE_THRESHOLD)
                    node = null;
                else if (combinedQueries.queries.size() < FAST.config.TRIE_OVERALL_MERGE_THRESHOLD
                        && FAST.keywordFrequencyMap.get(keyword).queryCount <= FAST.config.TRIE_OVERALL_MERGE_THRESHOLD)
                    textualIndex.put(keyword, combinedQueries);
            }
            if (node == null)
                cleaningIterator.remove();
        }
        return !cleaningIterator.hasNext();
    }

    static class SpatialOverlapComparator implements Comparator<Query> {
        private final Rectangle bounds;

        public SpatialOverlapComparator(Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        public int compare(Query e1, Query e2) {
            double val1 = SpatialHelper.getArea(e1.spatialBox()); //SpatialHelper.spatialIntersect(bounds, e1.spatialBox()));
            double val2 = SpatialHelper.getArea(e2.spatialBox()); //SpatialHelper.spatialIntersect(bounds, e2.spatialBox()));
            return Double.compare(val2, val1);
        }
    }
}
