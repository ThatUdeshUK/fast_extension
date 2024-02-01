package edu.purdue.cs.fast.structures;

import java.util.*;
import java.util.Map.Entry;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;

public class QueryTrieNode extends TextualNode {
    public HashMap<String, TextualNode> subtree;
    public HybridList queries;

    public ArrayList<KNNQuery> unboundedQueries;
    public ArrayList<Query> finalQueries;

    public QueryTrieNode() {
    }

    public void find(SpatialCell parent, DataObject obj, ArrayList<String> keywords, int start, List<Query> results,
                     List<ReinsertEntry> descendingKNNQueries, boolean isExpiry) {
        FAST.context.objectSearchTrieNodeCounter++;

        if (finalQueries != null)
            for (Query q : finalQueries) {
                FAST.context.objectSearchTrieFinalNodeCounter++;
                searchQueries(obj, results, q, isExpiry);
            }

        if (unboundedQueries != null) {
            for (Iterator<KNNQuery> it = unboundedQueries.iterator(); it.hasNext();) {
                KNNQuery q = it.next();
                FAST.context.objectSearchTrieFinalNodeCounter++;
                boolean nowBounded = q.ar < Double.MAX_VALUE;
                if (nowBounded) {
                    if (queries == null)
                        queries = new HybridList();

                    queries.add(q);
                    it.remove();
                } else {
                    searchQueries(obj, results, q, isExpiry);
                }
            }
        }

        if (queries != null) {
            for (MinimalRangeQuery q : queries.mbrQueries()) {
                FAST.context.objectSearchTrieNodeCounter++;
                searchQueries(obj, results, q, isExpiry);
            }

            if (FAST.config.INCREMENTAL_DESCENT) {
                SpatialCell.SpatialOverlapComparator soc = new SpatialCell.SpatialOverlapComparator(parent.bounds);
                List<KNNQuery> knnQueries = queries.kNNQueries();
                knnQueries.sort(soc);
                int queriesSize = knnQueries.size();
                int descendedCount = 0;
                for (int i = queriesSize - 1; i >= 0; i--) {
                    KNNQuery q = knnQueries.get(i);
//                    if (q.currentLevel != parent.level)
//                        continue;

                    if (q.et > FAST.context.timestamp || isExpiry) {
                        searchQueries(obj, results, q, isExpiry);
                    }

                    if (queriesSize > FAST.config.KNN_DEGRADATION_RATIO &&
                            parent.level == FAST.context.maxLevel &&
//                            q.currentLevel >= FAST.context.minInsertedLevel + 1 &&
//                            q.ar < Double.MAX_VALUE &&
                            q.ar < FAST.config.KNN_DEGRADATION_AR &&
                            descendedCount < queriesSize / 2) {
//                        if (q.id == 31) { // || q.id == 453
//                            System.out.println("Pushing down: " + q.id + ", from: " + q.currentLevel + ", x: " + q.location.x
//                                    + ", y: " + q.location.y + ", ar: " + q.ar + ", list_size: " + queriesSize + ", coz: " + obj.id);
//                        }
                        q = knnQueries.remove(i);
//                        Run.logger.debug("Descend from level on search " + parent.level + ", query: " + q.id);
                        descendingKNNQueries.add(new ReinsertEntry(SpatialHelper.spatialIntersect(FAST.context.bounds, q.spatialBox()), q));
                        descendedCount++;
                    }
                }
            } else {
                for (Iterator<KNNQuery> it = queries.kNNQueries().iterator(); it.hasNext(); ) {
                    KNNQuery q = it.next();
                    FAST.context.objectSearchTrieNodeCounter++;
                    if ((q.et <= FAST.context.timestamp && !isExpiry) || q.currentLevel != parent.level)
                        continue;

                    boolean kFilled = searchQueries(obj, results, q, isExpiry);
                    if (descendingKNNQueries != null && parent.level == FAST.context.maxLevel && kFilled) {
                        descendingKNNQueries.add(new ReinsertEntry(SpatialHelper.spatialIntersect(FAST.context.bounds, q.spatialBox()), q));
                        it.remove();
                    }
                }
            }
        }

        if (subtree != null)
            for (int i = start; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                TextualNode node = subtree.get(keyword);
                if (node == null)
                    continue;

                FAST.context.objectSearchTrieHashAccess++;
                if (node instanceof QueryNode) {
                    FAST.context.objectSearchTrieNodeCounter++;
                    if (((QueryNode) node).query instanceof MinimalRangeQuery) {
                        MinimalRangeQuery query = (MinimalRangeQuery) ((QueryNode) node).query;
                        if (query.et > FAST.context.timestamp && SpatialHelper.overlapsSpatially(obj.location, query.spatialRange) &&
                                TextHelpers.containsTextually(keywords, query.keywords))
                            results.add(query);
                    }
                    if (((QueryNode) node).query instanceof KNNQuery) {
                        KNNQuery query = (KNNQuery) ((QueryNode) node).query;
                        if ((query.et > FAST.context.timestamp || isExpiry) && (FAST.config.INCREMENTAL_DESCENT || query.currentLevel == parent.level) &&
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
                        FAST.context.objectSearchTrieNodeCounter++;
                        if (query.et > FAST.context.timestamp && SpatialHelper.overlapsSpatially(obj.location, query.spatialRange)
                                && TextHelpers.containsTextually(keywords, query.keywords))
                            results.add(query);
                    }
                    for (KNNQuery query : ((QueryListNode) node).queries.kNNQueries()) {
                        FAST.context.objectSearchTrieNodeCounter++;
                        if ((query.et > FAST.context.timestamp || isExpiry) && (FAST.config.INCREMENTAL_DESCENT || query.currentLevel == parent.level) &&
                                SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar) &&
                                TextHelpers.containsTextually(keywords, query.keywords)) {
                            results.add(query);
                            if (!isExpiry) {
                                query.pushUntilKHat(obj);
                            }
                        }
                    }
                } else if (node instanceof QueryTrieNode) {
                    ((QueryTrieNode) node).find(parent, obj, keywords, i + 1, results, descendingKNNQueries, isExpiry);
                }
            }
    }

    private boolean searchQueries(DataObject obj, List<Query> results, Query q, boolean isExpiry) {
        if (q instanceof MinimalRangeQuery && SpatialHelper.overlapsSpatially(obj.location, ((MinimalRangeQuery) q).spatialRange)) {
            results.add(q);
        }
        if (q instanceof KNNQuery) {
            KNNQuery query = (KNNQuery) q;
            if (query.id == 135 && obj.id == 135) {
                System.out.println("DEBUG!");
            }
            if (SpatialHelper.overlapsSpatially(obj.location, query.location, query.ar)) {
                results.add(query);
                if (!isExpiry)
                    query.pushUntilKHat(obj);
                return query.kFilled();
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
                if (q.et < FAST.context.timestamp || (FAST.config.CLEAN_METHOD == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
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
                    if (q.et < FAST.context.timestamp || (FAST.config.CLEAN_METHOD == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
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
                        if (q.et < FAST.context.timestamp || (FAST.config.CLEAN_METHOD == CleanMethod.EXPIRE_KNN && q instanceof KNNQuery &&
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

