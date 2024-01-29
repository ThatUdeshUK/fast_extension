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
package edu.purdue.cs.fast.baselines.fast;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs.fast.baselines.fast.helper.LSpatialHelper;
import edu.purdue.cs.fast.baselines.fast.helper.LTextHelpers;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.Rectangle;

public class LIndexCell {

	public ConcurrentHashMap<String, Object> ptp;//queries that fall only within the cells range
	Iterator<Entry<String, Object>> cleaningIterator;
	Rectangle bounds;
	int coordinate;
	boolean test;
	LMinimalRangeQuery testObject ;
	int level;
	int debugQID=-1;
	

	public LIndexCell(Rectangle bounds, Integer globalCoordinates, int level) {
		ptp = null;
		this.bounds = bounds;
		this.bounds.max.x -=.001;
		this.bounds.max.y -=.001;
		this.coordinate = globalCoordinates;
		test = false;
		this.level=level;
	}

	
	public void addInternalQuery(String keyword, LMinimalRangeQuery query, ArrayList<LMinimalRangeQuery> sharedQueries, ArrayList<LReinsertEntry>insertNextLevelQueries, boolean force) {
		if (ptp == null) {
			ptp = new ConcurrentHashMap<String, Object>();
		}
		if (!ptp.containsKey(keyword) && sharedQueries != null) {
			LFAST.numberOfHashEntries++;
			ptp.put(keyword, sharedQueries);
		} else {
			Object keywordIndex = ptp.get(keyword);
			if (keywordIndex instanceof LMinimalRangeQuery) {
				LMinimalRangeQuery exitingQuery = (LMinimalRangeQuery) keywordIndex;
				if (exitingQuery.et > LFAST.queryTimeStampCounter) { //checking for the support of the query
					if (sharedQueries.contains(exitingQuery)) {
						ptp.put(keyword, sharedQueries);
					} else if (sharedQueries.size() < LFAST.Trie_SPLIT_THRESHOLD) {
						LFAST.queryInsertInvListNodeCounter++;
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
				ArrayList<LMinimalRangeQuery> nonSharedQueries = new ArrayList<LMinimalRangeQuery>();
				for (LMinimalRangeQuery q : (ArrayList<LMinimalRangeQuery>) keywordIndex) {
					if (!sharedQueries.contains(q))
						nonSharedQueries.add(q);
				}
				if (nonSharedQueries.size() > 0 && nonSharedQueries.size() + sharedQueries.size() <=   LFAST.Trie_SPLIT_THRESHOLD) {
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

	public void deleteQueryFromStats(LMinimalRangeQuery query) {
		if (query.deleted == false) {
			query.deleted = true;
			for (String keyword : query.keywords) {
				LFAST.overallQueryTextSummery.get(keyword).queryCount--;
			}
		}
	}

	public Object addInternalQueryNoShare(String keyword, LMinimalRangeQuery query, ArrayList<LMinimalRangeQuery> sharedQueries, ArrayList<LReinsertEntry> insertNextLevelQueries, boolean force) {
		if (ptp == null) {
			ptp = new ConcurrentHashMap<String, Object>();
		}
		Queue<LMinimalRangeQuery> queue = new LinkedList<LMinimalRangeQuery>();
		boolean inserted = false;
		//step 1 find the best keyword to insert in:
		if(query.id ==debugQID ){
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
			if(query.id ==debugQID ){
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
				for (String term : query.keywords) {
					//mark all these keywords as tries
					if (ptp.get(term) instanceof ArrayList) {
						for(LMinimalRangeQuery q:(ArrayList<LMinimalRangeQuery>) ptp.get(term)){
							if(q.id == debugQID)
								System.out.println("Adding query to queue for reinsert at a different list: "+query+coordinate +"keyword "+term);
						}
						queue.addAll((ArrayList<LMinimalRangeQuery>) ptp.get(term));
						ptp.put(term, new LKeywordTrieCell());
						LFAST.numberOfTrieNodes++;
					}
				}
			}
			;
			LKeywordTrieCell currentCell = (LKeywordTrieCell) ptp.get(query.keywords.get(0));

			for (int j = 1; j < query.getKeywords().size() & !inserted; j++) {
				keyword = query.getKeywords().get(j);
				if (currentCell.trieCells == null)
					currentCell.trieCells = new HashMap<String, Object>();

				Object cell = currentCell.trieCells.get(keyword);
				if (cell == null) {
					currentCell.trieCells.put(keyword, query);
					inserted = true;
				} else if (cell instanceof LMinimalRangeQuery) {
//					if (((LMinimalRangeQuery) cell).id == 31) {
//						System.out.println("DEBUG");
//					}
					if (((LMinimalRangeQuery) cell).et > LFAST.queryTimeStampCounter) {
						ArrayList<LMinimalRangeQuery> queries = new ArrayList<LMinimalRangeQuery>();
						queries.add((LMinimalRangeQuery) cell);
						queries.add((LMinimalRangeQuery) query);
						currentCell.trieCells.put(keyword, queries);
						inserted = true;
					} else {
						deleteQueryFromStats((LMinimalRangeQuery) cell);
						currentCell.trieCells.put(keyword, query);
						inserted = true;
					}
				} else if (cell instanceof ArrayList && ((ArrayList<LMinimalRangeQuery>) cell).size() <= LFAST.Trie_SPLIT_THRESHOLD) {
					((ArrayList<LMinimalRangeQuery>) cell).add(query);
					inserted = true;
				} else if (cell instanceof ArrayList && ((ArrayList<LMinimalRangeQuery>) cell).size() > LFAST.Trie_SPLIT_THRESHOLD) {
					LKeywordTrieCell newCell = new LKeywordTrieCell();
					LFAST.numberOfTrieNodes++;
					((ArrayList<LMinimalRangeQuery>) cell).add(query);
					newCell.trieCells = new HashMap<String, Object>();
					newCell.queries = new ArrayList<LMinimalRangeQuery>();
					currentCell.trieCells.put(keyword, newCell);
					for (LMinimalRangeQuery otherQuery : ((ArrayList<LMinimalRangeQuery>) cell)) {
						if (otherQuery.et > LFAST.queryTimeStampCounter) {
							if (otherQuery.getKeywords().size() > (j + 1)) {

								Object otherCell = newCell.trieCells.get(otherQuery.getKeywords().get(j + 1));
								if (otherCell == null) {
									otherCell = new ArrayList<LMinimalRangeQuery>();
								}
								((ArrayList<LMinimalRangeQuery>) otherCell).add(otherQuery);
								newCell.trieCells.put(otherQuery.getKeywords().get(j + 1), otherCell);
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
				} else if (cell instanceof LKeywordTrieCell) {
					if (j < (query.getKeywords().size() - 1)) {
						currentCell = (LKeywordTrieCell) cell;
					} else {
						if (level==0||checkSpanForForceInsertFinal(query)) {
							if (((LKeywordTrieCell) cell).finalQueries == null)
								((LKeywordTrieCell) cell).finalQueries = new ArrayList<LMinimalRangeQuery>();
							((LKeywordTrieCell) cell).finalQueries.add(query);
							if(query.id ==debugQID){
								System.out.println("final"+query+coordinate);
								test = true;
								testObject = query;
							}
						} else {

							if (((LKeywordTrieCell) cell).queries == null)
								((LKeywordTrieCell) cell).queries = new ArrayList<LMinimalRangeQuery>();
							((LKeywordTrieCell) cell).queries.add(query);
							if(query.id ==debugQID)
								System.out.println("Not final"+query+coordinate);
							if (((LKeywordTrieCell) cell).queries.size() >  LFAST.Degredation_Ratio) {
								findQueriesToReinsert((LKeywordTrieCell) cell, insertNextLevelQueries);
							}
						}
						inserted = true;
					}

				}

			}
			if (!inserted) {
				if ((level==0||checkSpanForForceInsertFinal(query)) ) {
					if (currentCell.finalQueries == null)
						currentCell.finalQueries = new ArrayList<LMinimalRangeQuery>();
					currentCell.finalQueries.add(query);
					if(query.id ==debugQID)
						System.out.println("final"+query+coordinate);

				} else {
					if (currentCell.queries == null)
						currentCell.queries = new ArrayList<LMinimalRangeQuery>();
					currentCell.queries.add(query);
					if(query.id ==debugQID)
						System.out.println("Not final"+query+coordinate);
					if (currentCell.queries.size() > LFAST.Degredation_Ratio)
						findQueriesToReinsert(currentCell, insertNextLevelQueries);
				}
			}

		}

		if(test ==true &&coordinate==16777766){
			;;
			LKeywordTrieCell index1 = ((LKeywordTrieCell)ptp.get("casino"));
			LKeywordTrieCell index2 = ((LKeywordTrieCell)index1.trieCells.get("hotel"));
			LKeywordTrieCell index3 =  ((LKeywordTrieCell)index2.trieCells.get("las"));
			
			if(!index3.finalQueries.contains(testObject))
				System.out.println("There is an error here");
		}
		return null;
	}
	public boolean checkSpanForForceInsertFinal(LMinimalRangeQuery query){
		double queryRange = query.spatialBox().max.x -query.spatialBox().min.x;
		double cellRange = bounds.max.x -bounds.min.x;
		if(queryRange>(cellRange/2) )
			return true;
		return false;
		
	}
	public boolean insertAtKeyWord(String keyword, LMinimalRangeQuery query, ArrayList<LMinimalRangeQuery> sharedQueries) {
		boolean inserted = false;
		if (!ptp.containsKey(keyword)) {
			LFAST.numberOfHashEntries++;
			LFAST.queryInsertInvListNodeCounter++;
			inserted = true;
			ptp.put(keyword, query);
			return inserted;
		} else { //this keyword already exists in the index
			Object keywordIndex = ptp.get(keyword);
			if (keywordIndex == null) {
				LFAST.queryInsertInvListNodeCounter++;
				ptp.put(keyword, query);
				inserted = true;
				return inserted;
			}
			if (keywordIndex instanceof LMinimalRangeQuery) { //single query
				LMinimalRangeQuery exitingQuery = (LMinimalRangeQuery) keywordIndex;
//				if (exitingQuery.id == 31) {
//					System.out.println("Debug");
//				}
				if (exitingQuery.et > LFAST.queryTimeStampCounter) { //checking for the support of the query
					ArrayList<LMinimalRangeQuery> rareQueries = new ArrayList<LMinimalRangeQuery>();
					LFAST.queryInsertInvListNodeCounter++;
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

			} else if ((keywordIndex instanceof ArrayList) && ((ArrayList<LMinimalRangeQuery>) keywordIndex).size() < LFAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
				if (((ArrayList<LMinimalRangeQuery>) keywordIndex) != sharedQueries)
					if (!((ArrayList<LMinimalRangeQuery>) keywordIndex).contains( query)) {
						((ArrayList<LMinimalRangeQuery>) keywordIndex).add(query);
						LFAST.queryInsertInvListNodeCounter++;
					}
				inserted = true;
				return inserted;
			} else if ((keywordIndex instanceof ArrayList) && ((ArrayList<LMinimalRangeQuery>) keywordIndex).size() >= LFAST.Trie_SPLIT_THRESHOLD) { // this keyword is rare
				if (((ArrayList<LMinimalRangeQuery>) keywordIndex) != sharedQueries) {
					if (!((ArrayList<LMinimalRangeQuery>) keywordIndex).contains( query)) {
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
			} else if (keywordIndex instanceof LKeywordTrieCell) {
				inserted = false;
				return inserted;
			} else {
				System.err.println("This is an error you should never be here");
			}
		}
		return false;
	}

	public String getOtherKeywordToInser(LMinimalRangeQuery query) {
		int minSize = Integer.MAX_VALUE;
		String minKeyword = null;
		for (String term : query.keywords) {
			if (!ptp.containsKey(term)) {
				int size = 0;
				if (size < minSize) {
					minSize = size;
					minKeyword = term;
				}

			} else if (ptp.containsKey(term) && ptp.get(term) instanceof LMinimalRangeQuery) {
				int size = 1;
				if (size < minSize) {
					minSize = size;
					minKeyword = term;
				}

			} else if (ptp.containsKey(term) && ptp.get(term) instanceof ArrayList) {
				int size = ((ArrayList<LMinimalRangeQuery>) ptp.get(term)).size();
				if (size < minSize) {
					minSize = size;
					minKeyword = term;
				}
			}

		}
		return minKeyword;
	}

	public void findQueriesToReinsert(LKeywordTrieCell cell, ArrayList<LReinsertEntry> insertNextLevelQueries) {
		LSpatialOverlapCompartor spatialOverlapCompartor = new LSpatialOverlapCompartor(bounds);
		Collections.sort(cell.queries, spatialOverlapCompartor);
		int queriesSize =  cell.queries.size() ;
		for (int i = queriesSize - 1; i > queriesSize/2; i--) {
		//for (int i = queriesSize - 1; i >=0; i--) {
			LMinimalRangeQuery query = cell.queries.remove(i);
			insertNextLevelQueries.add(new LReinsertEntry(LSpatialHelper.spatialIntersect(bounds, query.spatialBox()), query));
			if(query.id ==debugQID)
				System.out.println("reinsert"+query+coordinate);
		}
	}

	//removal of expired entries;
	public boolean clean() {
		if (cleaningIterator == null || !cleaningIterator.hasNext())
			cleaningIterator = ptp.entrySet().iterator();
		Integer numberOfVisitedEntries = 0;
		while (cleaningIterator.hasNext() && numberOfVisitedEntries < LFAST.MAX_ENTRIES_PER_CLEANING_INTERVAL) {
			Entry<String, Object> keywordIndexEntry = cleaningIterator.next();
			Object keywordIndex = keywordIndexEntry.getValue();
			String keyword = keywordIndexEntry.getKey();
			if (keywordIndex instanceof LMinimalRangeQuery) {
				numberOfVisitedEntries++;
				if (((LMinimalRangeQuery) keywordIndex).et < LFAST.queryTimeStampCounter)
					keywordIndex = null;
			} else if (keywordIndex instanceof ArrayList) {
				Iterator<LMinimalRangeQuery> queriesItrator = ((ArrayList<LMinimalRangeQuery>) keywordIndex).iterator();
				while (queriesItrator.hasNext()) {
					LMinimalRangeQuery query = queriesItrator.next();
					if (query.et < LFAST.queryTimeStampCounter)
						queriesItrator.remove();
					numberOfVisitedEntries++;
				}
				if (((ArrayList<LMinimalRangeQuery>) keywordIndex).size() == 0)
					keywordIndex = null;
				else if (((ArrayList<LMinimalRangeQuery>) keywordIndex).size() == 1) {
					LMinimalRangeQuery singleQuery = ((ArrayList<LMinimalRangeQuery>) keywordIndex).get(0);
					ptp.put(keyword, singleQuery);

				}
			} else if (keywordIndex instanceof LKeywordTrieCell) {

				ArrayList<LMinimalRangeQuery> combinedQueries = new ArrayList<LMinimalRangeQuery>();
				numberOfVisitedEntries += ((LKeywordTrieCell) keywordIndex).clean(combinedQueries);
				if (((LKeywordTrieCell) keywordIndex).queries == null && ((LKeywordTrieCell) keywordIndex).trieCells == null
						&& LFAST.overallQueryTextSummery.get(keyword).queryCount <= LFAST.Trie_OVERLALL_MERGE_THRESHOLD)
					keywordIndex = null;
				else if (combinedQueries.size() < LFAST.Trie_OVERLALL_MERGE_THRESHOLD
						&& LFAST.overallQueryTextSummery.get(keyword).queryCount <= LFAST.Trie_OVERLALL_MERGE_THRESHOLD)
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

	public ArrayList<String> getInternalSpatiotTextualOverlappingQueries(Point p, List<String> keywords, List<Query> finalQueries) {
		ArrayList<String> remainingKewords = new ArrayList<String>();
		for (int i = 0; i < keywords.size(); i++) {
			String keyword = keywords.get(i);
			Object keyWordIndex = ptp.get(keyword);
			LFAST.objectSearchInvListHashAccess++;
			if (keyWordIndex != null) {
				if (keyWordIndex instanceof LMinimalRangeQuery) {
					LMinimalRangeQuery query = ((LMinimalRangeQuery) keyWordIndex);
					if (keywords.size() >= query.keywords.size() && LSpatialHelper.overlapsSpatially(p, query.getSpatialRange()) && LTextHelpers.containsTextually(keywords, query.getKeywords())) {
						finalQueries.add(query);
					}
				} else if (keyWordIndex instanceof ArrayList) {
					ArrayList<LMinimalRangeQuery> rareQueries = (ArrayList<LMinimalRangeQuery>) keyWordIndex;
					for (LMinimalRangeQuery q : rareQueries) {
						LFAST.objectSearchInvListNodeCounter++;
						if (keywords.size() >= q.keywords.size() && LSpatialHelper.overlapsSpatially(p, q.getSpatialRange()) && LTextHelpers.containsTextually(keywords, q.getKeywords())) {
							finalQueries.add(q);
						}
					}
				} else if (keyWordIndex instanceof LKeywordTrieCell) {
					remainingKewords.add(keyword);
				}
			}

		}
		for (int i = 0; i < remainingKewords.size(); i++) {
			String keyword = remainingKewords.get(i);
			Object keyWordIndex = ptp.get(keyword);
			LFAST.totalTrieAccess++;
			((LKeywordTrieCell) keyWordIndex).find(remainingKewords, i + 1, finalQueries, 0, p);
		}

		return remainingKewords;
	}

}

class LSpatialOverlapCompartor implements Comparator<LMinimalRangeQuery> {
	Rectangle bounds;

	public LSpatialOverlapCompartor(Rectangle bounds) {
		this.bounds = bounds;
	}

	@Override
	public int compare(LMinimalRangeQuery e1, LMinimalRangeQuery e2) {
		double val1 = 0, val2 = 0;

		val1 = LSpatialHelper.getArea(LSpatialHelper.spatialIntersect(bounds, e1.spatialBox()));

		val2 = LSpatialHelper.getArea(LSpatialHelper.spatialIntersect(bounds, e2.spatialBox()));
		if (val1 < val2) {
			return 1;
		} else if (val1 == val2)
			return 0;
		else {
			return -1;
		}
	}
}
