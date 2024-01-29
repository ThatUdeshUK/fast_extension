package edu.purdue.cs.fast.baselines.fast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.purdue.cs.fast.baselines.fast.helper.LSpatialHelper;
import edu.purdue.cs.fast.baselines.fast.helper.LTextHelpers;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;

public class LKeywordTrieCell {
	public HashMap<String, Object> trieCells;
	public ArrayList<LMinimalRangeQuery> queries;
	public ArrayList<LMinimalRangeQuery> finalQueries;
	//public boolean extended ;

	public LKeywordTrieCell() {
		queries = null;//
		finalQueries = null;
		trieCells = null;
		//	extended = false;
	}

	public void find(ArrayList<String> keywords, int start, List<Query> result, int level, Point location) {
		LFAST.objectSearchTrieNodeCounter++;
		if (finalQueries != null)
			for (LMinimalRangeQuery q : finalQueries) {
				LFAST.objectSearchTrieFinalNodeCounter++;
				if (LSpatialHelper.overlapsSpatially(location, q.spatialBox())) {
					result.add(q);
				}
			}
		if (queries != null)
			for (LMinimalRangeQuery q : queries) {
				LFAST.objectSearchTrieNodeCounter++;
				if (LSpatialHelper.overlapsSpatially(location, q.spatialBox())) {
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
				LFAST.objectSearchTrieHashAccess++;
				if (cell instanceof LMinimalRangeQuery) {
					LFAST.objectSearchTrieNodeCounter++;
					if (LSpatialHelper.overlapsSpatially(location, ((LMinimalRangeQuery) cell).spatialBox()) && LTextHelpers.containsTextually(keywords, ((LMinimalRangeQuery) cell).getKeywords()))
						result.add(((LMinimalRangeQuery) cell));

				} else if (cell instanceof ArrayList) {
					for (LMinimalRangeQuery q : ((ArrayList<LMinimalRangeQuery>) cell)) {
						LFAST.objectSearchTrieNodeCounter++;
						if (LSpatialHelper.overlapsSpatially(location, q.spatialBox()) && LTextHelpers.containsTextually(keywords, q.getKeywords()))
							result.add(q);
					}

				} else if (cell instanceof LKeywordTrieCell) {
					((LKeywordTrieCell) cell).find(keywords, i + 1, result, level + 1, location);
				}

			}
	}

	public void findTextualOnly(ArrayList<String> keywords, int start, List<LMinimalRangeQuery> result, int level) {

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
				if (cell instanceof LMinimalRangeQuery) {
					if (LTextHelpers.containsTextually(keywords, ((LMinimalRangeQuery) cell).getKeywords(), i, level))
						result.add(((LMinimalRangeQuery) cell));


				} else if (cell instanceof ArrayList) {
					for (LMinimalRangeQuery q : ((ArrayList<LMinimalRangeQuery>) cell)) {
						if (LTextHelpers.containsTextually(keywords, q.getKeywords(), i, level))
							result.add(q);

					}

				} else if (cell instanceof LKeywordTrieCell) {
					((LKeywordTrieCell) cell).findTextualOnly(keywords, i + 1, result, level + 1);
				}

			}

	}

	public int clean(ArrayList<LMinimalRangeQuery> combinedQueries) {
		int operations = 0;
		if (queries != null) {
			Iterator<LMinimalRangeQuery> queriesItr = queries.iterator();
			while (queriesItr.hasNext()) {
				LMinimalRangeQuery query = queriesItr.next();
				if (query.et < LFAST.queryTimeStampCounter)
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
				if (cell instanceof LMinimalRangeQuery) {
					if (((LMinimalRangeQuery) cell).et < LFAST.queryTimeStampCounter)
						trieCellsItr.remove();
					else {
						combinedQueries.add((LMinimalRangeQuery) cell);
					}
					operations++;
				} else if (cell instanceof ArrayList) {
					Iterator<LMinimalRangeQuery> queriesInternalItr = ((ArrayList<LMinimalRangeQuery>) cell).iterator();
					while (queriesInternalItr.hasNext()) {
						LMinimalRangeQuery query = queriesInternalItr.next();
						if (query.et < LFAST.queryTimeStampCounter)
							queriesInternalItr.remove();
						else {
							combinedQueries.add(query);
						}
						operations++;
					}
					if (((ArrayList<LMinimalRangeQuery>) cell).size() == 0)
						trieCellsItr.remove();
				} else if (cell instanceof LKeywordTrieCell) {
					operations += ((LKeywordTrieCell) cell).clean(combinedQueries);
					if (((LKeywordTrieCell) cell).queries == null && ((LKeywordTrieCell) cell).trieCells == null)
						trieCellsItr.remove();
				}
			}
			if (trieCells.size() == 0)
				trieCells = null;
		}

		return operations;
	}

}
