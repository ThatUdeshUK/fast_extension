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
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.purdue.cs.fast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.messages.MinimalRangeQuery;
import edu.purdue.cs.fast.messages.Query;

public class ExpandbleIndex {

	public ConcurrentHashMap<String, Object> ptp;//queries that fall only within the cells range
	Iterator<Entry<String, Object>> cleaningIterator;
	boolean test;
	MinimalRangeQuery testObject;
	int level;
	int debugQID = -1;
	public static HashMap<String, KeywordFrequencyStats> overallQueryTextSummery;

	public ExpandbleIndex() {
		ptp = null;
		overallQueryTextSummery = new HashMap<String, KeywordFrequencyStats>();
	}

	public Object addInternalQueryNoShare(MinimalRangeQuery query) {
		if (ptp == null) {
			ptp = new ConcurrentHashMap<String, Object>();
		}
		Queue<MinimalRangeQuery> queue = new LinkedList<MinimalRangeQuery>();
		boolean inserted = false;
		
		String keyword = null;
		int minCount = Integer.MAX_VALUE;

		for (String key :query.getQueryText()) {
			KeywordFrequencyStats stats = overallQueryTextSummery.get(key);
			if (stats == null) {
				overallQueryTextSummery.put(key, stats);
				keyword = key;
				minCount = 0;
			} else {
					stats.queryCount++;
				if (stats.queryCount < minCount) {
					minCount = stats.queryCount;
					keyword = key;
				}
			}
		}
		
		inserted = insertAtKeyWord(keyword, query);
		if (inserted)
			if (ptp.get(keyword) instanceof ArrayList)
				return ptp.get(keyword);
			else
				return null;
		else {
			queue.add(query);

		}

		while (!queue.isEmpty()) {
			query = queue.remove();
			
			inserted = false;

			String otherKeyword = getOtherKeywordToInser(query);
			if (otherKeyword != null) {
				inserted = insertAtKeyWord(otherKeyword, query);
			}
			if (inserted)
				continue;
			else {
				for (String term : query.queryText) {
					//mark all these keywords as tries
					if (ptp.get(term) instanceof ArrayList) {
						
						queue.addAll((ArrayList<MinimalRangeQuery>) ptp.get(term));
						ptp.put(term, new KeywordTrieCell());
						FAST.numberOfTrieNodes++;
					}
				}
			}
			;
			KeywordTrieCell currentCell = (KeywordTrieCell) ptp.get(query.queryText.get(0));

			for (int j = 1; j < query.getQueryText().size() & !inserted; j++) {
				keyword = query.getQueryText().get(j);
				if (currentCell.trieCells == null)
					currentCell.trieCells = new HashMap<String, Object>();

				Object cell = currentCell.trieCells.get(keyword);
				if (cell == null) {
					currentCell.trieCells.put(keyword, query);
					inserted = true;
				} else if (cell instanceof MinimalRangeQuery) {
					if (((MinimalRangeQuery) cell).expireTime > FAST.queryTimeStampCounter) {
						ArrayList<MinimalRangeQuery> queries = new ArrayList<MinimalRangeQuery>();
						queries.add((MinimalRangeQuery) cell);
						queries.add((MinimalRangeQuery) query);
						currentCell.trieCells.put(keyword, queries);
						inserted = true;
					} else {
						currentCell.trieCells.put(keyword, query);
						inserted = true;
					}
				} else if (cell instanceof ArrayList && ((ArrayList<MinimalRangeQuery>) cell).size() <= FAST.Trie_SPLIT_THRESHOLD) {
					((ArrayList<MinimalRangeQuery>) cell).add(query);
					inserted = true;
				} else if (cell instanceof ArrayList && ((ArrayList<MinimalRangeQuery>) cell).size() > FAST.Trie_SPLIT_THRESHOLD) {
					KeywordTrieCell newCell = new KeywordTrieCell();
					FAST.numberOfTrieNodes++;
					((ArrayList<MinimalRangeQuery>) cell).add(query);
					newCell.trieCells = new HashMap<String, Object>();
					newCell.queries = new ArrayList<MinimalRangeQuery>();
					currentCell.trieCells.put(keyword, newCell);
					for (MinimalRangeQuery otherQuery : ((ArrayList<MinimalRangeQuery>) cell)) {
						if (otherQuery.getQueryText().size() > (j + 1)) {

							Object otherCell = newCell.trieCells.get(otherQuery.getQueryText().get(j + 1));
							if (otherCell == null) {
								otherCell = new ArrayList<MinimalRangeQuery>();
							}
							((ArrayList<MinimalRangeQuery>) otherCell).add(otherQuery);
							newCell.trieCells.put(otherQuery.getQueryText().get(j + 1), otherCell);
						} else {
							newCell.queries.add(otherQuery);
						}

					}
					if (newCell.queries != null && newCell.queries.size() == 0)
						newCell.queries = null;
					inserted = true;
				} else if (cell instanceof KeywordTrieCell) {
					if (j < (query.getQueryText().size() - 1)) {
						currentCell = (KeywordTrieCell) cell;
					} else {

						if (((KeywordTrieCell) cell).queries == null)
							((KeywordTrieCell) cell).queries = new ArrayList<MinimalRangeQuery>();
						((KeywordTrieCell) cell).queries.add(query);
						if (query.queryId == debugQID)
							System.out.println("Not final" + query);

						inserted = true;
					}

				}

			}
			if (!inserted) {

				if (currentCell.queries == null)
					currentCell.queries = new ArrayList<MinimalRangeQuery>();
				currentCell.queries.add(query);
				if (query.queryId == debugQID)
					System.out.println("Not final" + query);

			}

		}
		return null;

	}

