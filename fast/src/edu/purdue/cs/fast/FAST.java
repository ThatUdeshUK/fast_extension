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
package edu.purdue.cs.fast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.messages.DataObject;
import edu.purdue.cs.fast.messages.MinimalRangeQuery;

public class FAST {
	public double localXstep;
	public double localYstep;
	public ConcurrentHashMap<Integer, IndexCell> index;
	public Rectangle selfBounds;
	public int gridGranularity;
	public int maxLevel;
	public static int Trie_SPLIT_THRESHOLD = 2;
	public static int Degredation_Ratio = 2;
	public static int Trie_OVERLALL_MERGE_THRESHOLD = 2;
	public static int CLEANING_INTERVAL = 1000;
	public static int NUMBER_OF_ACTIVE_QUERIES = 1000000;
	public static int MAX_ENTRIES_PER_CLEANING_INTERVAL = 10;
	public IndexCell cellBeingCleaned;
	public boolean lastCellCleaningDone; //to check if an entireCellHasBeenCleaned

	public static HashMap<String, KeywordFrequencyStats> overallQueryTextSummery;
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
	public static int splitThreshold ;
	public Iterator<Entry<Integer, IndexCell>> cleaningIterator;//iterates over cells to clean expired entries

	public FAST(Rectangle selfBounds, Integer xGridGranularity, Integer maxLevel) {
		
		this.selfBounds = selfBounds;
		Double globalXrange = SpatioTextualConstants.xMaxRange;
		Double globalYrange = SpatioTextualConstants.yMaxRange;
		this.gridGranularity = xGridGranularity;
		this.localXstep = (globalXrange / this.gridGranularity);
		this.localYstep = (globalYrange / this.gridGranularity);
		this.maxLevel = Math.min((int) (Math.log(gridGranularity) / Math.log(2)), maxLevel);
		this.minInsertedLevel = -1;
		this.maxInsertedLevel = -1;
		this.minInsertedLevelInterleaved = -1;
		this.maxInsertedLevelInterleaved = -1;
		index = new ConcurrentHashMap<Integer, IndexCell>();
		overallQueryTextSummery = new HashMap<String, KeywordFrequencyStats>();

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

	public FAST(Rectangle selfBounds, Integer xGridGranularity, Integer maxLevel, Integer splitThreshold) {
		
		this.splitThreshold = splitThreshold;

	}

	public Integer mapToRawMajor(int level, int x, int y, int gridGranularity) {
		return (level << 22) + y * gridGranularity + x;
	}

	public double getAverageRankedInvListSize() {
		Double sum = 0.0, count = 0.0;
		ConcurrentHashMap<Integer, IndexCell> levelIndex = index;
		Iterator itr = levelIndex.values().iterator();
		while (itr.hasNext()) {
			IndexCell cell = (IndexCell) itr.next();
			if (cell.ptp != null) {
				Iterator itr2 = cell.ptp.values().iterator();
				while (itr2.hasNext()) {
					Object queries = (Object) itr2.next();
					if (queries != null && queries instanceof ArrayList && ((ArrayList<MinimalRangeQuery>) queries).size() > 0) {
						count++;
						sum += ((ArrayList<MinimalRangeQuery>) queries).size();
					}
					if (queries != null && queries instanceof KeywordTrieCell && ((KeywordTrieCell) queries).queries != null && ((KeywordTrieCell) queries).queries.size() > 0) {
						count++;
						sum += ((KeywordTrieCell) queries).queries.size();
					}
				}
			}
		}
		return sum / count;

	}

	public Boolean addContinousQuery(MinimalRangeQuery q) {
		queryTimeStampCounter++;

		Boolean completed = true;

		int level = maxLevel;
		if(q.queryId==debugQueryId)
			System.out.println("First insert"+q);
		ArrayList<ReinsertEntry> currentLevelQueries = new ArrayList<ReinsertEntry>();
		currentLevelQueries.add(new ReinsertEntry(q.spatialRange, q));
		while (level >= 0 && currentLevelQueries.size() > 0) {
			ArrayList<ReinsertEntry>insertNextLevelQueries = new ArrayList<ReinsertEntry>();
			int levelGranuality = (int) (gridGranularity / Math.pow(2, level));
			double levelStep = (SpatioTextualConstants.xMaxRange / levelGranuality);
			for (ReinsertEntry entry : currentLevelQueries) {

				int levelxMinCell = (int) (entry.query.getSpatialRange().getMin().getX() / levelStep);
				int levelyMinCell = (int) (entry.query.getSpatialRange().getMin().getY() / levelStep);
				int levelxMaxCell = (int) (entry.query.getSpatialRange().getMax().getX() / levelStep);
				int levelyMaxCell = (int) (entry.query.getSpatialRange().getMax().getY() / levelStep);
				int span = Math.max(levelxMaxCell - levelxMinCell, levelyMaxCell - levelyMinCell);

				if (entry.range != null) {
					if(entry.query.queryId==debugQueryId)
						System.out.println("reinsert"+entry.query);

					levelxMinCell = (int) (entry.range.getMin().getX() / levelStep);
					levelyMinCell = (int) (entry.range.getMin().getY() / levelStep);
					levelxMaxCell = (int) (entry.range.getMax().getX() / levelStep);
					levelyMaxCell = (int) (entry.range.getMax().getY() / levelStep);
				}

				if (minInsertedLevel == -1)
					minInsertedLevel = maxInsertedLevel = level;
				if (level < minInsertedLevel)
					minInsertedLevel = level;
				if (level > maxInsertedLevel)
					maxInsertedLevel = level;
				String minkeyword = null;
				int minCount = Integer.MAX_VALUE;

				for (String keyword : entry.query.getQueryText()) {
					KeywordFrequencyStats stats = overallQueryTextSummery.get(keyword);
					if (stats == null) {
						stats = new KeywordFrequencyStats(1, 1, objectTimeStampCounter);
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
				ArrayList<MinimalRangeQuery> sharedQueries = null;
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
							if (bounds.getMin().getX() >= SpatioTextualConstants.xMaxRange || bounds.getMin().getY() >= SpatioTextualConstants.yMaxRange)
								continue;
							index.put(coodinate, new IndexCell(bounds, coodinate, level));
						}
						IndexCell indexCell = index.get(coodinate);
						if (SpatialHelper.overlapsSpatially(entry.query.spatialRange, indexCell.bounds)) {
							if (i == levelxMinCell && j == levelyMinCell) {
								sharedQueries = (ArrayList<MinimalRangeQuery>) indexCell.addInternalQueryNoShare(minkeyword, entry.query, null, insertNextLevelQueries, forceInsert);
							} else if (sharedQueries != null && sharedQueries instanceof ArrayList) {
								indexCell.addInternalQuery(minkeyword, entry.query, sharedQueries, insertNextLevelQueries, forceInsert);
							} else
								indexCell.addInternalQueryNoShare(minkeyword, entry.query, null, insertNextLevelQueries, forceInsert);
						}
						if (indexCell.ptp == null) {
							index.remove(coodinate);
						}

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
		IndexCell cell;
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
		Double x = point.getX();
		Double y = point.getY();
		Integer xCell = (int) ((x) / step);
		Integer yCell = (int) ((y) / step);
		Integer indexCellCoordinate = mapToRawMajor(level, xCell, yCell, granularity);
		return indexCellCoordinate;
	}

	public void setSelfBounds(Rectangle selfBounds) {
		this.selfBounds = selfBounds;
	}

	public List<MinimalRangeQuery> getReleventSpatialKeywordRangeQueries(DataObject dataObject, Boolean fromNeighbour) {
		objectTimeStampCounter++;
		List<MinimalRangeQuery> result = new LinkedList<MinimalRangeQuery>();
		if (minInsertedLevel == -1)
			return result;
		double step = (maxInsertedLevel == 0) ? localXstep : (localXstep * (2 << (maxInsertedLevel - 1)));
		int granualrity = this.gridGranularity >> maxInsertedLevel;
		ArrayList<String> keywords = dataObject.getObjectText();
		for (int level = maxInsertedLevel; level >= minInsertedLevel && keywords != null && keywords.size() > 0; level--) {
			Integer cellCoordinates = mapDataPointToPartition(level, dataObject.getLocation(), step, granualrity);
			IndexCell indexCellOptimized = index.get(cellCoordinates);
			if (indexCellOptimized != null) {
				keywords = indexCellOptimized.getInternalSpatiotTextualOverlappingQueries(dataObject.getLocation(), keywords, result);
			}
			step /= 2;
			granualrity <<= 1;
		}

		return result;
	}

}
