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
 * 
 * 
 * This version is meant for the fair comparison of with the state of the art
 *  index that does not have any other cluster and tornado related attributes
 */
package edu.purdue.cs.fast.baselines.fast;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.fast.helper.LSpatialHelper;
import edu.purdue.cs.fast.baselines.fast.helper.LSpatioTextualConstants;
import edu.purdue.cs.fast.baselines.fast.messages.LDataObject;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.Rectangle;

public class LFAST implements SpatialKeywordIndex<Query, LDataObject> {
	public double localXstep;
	public double localYstep;
	public ConcurrentHashMap<Integer, LIndexCell> index;
	public Rectangle selfBounds;
	public int gridGranularity;
	public int maxLevel;
	public static int Trie_SPLIT_THRESHOLD = 2;
	public static int Degredation_Ratio = 2;
	public static int Trie_OVERLALL_MERGE_THRESHOLD = 2;
	public static int CLEANING_INTERVAL = 1000;
	public static int NUMBER_OF_ACTIVE_QUERIES = 1000000;
	public static int MAX_ENTRIES_PER_CLEANING_INTERVAL = 10;
	public LIndexCell cellBeingCleaned;
	public boolean lastCellCleaningDone; //to check if an entireCellHasBeenCleaned

	public static HashMap<String, LKeywordFrequencyStats> overallQueryTextSummery;
	public static int totalVisited = 0;
	public static int spatialOverlappingQuries = 0;
	public int minInsertedLevel;
	public int maxInsertedLevel;
	public int minInsertedLevelInterleaved;
	public int maxInsertedLevelInterleaved;
	public static int queryTimeStampCounter;
	public static int objectTimeStampCounter;
	public static int debugQueryId =-1;
	
	public static int queryInsertInvListNodeCounter = 0;
	public static int queryInsertTrieNodeCounter = 0;
	public static int totalQueryInsertionsIncludingReplications = 0;
	public static int objectSearchInvListNodeCounter = 0;
	public static int objectSearchTrieNodeCounter = 0;
	public static int objectSearchInvListHashAccess = 0;
	public static int objectSearchTrieHashAccess = 0;
	public static int numberOfHashEntries = 0;
	public static int numberOfTrieNodes = 0;
	public static int totalTrieAccess = 0;
	public static int objectSearchTrieFinalNodeCounter=0;
	public Iterator<Entry<Integer, LIndexCell>> cleaningIterator;//iterates over cells to clean expired entries

	public LFAST(Rectangle selfBounds, Integer xGridGranularity, Integer maxLevel) {
		
		this.selfBounds = selfBounds;
		Double globalXrange = LSpatioTextualConstants.xMaxRange;
		Double globalYrange = LSpatioTextualConstants.yMaxRange;
		this.gridGranularity = xGridGranularity;
		this.localXstep = (globalXrange / this.gridGranularity);
		this.localYstep = (globalYrange / this.gridGranularity);
		this.maxLevel = Math.min((int) (Math.log(gridGranularity) / Math.log(2)), maxLevel);
		this.minInsertedLevel = -1;
		this.maxInsertedLevel = -1;
		this.minInsertedLevelInterleaved = -1;
		this.maxInsertedLevelInterleaved = -1;
		index = new ConcurrentHashMap<Integer, LIndexCell>();
		overallQueryTextSummery = new HashMap<String, LKeywordFrequencyStats>();

		queryInsertInvListNodeCounter = 0;
		queryInsertTrieNodeCounter = 0;
		totalQueryInsertionsIncludingReplications = 0;
		objectSearchInvListNodeCounter = 0;
		objectSearchTrieNodeCounter = 0;
		objectSearchTrieFinalNodeCounter=0;
		objectSearchInvListHashAccess = 0;
		objectSearchTrieHashAccess = 0;
		numberOfHashEntries = 0;
		numberOfTrieNodes = 0;
		totalTrieAccess = 0;
		queryTimeStampCounter = 0;
		objectTimeStampCounter = 0;
		cleaningIterator = null;
		cellBeingCleaned = null;
		lastCellCleaningDone = true;
	}

	public Integer mapToRawMajor(int level, int x, int y, int gridGranularity) {
		return (level << 22) + y * gridGranularity + x;
	}