	public boolean insertAtKeyWord(String keyword, MinimalRangeQuery query) {
		boolean inserted = false;
		if (!ptp.containsKey(keyword)) {
			inserted = true;
			ptp.put(keyword, query);
			return inserted;
		} else { //this keyword already exists in the index
			Object keywordIndex = ptp.get(keyword);
			if (keywordIndex == null) {
				ptp.put(keyword, query);
				inserted = true;
				return inserted;
			}
			if (keywordIndex instanceof MinimalRangeQuery) { //single query 
				MinimalRangeQuery exitingQuery = (MinimalRangeQuery) keywordIndex;
				ArrayList<MinimalRangeQuery> rareQueries = new ArrayList<MinimalRangeQuery>();
				FAST.queryInsertInvListNodeCounter++;
				rareQueries.add(exitingQuery);
				rareQueries.add(query);
				inserted = true;
				ptp.put(keyword, rareQueries);
				return inserted;

			} else if ((keywordIndex instanceof ArrayList) && ((ArrayList<MinimalRangeQuery>) keywordIndex).size() < FAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
				((ArrayList<MinimalRangeQuery>) keywordIndex).add(query);
				FAST.queryInsertInvListNodeCounter++;
				inserted = true;
				return inserted;
			} else if ((keywordIndex instanceof ArrayList) && ((ArrayList<MinimalRangeQuery>) keywordIndex).size() >= FAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
				if (!((ArrayList<MinimalRangeQuery>) keywordIndex).contains(query)) {
					inserted = false;
					return inserted;
				} else {
					inserted = true;
					return inserted;
				}
				//then trie insert for all frequent keywords 
			} else if (keywordIndex instanceof KeywordTrieCell) {
				inserted = false;
				return inserted;
			} else {
				System.err.println("This is an error you should never be here");
			}
		}
		return false;
	}

	public String getOtherKeywordToInser(MinimalRangeQuery query) {
		int minSize = Integer.MAX_VALUE;
		String minKeyword = null;
		for (String term : query.queryText) {
			if (!ptp.containsKey(term)) {
				int size = 0;
				if (size < minSize) {
					minSize = size;
					minKeyword = term;
				}

			} else if (ptp.containsKey(term) && ptp.get(term) instanceof MinimalRangeQuery) {
				int size = 1;
				if (size < minSize) {
					minSize = size;
					minKeyword = term;
				}

			} else if (ptp.containsKey(term) && ptp.get(term) instanceof ArrayList) {
				int size = ((ArrayList<MinimalRangeQuery>) ptp.get(term)).size();
				if (size < minSize) {
					minSize = size;
					minKeyword = term;
				}
			}

		}
		return minKeyword;
	}

	public ArrayList<String> getInternalTextualOverlappingQueries(ArrayList<String> keywords, List<MinimalRangeQuery> finalQueries) {
		ArrayList<String> remainingKewords = new ArrayList<String>();
		for (int i = 0; i < keywords.size(); i++) {
			String keyword = keywords.get(i);
			Object keyWordIndex = ptp.get(keyword);
			if (keyWordIndex != null) {
				if (keyWordIndex instanceof MinimalRangeQuery) {
					MinimalRangeQuery query = ((MinimalRangeQuery) keyWordIndex);
					if (keywords.size() >= query.queryText.size() && TextHelpers.containsTextually(keywords, query.getQueryText())) {
						finalQueries.add(query);
					}
				} else if (keyWordIndex instanceof ArrayList) {
					ArrayList<MinimalRangeQuery> rareQueries = (ArrayList<MinimalRangeQuery>) keyWordIndex;
					for (MinimalRangeQuery q : rareQueries) {
						if (keywords.size() >= q.queryText.size() && TextHelpers.containsTextually(keywords, q.getQueryText())) {
							finalQueries.add(q);
						}
					}
				} else if (keyWordIndex instanceof KeywordTrieCell) {
					((KeywordTrieCell) keyWordIndex).findTextualOnly(keywords, i+1, finalQueries, 1);

				}
			}

		}

		return remainingKewords;
	}

}
