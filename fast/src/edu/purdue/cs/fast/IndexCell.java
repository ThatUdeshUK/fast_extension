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

public class IndexCell {

	public ConcurrentHashMap<String, Object> ptp;//queries that fall only within the cells range
	Iterator<Entry<String, Object>> cleaningIterator;
	Rectangle bounds;
	int coordinate;
	boolean test;
	MinimalRangeQuery testObject ;
	int level;
	int debugQID=-1;
	

	public IndexCell(Rectangle bounds, Integer globalCoordinates, int level) {
		ptp = null;
		this.bounds = bounds;
		this.bounds.getMax().X-=.001;
		this.bounds.getMax().Y-=.001;
		this.coordinate = globalCoordinates;
		test = false;
		this.level=level;
	}

	
	public void addInternalQuery(String keyword, MinimalRangeQuery query, ArrayList<MinimalRangeQuery> sharedQueries, ArrayList<ReinsertEntry>insertNextLevelQueries, boolean force) {
		if (ptp == null) {
			ptp = new ConcurrentHashMap<String, Object>();
		}
		if (!ptp.containsKey(keyword) && sharedQueries != null) {
			FAST.numberOfHashEntries++;
			ptp.put(keyword, sharedQueries);
		} else {
			Object keywordIndex = ptp.get(keyword);
			if (keywordIndex instanceof MinimalRangeQuery) {
				MinimalRangeQuery exitingQuery = (MinimalRangeQuery) keywordIndex;
				if (exitingQuery.expireTime > FAST.queryTimeStampCounter) { //checking for the support of the query
					if (sharedQueries.contains(exitingQuery)) {
						ptp.put(keyword, sharedQueries);
					} else if (sharedQueries.size() < FAST.Trie_SPLIT_THRESHOLD) {
						FAST.queryInsertInvListNodeCounter++;
						sharedQueries.add(exitingQuery);
						ptp.put(keyword, sharedQueries);
					} else
						addInternalQueryNoShare(keyword, query, sharedQueries, insertNextLevelQueries, force);
				} else {
					deleteQueryFromStats(query);
					ptp.put(keyword, sharedQueries);
				}
			} else if (keywordIndex instanceof ArrayList && keywordIndex == sharedQueries) {
				//already inserted do nothing;

			} else if (keywordIndex instanceof ArrayList && keywordIndex != sharedQueries) {
				//already inserted do nothing;
				ArrayList<MinimalRangeQuery> nonSharedQueries = new ArrayList<MinimalRangeQuery>();
				for (MinimalRangeQuery q : (ArrayList<MinimalRangeQuery>) keywordIndex) {
					if (!sharedQueries.contains(q))
						nonSharedQueries.add(q);
				}
				if (nonSharedQueries.size() > 0 && nonSharedQueries.size() + sharedQueries.size() <=   FAST.Trie_SPLIT_THRESHOLD) {
					sharedQueries.addAll(nonSharedQueries);
					ptp.put(keyword, sharedQueries);
				} else {
					addInternalQueryNoShare(keyword, query, sharedQueries, insertNextLevelQueries, force);
				}
			} else {

				addInternalQueryNoShare(keyword, query, sharedQueries, insertNextLevelQueries, force);
			}
		}
	}

	public void deleteQueryFromStats(MinimalRangeQuery query) {
		if (query.deleted == false) {
			query.deleted = true;
			for (String keyword : query.queryText) {
				FAST.overallQueryTextSummery.get(keyword).queryCount--;
			}
		}
	}