	public double getAverageRankedInvListSize() {
		Double sum = 0.0, count = 0.0;
		ConcurrentHashMap<Integer, LIndexCell> levelIndex = index;
		Iterator itr = levelIndex.values().iterator();
		while (itr.hasNext()) {
			LIndexCell cell = (LIndexCell) itr.next();
			if (cell.ptp != null) {
				Iterator itr2 = cell.ptp.values().iterator();
				while (itr2.hasNext()) {
					Object queries = (Object) itr2.next();
					if (queries != null && queries instanceof ArrayList && ((ArrayList<LMinimalRangeQuery>) queries).size() > 0) {
						count++;
						sum += ((ArrayList<LMinimalRangeQuery>) queries).size();
					}
					if (queries != null && queries instanceof LKeywordTrieCell && ((LKeywordTrieCell) queries).queries != null && ((LKeywordTrieCell) queries).queries.size() > 0) {
						count++;
						sum += ((LKeywordTrieCell) queries).queries.size();
					}
				}
			}
		}
		return sum / count;
	}

	@Override
	public Collection<LDataObject> insertQuery(Query query) {
		addContinousQuery((LMinimalRangeQuery) query);
		return null;
	}

	public Boolean addContinousQuery(LMinimalRangeQuery q) {
		queryTimeStampCounter++;
		Boolean completed = true;

		int level = maxLevel;
		if(q.id ==debugQueryId)
			System.out.println("First insert"+q);
		ArrayList<LReinsertEntry> currentLevelQueries = new ArrayList<LReinsertEntry>();
		currentLevelQueries.add(new LReinsertEntry(q.spatialBox(), q));
		while (level >= 0 && currentLevelQueries.size() > 0) {
			ArrayList<LReinsertEntry>insertNextLevelQueries = new ArrayList<LReinsertEntry>();
			int levelGranuality = (int) (gridGranularity / Math.pow(2, level));
			double levelStep = (LSpatioTextualConstants.xMaxRange / levelGranuality);
			for (LReinsertEntry entry : currentLevelQueries) {
//				if (entry.query.id == 31) {
//					System.out.println("DEBUG!");
//					System.out.println("Inserting q: " + q + " at: " + level);
//				}

				int levelxMinCell = (int) (entry.query.getSpatialRange().min.x / levelStep);
				int levelyMinCell = (int) (entry.query.getSpatialRange().min.y / levelStep);
				int levelxMaxCell = (int) (entry.query.getSpatialRange().max.x / levelStep);
				int levelyMaxCell = (int) (entry.query.getSpatialRange().max.y / levelStep);
				int span = Math.max(levelxMaxCell - levelxMinCell, levelyMaxCell - levelyMinCell);

				if (entry.range != null) {
					if(entry.query.id ==debugQueryId)
						System.out.println("reinsert"+entry.query);

					levelxMinCell = (int) (entry.range.min.x / levelStep);
					levelyMinCell = (int) (entry.range.min.y / levelStep);
					levelxMaxCell = (int) (entry.range.max.x / levelStep);
					levelyMaxCell = (int) (entry.range.max.y / levelStep);
				}

				if (minInsertedLevel == -1)
					minInsertedLevel = maxInsertedLevel = level;
				if (level < minInsertedLevel)
					minInsertedLevel = level;
				if (level > maxInsertedLevel)
					maxInsertedLevel = level;
				String minkeyword = null;
				int minCount = Integer.MAX_VALUE;

				for (String keyword : entry.query.getKeywords()) {
					LKeywordFrequencyStats stats = overallQueryTextSummery.get(keyword);
					if (stats == null) {
						stats = new LKeywordFrequencyStats(1, 1, objectTimeStampCounter);
						overallQueryTextSummery.put(keyword, stats);
						minkeyword = keyword;
						minCount = 0;
					} else {
						if (level == maxLevel)
							stats.queryCount++;
						if (stats.queryCount < minCount) {

							minCount = stats.queryCount;
							minkeyword = keyword;
						}
					}
				}

				Integer coodinate = 0;
				ArrayList<LMinimalRangeQuery> sharedQueries = null;
				boolean forceInsert = false;
				if (level == 0)
					forceInsert = true;
				else if (span > 0)
					forceInsert = true;
				else
					forceInsert = false;
				for (Integer i = levelxMinCell; i <= levelxMaxCell; i++) {
					for (Integer j = levelyMinCell; j <= levelyMaxCell; j++) {
						totalQueryInsertionsIncludingReplications++;
						coodinate = mapToRawMajor(level, i, j, levelGranuality);
						if (!index.containsKey(coodinate)) {
							Rectangle bounds = getBoundForIndexCell(i, j, levelStep);
							if (bounds.min.x >= LSpatioTextualConstants.xMaxRange || bounds.min.y >= LSpatioTextualConstants.yMaxRange)
								continue;
							index.put(coodinate, new LIndexCell(bounds, coodinate, level));
						}
						LIndexCell indexCell = index.get(coodinate);
						if (LSpatialHelper.overlapsSpatially(entry.query.spatialBox(), indexCell.bounds)) {
							if (i == levelxMinCell && j == levelyMinCell) {
								sharedQueries = (ArrayList<LMinimalRangeQuery>) indexCell.addInternalQueryNoShare(minkeyword, entry.query, null, insertNextLevelQueries, forceInsert);
							} else if (sharedQueries != null && sharedQueries instanceof ArrayList) {
								indexCell.addInternalQuery(minkeyword, entry.query, sharedQueries, insertNextLevelQueries, forceInsert);
							} else
								indexCell.addInternalQueryNoShare(minkeyword, entry.query, null, insertNextLevelQueries, forceInsert);
						}
						if (indexCell.ptp == null) {
							index.remove(coodinate);
						}
//						if (level == 9) {
//							System.out.println("Attorney state: " + indexCell.ptp.get("attorney"));
//						}
					}
				}
			}

			currentLevelQueries = insertNextLevelQueries;
			level--;
		}
//		if (queryTimeStampCounter % CLEANING_INTERVAL == 0)
//			cleanNextSetOfEntries();
		return completed;
	}

