package edu.purdue.cs.fast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import edu.purdue.cs.fast.messages.MinimalRangeQuery;

public class KeyWordTrieIndexMinimal {
	public KeywordTrieCellMinimal root;
	public static int SPLIT_THRESHOLD = 0;
	public static int SPLIT_THRESHOLD_FREQ =0;
	int size = 0;

	public KeyWordTrieIndexMinimal() {
		root = new KeywordTrieCellMinimal();
		root.trieCells = new HashMap<String, KeywordTrieCellMinimal>();
		index = new HashMap<String, HashMap<String, ArrayList<MinimalRangeQuery>>>();
	}

	HashMap<String, HashMap<String, ArrayList<MinimalRangeQuery>>> index;

	public void insert(ArrayList<String> keywordList, MinimalRangeQuery q) {
		KeywordTrieCellMinimal currentCell = root;
		size++;
		int i = 0;
		for (String keyword : keywordList) {

			if (currentCell.trieCells == null)
				currentCell.trieCells = new HashMap<String, KeywordTrieCellMinimal>();
			
			KeywordTrieCellMinimal cell = currentCell.trieCells.get(keyword);
			if (cell == null) {
				cell = new KeywordTrieCellMinimal();
				currentCell.trieCells.put(keyword, cell);

			}
			currentCell = cell;
			if (currentCell.trieCells == null && i<keywordList.size()-1&&(currentCell.verifiableQueries == null || currentCell.verifiableQueries.size() <= SPLIT_THRESHOLD)) {
				if (currentCell.verifiableQueries == null)
					currentCell.verifiableQueries = new LinkedList<MinimalRangeQuery>();
				currentCell.verifiableQueries.add(q);
				return;
			} else if (currentCell.trieCells == null &&currentCell.verifiableQueries!=null&&currentCell.verifiableQueries.size() > SPLIT_THRESHOLD) {
				currentCell.trieCells = new HashMap<String, KeywordTrieCellMinimal>();
				//expand one more keyword
				for (MinimalRangeQuery otherQuery : currentCell.verifiableQueries) {
					KeywordTrieCellMinimal otherCell = currentCell.trieCells.get(otherQuery.getQueryText().get(i + 1));
					if (otherCell == null) {
						otherCell = new KeywordTrieCellMinimal();
						currentCell.trieCells.put(otherQuery.getQueryText().get(i + 1), otherCell);
					}
					if (i + 1 == otherQuery.getQueryText().size() - 1) {//last keyword
						if (otherCell.storedQueries == null)
							otherCell.storedQueries = new LinkedList<MinimalRangeQuery>();
						otherCell.storedQueries.add(otherQuery);
					} else {
						if (otherCell.verifiableQueries == null)
							otherCell.verifiableQueries = new LinkedList<MinimalRangeQuery>();
						otherCell.verifiableQueries.add(otherQuery);
					}
					currentCell.trieCells.put(keyword, otherCell);
				}
				currentCell.verifiableQueries = null;
			}
			i++;
			//				
		}
		if (currentCell.storedQueries == null)
			currentCell.storedQueries = new LinkedList<MinimalRangeQuery>();
		currentCell.storedQueries.add(q);

	}
	public void insertNonExpandable(ArrayList<String> keywordList, MinimalRangeQuery q) {
		KeywordTrieCellMinimal currentCell = root;
		size++;
		
		for (String keyword : keywordList) {

			if (currentCell.trieCells == null)
				currentCell.trieCells = new HashMap<String, KeywordTrieCellMinimal>();
			
			KeywordTrieCellMinimal cell = currentCell.trieCells.get(keyword);
			if (cell == null) {
				cell = new KeywordTrieCellMinimal();
				currentCell.trieCells.put(keyword, cell);

			}
			currentCell = cell;
		}
		if (currentCell.storedQueries == null)
			currentCell.storedQueries = new LinkedList<MinimalRangeQuery>();
		currentCell.storedQueries.add(q);

	}