	public Object addInternalQueryNoShare(String keyword, MinimalRangeQuery query, ArrayList<MinimalRangeQuery> sharedQueries, ArrayList<ReinsertEntry> insertNextLevelQueries, boolean force) {
		if (ptp == null) {
			ptp = new ConcurrentHashMap<String, Object>();
		}
		Queue<MinimalRangeQuery> queue = new LinkedList<MinimalRangeQuery>();
		boolean inserted = false;
		//step 1 find the best keyword to insert in:
		if(query.queryId==debugQID ){
			System.out.println("Inserting: "+query+coordinate);
		}
		inserted = insertAtKeyWord(keyword, query, sharedQueries);
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
			if(query.queryId==debugQID ){
				System.out.println("Inserting: "+query+coordinate);
			}
			inserted = false;

			String otherKeyword = getOtherKeywordToInser(query);
			if (otherKeyword != null) {
				inserted = insertAtKeyWord(otherKeyword, query, null);
			}
			if (inserted)
				continue;
			else {
				for (String term : query.queryText) {
					//mark all these keywords as tries
					if (ptp.get(term) instanceof ArrayList) {
						for(MinimalRangeQuery q:(ArrayList<MinimalRangeQuery>) ptp.get(term)){
							if(q.queryId == debugQID)
								System.out.println("Adding query to queue for reinsert at a different list: "+query+coordinate +"keyword "+term);
						}
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
						deleteQueryFromStats((MinimalRangeQuery) cell);
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
						if (otherQuery.expireTime > FAST.queryTimeStampCounter) {
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
						} else {
							deleteQueryFromStats(otherQuery);
						}
					}
					if (newCell.queries != null && newCell.queries.size() == 0)
						newCell.queries = null;
					inserted = true;
				} else if (cell instanceof KeywordTrieCell) {
					if (j < (query.getQueryText().size() - 1)) {
						currentCell = (KeywordTrieCell) cell;
					} else {
						if (level==0||checkSpanForForceInsertFinal(query)) {
							if (((KeywordTrieCell) cell).finalQueries == null)
								((KeywordTrieCell) cell).finalQueries = new ArrayList<MinimalRangeQuery>();
							((KeywordTrieCell) cell).finalQueries.add(query);
							if(query.queryId==debugQID){
								System.out.println("final"+query+coordinate);
								test = true;
								testObject = query;
							}
						} else {

							if (((KeywordTrieCell) cell).queries == null)
								((KeywordTrieCell) cell).queries = new ArrayList<MinimalRangeQuery>();
							((KeywordTrieCell) cell).queries.add(query);
							if(query.queryId==debugQID)
								System.out.println("Not final"+query+coordinate);
							if (((KeywordTrieCell) cell).queries.size() >  FAST.Degredation_Ratio) {
								findQueriesToReinsert((KeywordTrieCell) cell, insertNextLevelQueries);
							}
						}
						inserted = true;
					}

				}

			}
			if (!inserted) {
				if ((level==0||checkSpanForForceInsertFinal(query)) ) {
					if (currentCell.finalQueries == null)
						currentCell.finalQueries = new ArrayList<MinimalRangeQuery>();
					currentCell.finalQueries.add(query);
					if(query.queryId==debugQID)
						System.out.println("final"+query+coordinate);

				} else {
					if (currentCell.queries == null)
						currentCell.queries = new ArrayList<MinimalRangeQuery>();
					currentCell.queries.add(query);
					if(query.queryId==debugQID)
						System.out.println("Not final"+query+coordinate);
					if (currentCell.queries.size() > FAST.Degredation_Ratio)
						findQueriesToReinsert(currentCell, insertNextLevelQueries);
				}
			}

		}

