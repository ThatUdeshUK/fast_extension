package edu.purdue.cs.fast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.messages.MinimalRangeQuery;

public class KeywordTrieCell {
	public HashMap<String, Object> trieCells;
	public ArrayList<MinimalRangeQuery> queries;
	public ArrayList<MinimalRangeQuery> finalQueries;
	//public boolean extended ;

	public KeywordTrieCell() {
		queries = null;//
		finalQueries = null;
		trieCells = null;
		//	extended = false;
	}

	public void find(ArrayList<String> keywords, int start, List<MinimalRangeQuery> result, int level, Point location) {
		FAST.objectSearchTrieNodeCounter++;
		if (finalQueries != null)
			for (MinimalRangeQuery q : finalQueries) {
				FAST.objectSearchTrieFinalNodeCounter++;
				if (SpatialHelper.overlapsSpatially(location, q.spatialRange)) {
					result.add(q);
				}
			}
		if (queries != null)
			for (MinimalRangeQuery q : queries) {
				FAST.objectSearchTrieNodeCounter++;
				if (SpatialHelper.overlapsSpatially(location, q.spatialRange)) {
					result.add(q);
				}
			}
		int i = start;
		HashMap<String, Object> currentCells = trieCells;
		if (currentCells != null)
			for (; i < keywords.size(); i++) {
				String keyword = keywords.get(i);
				Object cell = currentCells.get(keyword);

				if (cell == null)
					continue;
				FAST.objectSearchTrieHashAccess++;
				if (cell instanceof MinimalRangeQuery) {
					FAST.objectSearchTrieNodeCounter++;
					if (SpatialHelper.overlapsSpatially(location, ((MinimalRangeQuery) cell).spatialRange) && TextHelpers.containsTextually(keywords, ((MinimalRangeQuery) cell).getQueryText()))
						result.add(((MinimalRangeQuery) cell));

				} else if (cell instanceof ArrayList) {
					for (MinimalRangeQuery q : ((ArrayList<MinimalRangeQuery>) cell)) {
						FAST.objectSearchTrieNodeCounter++;
						if (SpatialHelper.overlapsSpatially(location, q.spatialRange) && TextHelpers.containsTextually(keywords, q.getQueryText()))
							result.add(q);
					}

				} else if (cell instanceof KeywordTrieCell) {
					((KeywordTrieCell) cell).find(keywords, i + 1, result, level + 1, location);
				}

			}
	}

	public void findTextualOnly(ArrayList<String> keywords, int start, List<MinimalRangeQuery> result, int level) {

		if (queries != null)
			result.addAll(queries);

		int i = start;
		HashMap<String, Object> currentCells = trieCells;
		if (currentCells != null)
			for (; i < keywords.size(); i++) {
				String keyword = keywords.get(i);
				Object cell = currentCells.get(keyword);

				if (cell == null)
					continue;
				if (cell instanceof MinimalRangeQuery) {
					if (TextHelpers.containsTextually(keywords, ((MinimalRangeQuery) cell).getQueryText(), i, level))
						result.add(((MinimalRangeQuery) cell));
					

				} else if (cell instanceof ArrayList) {
					for (MinimalRangeQuery q : ((ArrayList<MinimalRangeQuery>) cell)) {
						if (TextHelpers.containsTextually(keywords, q.getQueryText(), i, level))
							result.add(q);
						
					}

				} else if (cell instanceof KeywordTrieCell) {
					((KeywordTrieCell) cell).findTextualOnly(keywords, i + 1, result, level + 1);
				}

			}

	}

	public int clean(ArrayList<MinimalRangeQuery> combinedQueries) {
		int operations = 0;
		if (queries != null) {
			Iterator<MinimalRangeQuery> queriesItr = queries.iterator();
			while (queriesItr.hasNext()) {
				MinimalRangeQuery query = queriesItr.next();
				if (query.expireTime < FAST.queryTimeStampCounter)
					queriesItr.remove();
				else {
					combinedQueries.add(query);
				}
				operations++;
			}
			if (queries.size() == 0)
				queries = null;
		}
		if (trieCells != null) {
			Iterator<Entry<String, Object>> trieCellsItr = trieCells.entrySet().iterator();
			while (trieCellsItr.hasNext()) {
				Entry<String, Object> trieCellEntry = trieCellsItr.next();
				Object cell = trieCellEntry.getValue();
				if (cell instanceof MinimalRangeQuery) {
					if (((MinimalRangeQuery) cell).expireTime < FAST.queryTimeStampCounter)
						trieCellsItr.remove();
					else {
						combinedQueries.add((MinimalRangeQuery) cell);
					}
					operations++;
				} else if (cell instanceof ArrayList) {
					Iterator<MinimalRangeQuery> queriesInternalItr = ((ArrayList<MinimalRangeQuery>) cell).iterator();
					while (queriesInternalItr.hasNext()) {
						MinimalRangeQuery query = queriesInternalItr.next();
						if (query.expireTime < FAST.queryTimeStampCounter)
							queriesInternalItr.remove();
						else {
							combinedQueries.add(query);
						}
						operations++;
					}
					if (((ArrayList<MinimalRangeQuery>) cell).size() == 0)
						trieCellsItr.remove();
				} else if (cell instanceof KeywordTrieCell) {
					operations += ((KeywordTrieCell) cell).clean(combinedQueries);
					if (((KeywordTrieCell) cell).queries == null && ((KeywordTrieCell) cell).trieCells == null)
						trieCellsItr.remove();
				}
			}
			if (trieCells.size() == 0)
				trieCells = null;
		}

		return operations;
	}

}
