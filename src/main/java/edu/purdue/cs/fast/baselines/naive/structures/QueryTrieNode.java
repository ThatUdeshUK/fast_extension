package edu.purdue.cs.fast.baselines.naive.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.baselines.naive.models.NaiveKNNQuery;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.structures.QueryNode;
import edu.purdue.cs.fast.structures.TextualNode;
import edu.purdue.cs.fast.models.*;

public class QueryTrieNode extends TextualNode {
    public HashMap<String, TextualNode> subtree;
    public HybridList queries;
    public ArrayList<Query> finalQueries;

    public QueryTrieNode() {
    }

    public void find(DataObject obj, ArrayList<String> keywords, int start, List<Query> results) {
        NaiveFAST.objectSearchTrieNodeCounter++;

        if (finalQueries != null)
            for (Query q : finalQueries) {
                NaiveFAST.objectSearchTrieFinalNodeCounter++;
                searchQueries(obj, results, q);
            }

        if (queries != null)
            for (Query q : queries.allQueries()) {
                NaiveFAST.objectSearchTrieNodeCounter++;
                searchQueries(obj, results, q);
            }

        if (subtree != null)
            for (int i = start; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                TextualNode node = subtree.get(keyword);
                if (node == null)
                    continue;

                NaiveFAST.objectSearchTrieHashAccess++;
                if (node instanceof QueryNode) {
                    NaiveFAST.objectSearchTrieNodeCounter++;
                    if (((QueryNode) node).query instanceof MinimalRangeQuery) {
                        MinimalRangeQuery query = (MinimalRangeQuery) ((QueryNode) node).query;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > NaiveFAST.timestamp)
                            results.add(query);
                    }
                    if (((QueryNode) node).query instanceof NaiveKNNQuery) {
                        NaiveKNNQuery query = (NaiveKNNQuery) ((QueryNode) node).query;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar)  && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > NaiveFAST.timestamp) {
                            query.pushWithLimitK(obj);
                            if (query.monitoredObjects.contains(obj))
                                results.add(query);
                        }
                    }
                } else if (node instanceof QueryListNode) {
                    for (MinimalRangeQuery query : ((QueryListNode) node).queries.mbrQueries()) {
                        NaiveFAST.objectSearchTrieNodeCounter++;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.spatialRange)
                                && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > NaiveFAST.timestamp)
                            results.add(query);
                    }
                    for (NaiveKNNQuery query : ((QueryListNode) node).queries.kNNQueries()) {
                        NaiveFAST.objectSearchTrieNodeCounter++;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar)  && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > NaiveFAST.timestamp) {
                            query.pushWithLimitK(obj);
                            if (query.monitoredObjects.contains(obj))
                                results.add(query);
                        }
                    }
                } else if (node instanceof QueryTrieNode) {
                    ((QueryTrieNode) node).find(obj, keywords, i + 1, results);
                }
            }
    }

    private void searchQueries(DataObject obj, List<Query> results, Query q) {
        if (q instanceof MinimalRangeQuery && SpatialHelper.overlapsSpatially(obj.location, ((MinimalRangeQuery) q).spatialRange)) {
            results.add(q);
        }
        if (q instanceof NaiveKNNQuery query) {
            if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) &&
                    query.et > NaiveFAST.timestamp) {
                query.pushWithLimitK(obj);
                if (query.monitoredObjects.contains(obj))
                    results.add(query);
            }
        }
    }

    public int clean(QueryListNode combinedQueries) {
        int operations = 0;
        if (queries != null) {
            Iterator<Query> queriesItr = queries.allQueries().iterator();
            while (queriesItr.hasNext()) {
                Query query = queriesItr.next();
                if (query.et < NaiveFAST.timestamp)
                    queriesItr.remove();
                else {
                    combinedQueries.queries.add(query);
                }
                operations++;
            }
            if (queries.isEmpty())
                queries = null;
        }
        if (subtree != null) {
            Iterator<Entry<String, TextualNode>> trieCellsItr = subtree.entrySet().iterator();
            while (trieCellsItr.hasNext()) {
                TextualNode node = trieCellsItr.next().getValue();
                if (node instanceof QueryNode) {
                    MinimalRangeQuery query = (MinimalRangeQuery) ((QueryNode) node).query;
                    if (query.et < NaiveFAST.timestamp)
                        trieCellsItr.remove();
                    else {
                        combinedQueries.queries.add(query);
                    }
                    operations++;
                } else if (node instanceof QueryListNode) {
                    Iterator<Query> queriesInternalItr = ((QueryListNode) node).queries.allQueries().iterator();
                    while (queriesInternalItr.hasNext()) {
                        Query query = queriesInternalItr.next();
                        if (query.et < NaiveFAST.timestamp)
                            queriesInternalItr.remove();
                        else {
                            combinedQueries.queries.add(query);
                        }
                        operations++;
                    }
                    if (((QueryListNode) node).queries.isEmpty())
                        trieCellsItr.remove();
                } else if (node instanceof QueryTrieNode) {
                    operations += ((QueryTrieNode) node).clean(combinedQueries);
                    if (((QueryTrieNode) node).queries == null && ((QueryTrieNode) node).subtree == null)
                        trieCellsItr.remove();
                }
            }
            if (subtree.isEmpty())
                subtree = null;
        }

        return operations;
    }
}
