package edu.purdue.cs.fast.structures;

import java.util.*;
import java.util.Map.Entry;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.helper.CleanMethod;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;

public class QueryTrieNode extends TextualNode {
    public HashMap<String, TextualNode> subtree;
    public HybridList queries;
    public ArrayList<Query> finalQueries;

    public QueryTrieNode() {
    }

    public void find(SpatialCell parent, DataObject obj, ArrayList<String> keywords, int start, List<Query> results,
                     List<KNNQuery> descendingKNNQueries) {
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
            ArrayList<KNNQuery> toRemove = new ArrayList<>();
            for (KNNQuery q : queries.kNNQueries()) {
                FAST.objectSearchTrieNodeCounter++;
                if (q.et <= FAST.timestamp || q.currentLevel != parent.level)
                    continue;
//            for (Iterator<KNNQuery> it = queries.kNNQueries().iterator(); it.hasNext(); ) {
//                KNNQuery q = it.next();
                boolean kFilled = searchQueries(obj, results, q);
                if (descendingKNNQueries != null && parent.level == FAST.maxLevel && kFilled) {
                    descendingKNNQueries.add(q);
//                    it.remove();
                    toRemove.add(q);
                }
//                else if (descendingKNNQueries != null && parent.level != FAST.maxLevel && parent.level != 0 &&
//                        queries.kNNQueries().size() > 100 &&
//                        SpatialHelper.coversSpatially(parent.bounds, q.spatialBox())) {
//                    descendingKNNQueries.add(q);
//                    toRemove.add(q);
//                }
            }
            toRemove.forEach((query -> queries.kNNQueries().remove(query)));
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
                        if (query.et > FAST.timestamp && SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) &&
                                TextHelpers.containsTextually(keywords, query.keywords))
                            results.add(query);
                    }
                    if (((QueryNode) node).query instanceof KNNQuery) {
                        KNNQuery query = (KNNQuery) ((QueryNode) node).query;
                        if (query.et > FAST.timestamp && query.currentLevel == parent.level &&
                                SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) &&
                                TextHelpers.containsTextually(keywords, query.keywords)) {
                            results.add(query);
                            query.pushUntilK(obj);
                        }
                    }
                } else if (node instanceof QueryListNode) {
                    for (MinimalRangeQuery query : ((QueryListNode) node).queries.mbrQueries()) {
                        FAST.objectSearchTrieNodeCounter++;
                        if (query.et > FAST.timestamp && SpatialHelper.overlapsSpatially(obj.location, query.spatialRange)
                                && TextHelpers.containsTextually(keywords, query.keywords))
                            results.add(query);
                    }
                    for (KNNQuery query : ((QueryListNode) node).queries.kNNQueries()) {
                        FAST.objectSearchTrieNodeCounter++;
                        if (query.et > FAST.timestamp && query.currentLevel == parent.level &&
                                SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) &&
                                TextHelpers.containsTextually(keywords, query.keywords)) {
                            results.add(query);
                            query.pushUntilK(obj);
                        }
                    }
                } else if (node instanceof QueryTrieNode) {
                    ((QueryTrieNode) node).find(parent, obj, keywords,i + 1, results, descendingKNNQueries);
                }
            }
    }

    private boolean searchQueries(DataObject obj, List<Query> results, Query q) {
        if (q instanceof MinimalRangeQuery && SpatialHelper.overlapsSpatially(obj.location, ((MinimalRangeQuery) q).spatialRange)) {
            results.add(q);
        }
        if (q instanceof KNNQuery) {
            KNNQuery query = (KNNQuery) q;
            if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar)) {
                results.add(query);
                return query.pushUntilK(obj);
            }
        }
        return false;
    }

    public int clean(int level, Rectangle bounds, QueryListNode combinedQueries) {
        int operations = 0;
        if (queries != null) {
            Iterator<Query> queriesItr = queries.iterator();
            while (queriesItr.hasNext()) {
                Query q = queriesItr.next();
                if (q.et < FAST.timestamp || (FAST.cleanMethod == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
                        (((KNNQuery) q).currentLevel != level || !SpatialHelper.overlapsSpatially(bounds, ((KNNQuery) q).location, ((KNNQuery) q).ar))))
                    queriesItr.remove();
                else {
                    combinedQueries.queries.add(q);
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
                    Query q = ((QueryNode) node).query;
                    if (q.et < FAST.timestamp || (FAST.cleanMethod == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
                            (((KNNQuery) q).currentLevel != level || !SpatialHelper.overlapsSpatially(bounds, ((KNNQuery) q).location, ((KNNQuery) q).ar))))
                        trieCellsItr.remove();
                    else {
                        combinedQueries.queries.add(q);
                    }
                    operations++;
                } else if (node instanceof QueryListNode) {
                    Iterator<Query> queriesInternalItr = ((QueryListNode) node).queries.iterator();
                    while (queriesInternalItr.hasNext()) {
                        Query q = queriesInternalItr.next();
                        if (q.et < FAST.timestamp || (FAST.cleanMethod == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
                                (((KNNQuery) q).currentLevel != level || !SpatialHelper.overlapsSpatially(bounds, ((KNNQuery) q).location, ((KNNQuery) q).ar))))
                            queriesInternalItr.remove();
                        else {
                            combinedQueries.queries.add(q);
                        }
                        operations++;
                    }
                    if (((QueryListNode) node).queries.isEmpty())
                        trieCellsItr.remove();
                } else if (node instanceof QueryTrieNode) {
                    operations += ((QueryTrieNode) node).clean(level, bounds, combinedQueries);
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