	public void cleanNextSetOfEntries() {
		if (cleaningIterator == null || !cleaningIterator.hasNext())
			cleaningIterator = index.entrySet().iterator();
		LIndexCell cell;
		if (lastCellCleaningDone)
			cell = cleaningIterator.next().getValue();
		else
			cell = cellBeingCleaned;
		boolean cleaningDone = cell.clean();
		if (!cleaningDone) {
			cellBeingCleaned = cell;
		}
		if (cell.ptp == null)
			cleaningIterator.remove();

	}

	Rectangle getBoundForIndexCell(int i, int j, double step) {
		Rectangle bounds = new Rectangle(new Point(i * step, j * step), new Point((i + 1) * step, (j + 1) * step));
		return bounds;
	}

	public Integer getCountPerKeywrodsAll(ArrayList<String> keywords) {

		return 0;
	}

	public Rectangle getSelfBounds() {
		return selfBounds;
	}

	public Integer mapDataPointToPartition(int level, Point point, double step, int granularity) {
		Double x = point.x;
		Double y = point.y;
		Integer xCell = (int) ((x) / step);
		Integer yCell = (int) ((y) / step);
		Integer indexCellCoordinate = mapToRawMajor(level, xCell, yCell, granularity);
		return indexCellCoordinate;
	}

	public void setSelfBounds(Rectangle selfBounds) {
		this.selfBounds = selfBounds;
	}

	@Override
	public Collection<Query> insertObject(LDataObject dataObject) {
		return getReleventSpatialKeywordRangeQueries(dataObject, false);
	}

	public List<Query> getReleventSpatialKeywordRangeQueries(LDataObject dataObject, Boolean fromNeighbour) {
//		if (dataObject.id == 2) {
//			System.out.println("DEBUG!");
//			System.out.println("LFAST obj:" + dataObject);
//		}
		objectTimeStampCounter++;
		List<Query> result = new LinkedList<>();
		if (minInsertedLevel == -1)
			return result;
		double step = (maxInsertedLevel == 0) ? localXstep : (localXstep * (2 << (maxInsertedLevel - 1)));
		int granualrity = this.gridGranularity >> maxInsertedLevel;
		List<String> keywords = dataObject.getKeywords();
		for (int level = maxInsertedLevel; level >= minInsertedLevel && keywords != null && keywords.size() > 0; level--) {
			Integer cellCoordinates = mapDataPointToPartition(level, dataObject.getLocation(), step, granualrity);
			LIndexCell indexCellOptimized = index.get(cellCoordinates);
			if (indexCellOptimized != null) {
				keywords = indexCellOptimized.getInternalSpatiotTextualOverlappingQueries(dataObject.location, keywords, result);
			}
			step /= 2;
			granualrity <<= 1;
		}

//		if (dataObject.id == 2) {
//			for (LMinimalRangeQuery query : result) {
//				System.out.println("LFAST: " + query);
//			}
//		}
		return result;
	}
}
