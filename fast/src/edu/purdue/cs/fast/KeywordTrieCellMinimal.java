package edu.purdue.cs.fast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.messages.MinimalRangeQuery;

public class KeywordTrieCellMinimal {
	public HashMap<String, KeywordTrieCellMinimal> trieCells;
	public LinkedList<MinimalRangeQuery> storedQueries;
	public LinkedList<MinimalRangeQuery> verifiableQueries;

	public KeywordTrieCellMinimal() {
		storedQueries = null;// new ArrayList<MinimalRangeQuery>();// new ArrayList<MinimalRangeQuery>();
		verifiableQueries = null;//
		trieCells = null;
	}

	public void find(ArrayList<String> keywords, int start, LinkedList<MinimalRangeQuery> result, int level) {
		if (storedQueries != null) {
			result.addAll(storedQueries);
		}
		if (verifiableQueries != null)
			for (MinimalRangeQuery q : verifiableQueries) {
				if (TextHelpers.containsTextually(keywords, q.getQueryText()))
					result.add(q);
			}
		int i = start;
		if (trieCells != null)
			for (; i < keywords.size(); i++) {
				String keyword = keywords.get(i);
				KeywordTrieCellMinimal cell = trieCells.get(keyword);
				if (cell != null)
					cell.find(keywords, i + 1, result, level + 1);
			}
	}
	public void find(ArrayList<String> keywords, int start, LinkedList<MinimalRangeQuery> result, int level, Integer[]opCount) {
		
		if (storedQueries != null) {
			opCount[2]++;
			opCount[0]+=storedQueries.size();
			result.addAll(storedQueries);
		}
		if (verifiableQueries != null) {
			
			for (MinimalRangeQuery q : verifiableQueries) {
				opCount[0]+=verifiableQueries.size();
				//opCount[0]+=(q.queryText.size()+keywords.size());
				if (TextHelpers.containsTextually(keywords, q.getQueryText()))
					result.add(q);
			}
		}
		int i = start;
		if (trieCells != null)
			for (; i < keywords.size(); i++) {
				
				opCount  [0]++;
				opCount[1]++;
				String keyword = keywords.get(i);
				KeywordTrieCellMinimal cell = trieCells.get(keyword);
				//opCount[0]++;
				if (cell != null) {
					
					
					cell.find(keywords, i + 1, result, level + 1,opCount);
				}
				
					
			}
	}

	public void findOld(ArrayList<String> keywords, int start, ArrayList<MinimalRangeQuery> result) {
		if (storedQueries != null)
			result.addAll(storedQueries);

		int i = start;
		if (trieCells != null)
			for (; i < keywords.size(); i++) {
				if (trieCells.containsKey(keywords.get(i)))
					trieCells.get(keywords.get(i)).findOld(keywords, i + 1, result);
			}
	}

}