	public void insertTwoLevelsOnly(ArrayList<String> keywordList, MinimalRangeQuery q) {
		String k1 = keywordList.get(0);
		String k2 = keywordList.get(1);
		HashMap<String, ArrayList<MinimalRangeQuery>> innerMap = index.get(k1);
		if (innerMap == null) {
			innerMap = new HashMap<String, ArrayList<MinimalRangeQuery>>();
			index.put(k1, innerMap);
		}
		ArrayList<MinimalRangeQuery> queries = innerMap.get(k2);
		if (queries == null) {
			queries = new ArrayList<MinimalRangeQuery>();
			innerMap.put(k2, queries);
		}
		queries.add(q);
	}

	public void insertOld1(ArrayList<String> keywordList, MinimalRangeQuery q) {
		KeywordTrieCellMinimal currentCell = root;

		for (String keyword : keywordList) {

			if (currentCell.trieCells == null)
				currentCell.trieCells = new HashMap<String, KeywordTrieCellMinimal>();
			KeywordTrieCellMinimal cell = currentCell.trieCells.get(keyword);
			if (cell == null) {
				cell = new KeywordTrieCellMinimal();
				currentCell.trieCells.put(keyword, cell);

			}
			currentCell = cell;
		}
		if (currentCell.storedQueries == null)
			currentCell.storedQueries = new LinkedList<MinimalRangeQuery>();
		currentCell.storedQueries.add(q);
		size++;
	}

	public void insertOld2(String keyword, MinimalRangeQuery q) {
		KeywordTrieCellMinimal currentCell = root;
		int level = 0;
		size++;
		//for (String keyword : keywordList) {
		if (currentCell.trieCells == null) {
			currentCell.storedQueries.add(q);
			if (currentCell.storedQueries.size() > SPLIT_THRESHOLD) {
				currentCell.trieCells = new HashMap<String, KeywordTrieCellMinimal>();
				LinkedList<MinimalRangeQuery> remainingQueries = new LinkedList<MinimalRangeQuery>();
				for (MinimalRangeQuery query : currentCell.storedQueries) {
					if (query.getQueryText().size() > level) {
						if (!currentCell.trieCells.containsKey(query.getQueryText().get(level))) {

							currentCell.trieCells.put(query.getQueryText().get(level), new KeywordTrieCellMinimal());
						}
						currentCell.trieCells.get(query.getQueryText().get(level)).storedQueries.add(query);
					} else {
						remainingQueries.add(query);
					}
				}
				currentCell.storedQueries = remainingQueries;

			}
			return;
		} else {
			if (currentCell.trieCells.containsKey(keyword)) {

				currentCell = currentCell.trieCells.get(keyword);
				level++;
			} else {
				KeywordTrieCellMinimal cell = new KeywordTrieCellMinimal();
				cell.storedQueries.add(q);
				currentCell.trieCells.put(keyword, cell);
				return;
			}
		}
		//	}

	}

	public ArrayList<MinimalRangeQuery> findOLd(ArrayList<String> keywordList) {
		ArrayList<MinimalRangeQuery> candidate = new ArrayList<MinimalRangeQuery>();
		for (int i = 0; i < keywordList.size(); i++) {
			if (index.containsKey(keywordList.get(i))) {
				HashMap<String, ArrayList<MinimalRangeQuery>> innerMap = index.get(keywordList.get(i));
				for (int j = i + 1; j < keywordList.size(); j++) {
					if (innerMap.containsKey(keywordList.get(j))) {
						candidate.addAll(innerMap.get(keywordList.get(j)));
					}
				}
			}
		}
		return candidate;
	}

	public LinkedList<MinimalRangeQuery> find(ArrayList<String> keywordList) {
		LinkedList<MinimalRangeQuery> result = new LinkedList<MinimalRangeQuery>();
		
		root.find(keywordList, 0, result, 0);
		return result;
	}
	public LinkedList<MinimalRangeQuery> find(ArrayList<String> keywordList,Integer[] opCount) {
		LinkedList<MinimalRangeQuery> result = new LinkedList<MinimalRangeQuery>();
		root.find(keywordList, 0, result, 0,opCount);
		return result;
	}

	public int size() {
		return size;
	}
}
