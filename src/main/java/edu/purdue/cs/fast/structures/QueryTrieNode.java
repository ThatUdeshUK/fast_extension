package edu.purdue.cs.fast.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;

public class QueryTrieNode extends TextualNode {
    public HashMap<String, TextualNode> subtree;
    public HybridList queries;
    public ArrayList<Query> finalQueries;

    public QueryTrieNode() {
    }

    public void find(DataObject obj, ArrayList<String> keywords, int start, List<Query> results,
                     ArrayList<KNNQuery> descendingKNNQueries) {
        FAST.objectSearchTrieNodeCounter++;

        if (finalQueries != null)
            for (Query q : finalQueries) {
                FAST.objectSearchTrieFinalNodeCounter++;
                searchQueries(obj, results, q);
            }

        if (queries != null) {
            for (MinimalRangeQuery q : queries.mbrQueries()) {
                FAST.objectSearchTrieNodeCounter++;
                searchQueries(obj, results, q);
            }
            for (Iterator<KNNQuery> it = queries.kNNQueries().iterator(); it.hasNext(); ) {
                KNNQuery q = it.next();
                FAST.objectSearchTrieNodeCounter++;
                boolean kFilled = searchQueries(obj, results, q);
                if (kFilled && descendingKNNQueries != null) {
                    descendingKNNQueries.add(q);
                    it.remove();
                }
            }
        }

        if (subtree != null)
            for (int i = start; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                TextualNode node = subtree.get(keyword);
                if (node == null)
                    continue;

                FAST.objectSearchTrieHashAccess++;
                if (node instanceof QueryNode) {
                    FAST.objectSearchTrieNodeCounter++;
                    if (((QueryNode) node).query instanceof MinimalRangeQuery) {
                        MinimalRangeQuery query = (MinimalRangeQuery) ((QueryNode) node).query;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > FAST.timestamp)
                            results.add(query);
                    }
                    if (((QueryNode) node).query instanceof KNNQuery) {
                        KNNQuery query = (KNNQuery) ((QueryNode) node).query;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > FAST.timestamp) {
                            results.add(query);
                            if (query.pushUntilK(obj) && descendingKNNQueries != null) {
                                descendingKNNQueries.add(query);
                                subtree.put(keyword, null);
                            }
                        }
                    }
                } else if (node instanceof QueryListNode) {
                    for (MinimalRangeQuery query : ((QueryListNode) node).queries.mbrQueries()) {
                        FAST.objectSearchTrieNodeCounter++;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.spatialRange)
                                && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > FAST.timestamp)
                            results.add(query);
                    }
                    ArrayList<KNNQuery> toRemove = new ArrayList<>();
                    for (KNNQuery query : ((QueryListNode) node).queries.kNNQueries()) {
                        FAST.objectSearchTrieNodeCounter++;
                        if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) && TextHelpers.containsTextually(keywords, query.keywords) &&
                                query.et > FAST.timestamp) {
                            results.add(query);
                            if (query.pushUntilK(obj) && descendingKNNQueries != null) {
                                descendingKNNQueries.add(query);
                                toRemove.add(query);
                            }
                        }
                    }
                    toRemove.forEach((KNNQuery query) -> ((QueryListNode) node).queries.remove(query));
                } else if (node instanceof QueryTrieNode) {
                    ((QueryTrieNode) node).find(obj, keywords, i + 1, results, descendingKNNQueries);
                }
            }
    }

    private boolean searchQueries(DataObject obj, List<Query> results, Query q) {
        if (q instanceof MinimalRangeQuery && SpatialHelper.overlapsSpatially(obj.location, ((MinimalRangeQuery) q).spatialRange)) {
            results.add(q);
        }
        if (q instanceof KNNQuery query) {
            if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) && query.et > FAST.timestamp) {
                results.add(query);
                return query.pushUntilK(obj);
            }
        }
        return false;
    }

    public int clean(QueryListNode combinedQueries) {
        int operations = 0;
        if (queries != null) {
            Iterator<Query> queriesItr = queries.iterator();
            while (queriesItr.hasNext()) {
                Query query = queriesItr.next();
                if (query.et <= FAST.timestamp)
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
                    if (query.et <= FAST.timestamp)
                        trieCellsItr.remove();
                    else {
                        combinedQueries.queries.add(query);
                    }
                    operations++;
                } else if (node instanceof QueryListNode) {
                    Iterator<Query> queriesInternalItr = ((QueryListNode) node).queries.iterator();
                    while (queriesInternalItr.hasNext()) {
                        Query query = queriesInternalItr.next();
                        if (query.et <= FAST.timestamp)
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
