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
package edu.purdue.cs.fast.baselines.naive.structures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.baselines.naive.models.NaiveKNNQuery;
import edu.purdue.cs.fast.baselines.naive.models.ReinsertEntry;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.structures.*;

public class SpatialCell {
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

    public void addInternalQuery(String keyword, MinimalRangeQuery query,
                                 QueryListNode sharedQueries, ArrayList<ReinsertEntry> insertNextLevelQueries) {
        if (textualIndex == null) {
            textualIndex = new ConcurrentHashMap<>();
        }

        if (!textualIndex.containsKey(keyword) && sharedQueries != null) {
            NaiveFAST.numberOfHashEntries++;
            textualIndex.put(keyword, sharedQueries);
        } else {
            if (sharedQueries == null)
                return;

            TextualNode node = textualIndex.get(keyword);
            if (node instanceof QueryNode) {
                Query exitingQuery = ((QueryNode) node).query;
                if (exitingQuery.et > NaiveFAST.timestamp) { //checking for the support of the query
                    if (sharedQueries.queries.contains(exitingQuery)) {
                        textualIndex.put(keyword, sharedQueries);
                    } else if (sharedQueries.queries.mbrQueries().size() < NaiveFAST.Trie_SPLIT_THRESHOLD) {
                        NaiveFAST.queryInsertInvListNodeCounter++;
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
                //already inserted do nothing;
                ArrayList<Query> nonSharedQueries = new ArrayList<>();
                for (Query q : ((QueryListNode) node).queries.allQueries()) {
                    if (!sharedQueries.queries.contains(q))
                        nonSharedQueries.add(q);
                }
                if (!nonSharedQueries.isEmpty() && nonSharedQueries.size() + sharedQueries.queries.size() <= NaiveFAST.Trie_SPLIT_THRESHOLD) {
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

    public void deleteQueryFromStats(Query query) {
        if (!query.deleted) {
            query.deleted = true;
            for (String keyword : query.keywords) {
                NaiveFAST.keywordFrequencyMap.get(keyword).queryCount--;
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
        if (inserted)
            if (textualIndex.get(keyword) instanceof QueryListNode)
                return (QueryListNode) textualIndex.get(keyword);
            else
                return null;
        else {
            queue.add(query);
        }

        while (!queue.isEmpty()) {
            query = queue.remove();
            inserted = false;

            String otherKeyword = getOtherKeywordToInsert(query);
            if (otherKeyword != null) {
                inserted = insertAtKeyWord(otherKeyword, query, null);
            }
            if (inserted)
                continue;
            else {
                for (String term : query.keywords) {
                    //mark all these keywords as tries
                    if (textualIndex.get(term) instanceof QueryListNode) {
                        queue.addAll(((QueryListNode) textualIndex.get(term)).queries.mbrQueries());
                        queue.addAll(((QueryListNode) textualIndex.get(term)).queries.kNNQueries());
                        textualIndex.put(term, new QueryTrieNode());
                        NaiveFAST.numberOfTrieNodes++;
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
                    inserted = true;
                } else if (node instanceof QueryNode) {
                    if (((QueryNode) node).query.et > NaiveFAST.timestamp) {
                        QueryListNode newNode = new QueryListNode(((QueryNode) node).query);
                        newNode.queries.add(query);
                        trieNode.subtree.put(keyword, newNode);
                    } else {
                        deleteQueryFromStats(((QueryNode) node).query);
                        trieNode.subtree.put(keyword, new QueryNode(query));
                    }
                    inserted = true;
                } else if (node instanceof QueryListNode && ((QueryListNode) node).queries.size() <= NaiveFAST.Trie_SPLIT_THRESHOLD) {
                    ((QueryListNode) node).queries.add(query);
                    inserted = true;
                } else if (node instanceof QueryListNode && ((QueryListNode) node).queries.size() > NaiveFAST.Trie_SPLIT_THRESHOLD) {
                    QueryTrieNode newCell = new QueryTrieNode();
                    NaiveFAST.numberOfTrieNodes++;
                    ((QueryListNode) node).queries.add(query);
                    newCell.subtree = new HashMap<>();
                    newCell.queries = new HybridList();
                    trieNode.subtree.put(keyword, newCell);
                    for (Query otherQuery : ((QueryListNode) node).queries.allQueries()) {
                        if (otherQuery.et > NaiveFAST.timestamp) {
                            if (otherQuery.keywords.size() > (j + 1)) {

                                QueryListNode otherCell = (QueryListNode) newCell.subtree.get(otherQuery.keywords.get(j + 1));
                                if (otherCell == null) {
                                    otherCell = new QueryListNode();
                                }
                                otherCell.queries.add(otherQuery);
                                newCell.subtree.put(otherQuery.keywords.get(j + 1), otherCell);
                            } else {
                                newCell.queries.add(otherQuery);
                            }
                        } else {
                            deleteQueryFromStats(otherQuery);
                        }
                    }
                    if (newCell.queries != null && newCell.queries.isEmpty())
                        newCell.queries = null;
                    inserted = true;
                } else if (node instanceof QueryTrieNode) {
                    if (j < (query.keywords.size() - 1)) {
                        trieNode = (QueryTrieNode) node;
                    } else {
                        if (level == 0 || checkSpanForForceInsertFinal(query)) {
                            if (((QueryTrieNode) node).finalQueries == null)
                                ((QueryTrieNode) node).finalQueries = new ArrayList<>();
                            ((QueryTrieNode) node).finalQueries.add(query);
                        } else {

                            if (((QueryTrieNode) node).queries == null)
                                ((QueryTrieNode) node).queries = new HybridList();
                            ((QueryTrieNode) node).queries.add(query);
                            if (((QueryTrieNode) node).queries.mbrQueries().size() > NaiveFAST.Degradation_Ratio) {
                                findQueriesToReinsert(((QueryTrieNode) node).queries.mbrQueries(), insertNextLevelQueries);
                            }
                        }
                        inserted = true;
                    }
                }
            }
            if (!inserted) {
                if ((level == 0 || checkSpanForForceInsertFinal(query))) {
                    if (trieNode.finalQueries == null)
                        trieNode.finalQueries = new ArrayList<>();
                    trieNode.finalQueries.add(query);

                } else {
                    if (trieNode.queries == null)
                        trieNode.queries = new HybridList();
                    trieNode.queries.add(query);
                    if (trieNode.queries.mbrQueries().size() > NaiveFAST.Degradation_Ratio)
                        findQueriesToReinsert(trieNode.queries.mbrQueries(), insertNextLevelQueries);
                }
            }
        }
        return null;
    }

    public boolean checkSpanForForceInsertFinal(Query query) {
        if (query instanceof MinimalRangeQuery) {
            double queryRange = ((MinimalRangeQuery) query).spatialRange.max.x - ((MinimalRangeQuery) query).spatialRange.min.x;
            double cellRange = bounds.max.x - bounds.min.x;
            return queryRange > (cellRange / 2);
        }
        // TODO - KNN - Can skip for now
        return false;
    }

    public boolean insertAtKeyWord(String keyword, Query query, QueryListNode sharedQueries) {
        if (!textualIndex.containsKey(keyword)) {
            NaiveFAST.numberOfHashEntries++;
            NaiveFAST.queryInsertInvListNodeCounter++;
            textualIndex.put(keyword, new QueryNode(query));
            return true;
        } else { //this keyword already exists in the index
            TextualNode node = textualIndex.get(keyword);
            if (node == null) {
                NaiveFAST.queryInsertInvListNodeCounter++;
                textualIndex.put(keyword, new QueryNode(query));
                return true;
            }
            if (node instanceof QueryNode) { //single query
                Query exitingQuery = ((QueryNode) node).query;
                if (exitingQuery.et > NaiveFAST.timestamp) { //checking for the support of the query
                    QueryListNode rareQueries = new QueryListNode(exitingQuery);
                    NaiveFAST.queryInsertInvListNodeCounter++;
                    rareQueries.queries.add(query);
                    textualIndex.put(keyword, rareQueries);
                } else {
                    deleteQueryFromStats(exitingQuery);
                    textualIndex.put(keyword, new QueryNode(query));
                }
                return true;
            } else if ((node instanceof QueryListNode) && ((QueryListNode) node).queries.size() < NaiveFAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
                if ((node) != sharedQueries)
                    if (!((QueryListNode) node).queries.contains(query)) {
                        ((QueryListNode) node).queries.add(query);
                        NaiveFAST.queryInsertInvListNodeCounter++;
                    }
                return true;
            } else if ((node instanceof QueryListNode) && ((QueryListNode) node).queries.size() >= NaiveFAST.Trie_SPLIT_THRESHOLD) { // this keyword is not rare
                if (node != sharedQueries) {
                    return ((QueryListNode) node).queries.contains(query);
                } else {
                    return true;
                }
            } else if (node instanceof QueryTrieNode) {
                return false;
            } else {
                System.err.println("This is an error you should never be here");
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

    public void findQueriesToReinsert(List<MinimalRangeQuery> queries, ArrayList<ReinsertEntry> insertNextLevelQueries) {
        SpatialOverlapComparator spatialOverlapComparator = new SpatialOverlapComparator(bounds);
        queries.sort(spatialOverlapComparator);
        int queriesSize = queries.size();
        for (int i = queriesSize - 1; i > queriesSize / 2; i--) {
            MinimalRangeQuery query = queries.remove(i);
            insertNextLevelQueries.add(new ReinsertEntry(SpatialHelper.spatialIntersect(bounds, query.spatialRange), query));
        }
    }

    public boolean clean() {
        if (cleaningIterator == null || !cleaningIterator.hasNext())
            cleaningIterator = textualIndex.entrySet().iterator();
        int numberOfVisitedEntries = 0;
        while (cleaningIterator.hasNext() && numberOfVisitedEntries < NaiveFAST.MAX_ENTRIES_PER_CLEANING_INTERVAL) {
            Entry<String, TextualNode> nextNode = cleaningIterator.next();
            TextualNode node = nextNode.getValue();
            String keyword = nextNode.getKey();
            if (node instanceof QueryNode) {
                numberOfVisitedEntries++;
                if (((QueryNode) node).query.et < NaiveFAST.timestamp)
                    node = null;
            } else if (node instanceof QueryListNode) {
                Iterator<Query> queriesIterator = ((QueryListNode) node).queries.allQueries().iterator();
                while (queriesIterator.hasNext()) {
                    Query query = queriesIterator.next();
                    if (query.et < NaiveFAST.timestamp)
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
                numberOfVisitedEntries += ((QueryTrieNode) node).clean(combinedQueries);
                if (((QueryTrieNode) node).queries == null && ((QueryTrieNode) node).subtree == null
                        && NaiveFAST.keywordFrequencyMap.get(keyword).queryCount <= NaiveFAST.Trie_OVERLALL_MERGE_THRESHOLD)
                    node = null;
                else if (combinedQueries.queries.size() < NaiveFAST.Trie_OVERLALL_MERGE_THRESHOLD
                        && NaiveFAST.keywordFrequencyMap.get(keyword).queryCount <= NaiveFAST.Trie_OVERLALL_MERGE_THRESHOLD)
                    textualIndex.put(keyword, combinedQueries);
            }
            if (node == null)
                cleaningIterator.remove();
        }
        return !cleaningIterator.hasNext();
    }

    public ArrayList<String> searchQueries(DataObject obj, List<String> keywords, List<Query> finalQueries) {
        if (obj.id == 73) {
            System.out.println(keywords);
        }
        ArrayList<String> remainingKeywords = new ArrayList<>();
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            TextualNode node = textualIndex.get(keyword);
            NaiveFAST.objectSearchInvListHashAccess++;
            if (node != null) {
                if (node instanceof QueryNode) {
                    if (((QueryNode) node).query instanceof MinimalRangeQuery) {
                        MinimalRangeQuery query = (MinimalRangeQuery) ((QueryNode) node).query;
                        if (keywords.size() >= query.keywords.size() && SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > NaiveFAST.timestamp)
                            finalQueries.add(query);
                    } else if (((QueryNode) node).query instanceof NaiveKNNQuery) {
                        NaiveKNNQuery query = (NaiveKNNQuery) ((QueryNode) node).query;
                        if (keywords.size() >= query.keywords.size() && SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > NaiveFAST.timestamp) {
                            query.pushWithLimitK(obj);
                            if (query.monitoredObjects.contains(obj))
                                finalQueries.add(query);
                        }
                    }
                } else if (node instanceof QueryListNode) {
                    List<Query> rareQueries = ((QueryListNode) node).queries.allQueries();
                    for (Query q : rareQueries) {
                        NaiveFAST.objectSearchInvListNodeCounter++;
                        if (q instanceof MinimalRangeQuery) {
                            MinimalRangeQuery query = ((MinimalRangeQuery) q);
                            if (SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                    query.et > NaiveFAST.timestamp)
                                finalQueries.add(q);
                        } else if (q instanceof NaiveKNNQuery) {
                            NaiveKNNQuery query = ((NaiveKNNQuery) q);
                            if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                    query.et > NaiveFAST.timestamp) {
                                query.pushWithLimitK(obj);
                                if (query.monitoredObjects.contains(obj))
                                    finalQueries.add(query);
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
            NaiveFAST.totalTrieAccess++;
            ((QueryTrieNode) keyWordIndex).find(obj, remainingKeywords, i + 1, finalQueries);
        }

        return remainingKeywords;
    }

    public static Rectangle getBounds(int i, int j, double step) {
        return new Rectangle(new Point(i * step, j * step), new Point((i + 1) * step, (j + 1) * step));
    }

    static class SpatialOverlapComparator implements Comparator<MinimalRangeQuery> {
        private final Rectangle bounds;

        public SpatialOverlapComparator(Rectangle bounds) {
            this.bounds = bounds;
        }

        @Override
        public int compare(MinimalRangeQuery e1, MinimalRangeQuery e2) {
            double val1 = SpatialHelper.getArea(SpatialHelper.spatialIntersect(bounds, e1.spatialRange));
            double val2 = SpatialHelper.getArea(SpatialHelper.spatialIntersect(bounds, e2.spatialRange));
            return Double.compare(val2, val1);
        }
    }
}