		if(test ==true &&coordinate==16777766){
			;;
			KeywordTrieCell index1 = ((KeywordTrieCell)ptp.get("casino"));
			KeywordTrieCell index2 = ((KeywordTrieCell)index1.trieCells.get("hotel"));
			KeywordTrieCell index3 =  ((KeywordTrieCell)index2.trieCells.get("las"));
			
			if(!index3.finalQueries.contains(testObject))
				System.out.println("There is an error here");
		}
		return null;
	}
	public boolean checkSpanForForceInsertFinal(MinimalRangeQuery query){
		double queryRange = query.spatialRange.getMax().X-query.spatialRange.getMin().X;
		double cellRange = bounds.getMax().X-bounds.getMin().X;
		if(queryRange>(cellRange/2) )
			return true;
		return false;
		
	}
	public boolean insertAtKeyWord(String keyword, MinimalRangeQuery query, ArrayList<MinimalRangeQuery> sharedQueries) {
		boolean inserted = false;
		if (!ptp.containsKey(keyword)) {
			FAST.numberOfHashEntries++;
			FAST.queryInsertInvListNodeCounter++;
			inserted = true;
			ptp.put(keyword, query);
			return inserted;
		} else { //this keyword already exists in the index
			Object keywordIndex = ptp.get(keyword);
			if (keywordIndex == null) {
				FAST.queryInsertInvListNodeCounter++;
				ptp.put(keyword, query);
				inserted = true;
				return inserted;
			}
			if (keywordIndex instanceof MinimalRangeQuery) { //single query 
				MinimalRangeQuery exitingQuery = (MinimalRangeQuery) keywordIndex;
				if (exitingQuery.expireTime > FAST.queryTimeStampCounter) { //checking for the support of the query
					ArrayList<MinimalRangeQuery> rareQueries = new ArrayList<MinimalRangeQuery>();
					FAST.queryInsertInvListNodeCounter++;
					rareQueries.add(exitingQuery);
					rareQueries.add(query);
					inserted = true;
					ptp.put(keyword, rareQueries);
					return inserted;
				} else {
					inserted = true;
					deleteQueryFromStats(exitingQuery);
					ptp.put(keyword, query);
					return inserted;
				}

			} else if ((keywordIndex instanceof ArrayList) && ((ArrayList<MinimalRangeQuery>) keywordIndex).size() < FAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
				if (((ArrayList<MinimalRangeQuery>) keywordIndex) != sharedQueries)
					if (!((ArrayList<MinimalRangeQuery>) keywordIndex).contains( query)) {
						((ArrayList<MinimalRangeQuery>) keywordIndex).add(query);
						FAST.queryInsertInvListNodeCounter++;
					}
				inserted = true;
				return inserted;
			} else if ((keywordIndex instanceof ArrayList) && ((ArrayList<MinimalRangeQuery>) keywordIndex).size() >= FAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
				if (((ArrayList<MinimalRangeQuery>) keywordIndex) != sharedQueries) {
					if (!((ArrayList<MinimalRangeQuery>) keywordIndex).contains( query)) {
						inserted = false;
						return inserted;
					} else {
						inserted = true;
						return inserted;
					}
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

	public void findQueriesToReinsert(KeywordTrieCell cell, ArrayList<ReinsertEntry> insertNextLevelQueries) {
		SpatialOverlapCompartor spatialOverlapCompartor = new SpatialOverlapCompartor(bounds);
		Collections.sort(cell.queries, spatialOverlapCompartor);
		int queriesSize =  cell.queries.size() ;
		for (int i = queriesSize - 1; i > queriesSize/2; i--) {
		//for (int i = queriesSize - 1; i >=0; i--) {
			MinimalRangeQuery query = cell.queries.remove(i);
			insertNextLevelQueries.add(new ReinsertEntry(SpatialHelper.spatialIntersect(bounds, query.spatialRange), query));
			if(query.queryId==debugQID)
				System.out.println("reinsert"+query+coordinate);
		}
	}

	//removal of expired entries;
	public boolean clean() {
		if (cleaningIterator == null || !cleaningIterator.hasNext())
			cleaningIterator = ptp.entrySet().iterator();
		Integer numberOfVisitedEntries = 0;
		while (cleaningIterator.hasNext() && numberOfVisitedEntries < FAST.MAX_ENTRIES_PER_CLEANING_INTERVAL) {
			Entry<String, Object> keywordIndexEntry = cleaningIterator.next();
			Object keywordIndex = keywordIndexEntry.getValue();
			String keyword = keywordIndexEntry.getKey();
			if (keywordIndex instanceof MinimalRangeQuery) {
				numberOfVisitedEntries++;
				if (((MinimalRangeQuery) keywordIndex).expireTime < FAST.queryTimeStampCounter)
					keywordIndex = null;
			} else if (keywordIndex instanceof ArrayList) {
				Iterator<MinimalRangeQuery> queriesItrator = ((ArrayList<MinimalRangeQuery>) keywordIndex).iterator();
				while (queriesItrator.hasNext()) {
					MinimalRangeQuery query = queriesItrator.next();
					if (query.expireTime < FAST.queryTimeStampCounter)
						queriesItrator.remove();
					numberOfVisitedEntries++;
				}
				if (((ArrayList<MinimalRangeQuery>) keywordIndex).size() == 0)
					keywordIndex = null;
				else if (((ArrayList<MinimalRangeQuery>) keywordIndex).size() == 1) {
					MinimalRangeQuery singleQuery = ((ArrayList<MinimalRangeQuery>) keywordIndex).get(0);
					ptp.put(keyword, singleQuery);

				}
			} else if (keywordIndex instanceof KeywordTrieCell) {

				ArrayList<MinimalRangeQuery> combinedQueries = new ArrayList<MinimalRangeQuery>();
				numberOfVisitedEntries += ((KeywordTrieCell) keywordIndex).clean(combinedQueries);
				if (((KeywordTrieCell) keywordIndex).queries == null && ((KeywordTrieCell) keywordIndex).trieCells == null
						&& FAST.overallQueryTextSummery.get(keyword).queryCount <= FAST.Trie_OVERLALL_MERGE_THRESHOLD)
					keywordIndex = null;
				else if (combinedQueries.size() < FAST.Trie_OVERLALL_MERGE_THRESHOLD
						&& FAST.overallQueryTextSummery.get(keyword).queryCount <= FAST.Trie_OVERLALL_MERGE_THRESHOLD)
					ptp.put(keyword, combinedQueries);
			}
			if (keywordIndex == null)
				cleaningIterator.remove();

		}
		if (cleaningIterator.hasNext())
			return false;
		else
			return true;

	}

	public ArrayList<String> getInternalSpatiotTextualOverlappingQueries(Point p, ArrayList<String> keywords, List<MinimalRangeQuery> finalQueries) {
		ArrayList<String> remainingKewords = new ArrayList<String>();
		for (int i = 0; i < keywords.size(); i++) {
			String keyword = keywords.get(i);
			Object keyWordIndex = ptp.get(keyword);
			FAST.objectSearchInvListHashAccess++;
			if (keyWordIndex != null) {
				if (keyWordIndex instanceof MinimalRangeQuery) {
					MinimalRangeQuery query = ((MinimalRangeQuery) keyWordIndex);
					if (keywords.size() >= query.queryText.size() && SpatialHelper.overlapsSpatially(p, query.getSpatialRange()) && TextHelpers.containsTextually(keywords, query.getQueryText())) {
						finalQueries.add(query);
					}
				} else if (keyWordIndex instanceof ArrayList) {
					ArrayList<MinimalRangeQuery> rareQueries = (ArrayList<MinimalRangeQuery>) keyWordIndex;
					for (MinimalRangeQuery q : rareQueries) {
						FAST.objectSearchInvListNodeCounter++;
						if (keywords.size() >= q.queryText.size() && SpatialHelper.overlapsSpatially(p, q.getSpatialRange()) && TextHelpers.containsTextually(keywords, q.getQueryText())) {
							finalQueries.add(q);
						}
					}
				} else if (keyWordIndex instanceof KeywordTrieCell) {
					remainingKewords.add(keyword);
				}
			}

		}
		for (int i = 0; i < remainingKewords.size(); i++) {
			String keyword = remainingKewords.get(i);
			Object keyWordIndex = ptp.get(keyword);
			FAST.totalTrieAccess++;
			((KeywordTrieCell) keyWordIndex).find(remainingKewords, i + 1, finalQueries, 0, p);
		}

		return remainingKewords;
	}

}

class SpatialOverlapCompartor implements Comparator<MinimalRangeQuery> {
	Rectangle bounds;

	public SpatialOverlapCompartor(Rectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public int compare(MinimalRangeQuery e1, MinimalRangeQuery e2) {
		double val1 = 0, val2 = 0;

		val1 = SpatialHelper.getArea(SpatialHelper.spatialIntersect(bounds, e1.spatialRange));

		val2 = SpatialHelper.getArea(SpatialHelper.spatialIntersect(bounds, e2.spatialRange));
		if (val1 < val2) {
			return 1;
		} else if (val1 == val2)
			return 0;
		else {
			return -1;
		}
	}
}
