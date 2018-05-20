package edu.purdue.cs.fast.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.KeyWordTrieIndexMinimal;
import edu.purdue.cs.fast.KeywordFrequencyStats;
import edu.purdue.cs.fast.helper.Command;
import edu.purdue.cs.fast.helper.LatLong;
import edu.purdue.cs.fast.helper.ObjectSizeCalculator;
import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.QueryType;
import edu.purdue.cs.fast.helper.RandomGenerator;
import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.helper.TextualPredicate;
import edu.purdue.cs.fast.messages.DataObject;
import edu.purdue.cs.fast.messages.MinimalRangeQuery;
import edu.purdue.cs.fast.messages.Query;
import edu.purdue.cs.fast.readers.QueriesReader;


/**
 * This class tests the global and local index performance and test the value of
 * partitioning
 * 
 * @author ahmed
 *
 */
public class Test {
	static Integer countId = 0;
	static Integer queryCountId = 0;
	static Integer finegGridGran = 64;
	public static final String vocabPath = "C:\\datasets\\synthetic\\vocab.csv";
	public static final String tweetsQueriesPath = "C:\\datasets\\tweets\\tweetsForQueries.csv";
	public static final String tweetsObjectsPath = "C:\\datasets\\tweets\\sampletweets.csv";
	public static final String spatiallyUniformTweetsQueriesPath = "C:\\datasets\\synthetic\\spatiallUniformTweetsForQueries.csv";
	public static final String spatiallySkewedTweetsQueriesPath = "C:\\datasets\\synthetic\\spatiallSkewedTweetsForQueries.csv";
	public static final String textuallyUniformTweetsQueriesPath = "C:\\datasets\\synthetic\\textuallyUniformTweetsQueriesPath.csv";

	
	public static final String spatiallyUniformTweetsObjectsPath = "C:\\datasets\\synthetic\\spatiallUniformtSampletweets.csv";
	public static final String spatiallySkewedQueryLikeTweetsObjectsPath = "C:\\datasets\\synthetic\\spatiallSkewedQueryLiketSampletweets.csv";
	public static final String spatiallySkewedQueryOppositeTweetsObjectsPath = "C:\\datasets\\synthetic\\spatiallSkewedQueryOppositetSampletweets.csv";
	public static final String textuallyUniformTweetsObjectPath= "C:\\datasets\\synthetic\\textuallyUniformTweetsObjectPath.csv";

	public static RandomGenerator randomGenerator = new RandomGenerator(0);

	public static void main(String[] args) throws Exception {
		
		testFast("results/results.csv",
				tweetsQueriesPath,tweetsObjectsPath, 5000000, 100000, false, 5, 20, 100.0, 3, 512, 8, 0);
		

	}

	
	static void testFast(String opFile, String qFile, String tweetFile, Integer numOfQueries, Integer numOfObjs,
			Boolean getMemSize, Integer expansionThreshold, Integer degThreshold, Double spaRange,
			Integer numOfKeywords, Integer gridGan, Integer maxLevl, Integer dataset) throws Exception {
		String outputFile = "results/ptpperformance.csv";
		if (opFile != null)
			outputFile = opFile;
		String tweetsFile = "/media/D/googleDrive/walid research/datasets/twittersample/sampletweets.csv";
		if (tweetFile != null)
			tweetsFile = tweetFile;
		String queriesFile = "/media/D/datasets/tweetsForQueries.csv";
		if (qFile != null)
			queriesFile = qFile;

		finegGridGran = 512;
		if (gridGan != null)
			finegGridGran = gridGan;

		Integer numberOfDataObjects = 10000;
		if (numOfObjs != null)
			numberOfDataObjects = numOfObjs;

		Integer numberOfQueries = 10000;
		if (numOfQueries != null)
			numberOfQueries = numOfQueries;
		Integer numberOfKeywords = 3;
		if (numOfKeywords != null)
			numberOfKeywords = numOfKeywords;
		boolean randomSpatialRange = true;
		boolean randomNumberOfKeywords = false;
		boolean getSize = false;
		if (getMemSize != null)
			getSize = getMemSize;
		boolean verifyCorrectness = false;

		Double spatialRange = 100.0;
		if (spaRange != null)
			spatialRange = spaRange;
		Integer threshold = 5;
		if (expansionThreshold != null)
			threshold = expansionThreshold;
		Integer degerationThreshold = 2;
		if (degThreshold != null)
			degerationThreshold = degThreshold;
		Integer maxLevel = 8;
		if (maxLevl != null)
			maxLevel = maxLevl;
		TextualPredicate txtPredicate = TextualPredicate.CONTAINS;
		ArrayList<DataObject> dataObjects = readDataObjects(tweetsFile, numberOfDataObjects, dataset);
		System.out.println("Done reading data");
		System.out.println("********************************************************");
		System.out.println("Hybrid Pyramid grid spatial range" + spatialRange);
		ArrayList<MinimalRangeQuery> queries = readMinimalQueries(queriesFile, numberOfQueries, txtPredicate,
				spatialRange, numberOfKeywords, randomSpatialRange, randomNumberOfKeywords, dataset);
		System.out.println("Done reading queries");
		// previousRange = spatialRange;
		// for (Integer threshold : thresholds) {
		String result = "PTP expansion Threashold," + threshold + ",Spatialrange," + spatialRange + ",numofkeywords,"
				+ numberOfKeywords + ",";
		System.out.println("Hybrid Pyramid grid threashold" + threshold);
		System.gc();
		System.gc();
		System.gc();
		System.gc();

		FAST.Trie_SPLIT_THRESHOLD = threshold;
		FAST.Degredation_Ratio = degerationThreshold;

		FAST localHybridPyramidIndexExperiment = new FAST(
				new Rectangle(new Point(0.0, 0.0),
						new Point(SpatioTextualConstants.xMaxRange, SpatioTextualConstants.yMaxRange)),
				finegGridGran, maxLevel);
		result += testLocalPyramid(dataObjects, queries, localHybridPyramidIndexExperiment, getSize, verifyCorrectness);

		localHybridPyramidIndexExperiment = null;
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		appendToFile(outputFile, result);
	}



	static String testLocalPyramid(ArrayList<DataObject> dataObjects, ArrayList<MinimalRangeQuery> queries,
			FAST localIndex, boolean findSize, boolean verifyCorrectness)
			throws Exception {
		String metadata = "";
		String toReturn = "";
		Long dataProcessingDuration;
		Long queryRegisterationduration;
		FAST.totalVisited = 0;
		FAST.spatialOverlappingQuries = 0;
		int queryTasks = 0;
		double sumQueryKeywords = 0;
		double sumOfObjectsKeywords = 0;
		Stopwatch stopwatch = Stopwatch.createStarted();

		for (MinimalRangeQuery q : queries) {
			localIndex.addContinousQuery(q);
			sumQueryKeywords += q.queryText.size();

		}

		stopwatch.stop();
		KeyWordTrieIndexMinimal trieIndex = new KeyWordTrieIndexMinimal();
		if (verifyCorrectness)
			for (MinimalRangeQuery q : queries) {
				trieIndex.insert(q.queryText, q);

			}
		

		queryRegisterationduration = stopwatch.elapsed(TimeUnit.NANOSECONDS);

		metadata += "\nQuery register Time per query (nanos)    = " + queryRegisterationduration / queries.size();
		metadata += "\nTotal query evalautors                   = " + queryTasks;
		metadata += "\nqueryInsertInvListNodeCounter            = "
				+ FAST.queryInsertInvListNodeCounter;
		metadata += "\nqueryInsertTrieNodeCounter               = "
				+ FAST.queryInsertTrieNodeCounter;
		metadata += "\nTotal node insertions                    = "
				+ (FAST.queryInsertTrieNodeCounter
						+ FAST.queryInsertInvListNodeCounter);
		metadata += "\ntotalQueryInsertionsIncludingReplications= "
				+ FAST.totalQueryInsertionsIncludingReplications;
		metadata += "\nAverage query replications               = "
				+ FAST.totalQueryInsertionsIncludingReplications
						/ (double) queries.size();
		metadata += "\nnumberOfHashEntries                      = "
				+ FAST.numberOfHashEntries;
		metadata += "\nnumberOfTrieNodes                        = "
				+ FAST.numberOfTrieNodes;
		metadata += "\nAverage ranked inv list length           = " + localIndex.getAverageRankedInvListSize();
		metadata += "\nAverage query keywords size              = " + sumQueryKeywords / queries.size();

		stopwatch = Stopwatch.createStarted();
		int querycount = 0;
		for (int i = 0; i < 5; i++) {

			for (DataObject obj : dataObjects) {

				sumOfObjectsKeywords += obj.getObjectText().size();
				List<MinimalRangeQuery> result = localIndex.getReleventSpatialKeywordRangeQueries(obj, false);
				if (verifyCorrectness)
					// if (result.size() > 0) {
					verifyCorrectness(trieIndex, obj, result, localIndex);
				// }
				if (result != null && result.size() > 0) {
					querycount += result.size();
				}
			}
		}
		stopwatch.stop();
		dataProcessingDuration = stopwatch.elapsed(TimeUnit.NANOSECONDS);
		System.gc();
		System.gc();
		long queriesSize = 0;
		long indexMemorySize = 0;
		if (findSize) {
			queriesSize = ObjectSizeCalculator.getObjectSize(queries);
			System.out.println("Queries size =" + queriesSize / 1024 / 1024 + " MB");
			indexMemorySize = ObjectSizeCalculator.getObjectSize(localIndex) - queriesSize;
			System.out.println("Local index size =" + indexMemorySize / 1024 / 1024 + " MB");
			System.gc();
			System.gc();
		}

		Integer totalEmiitedCount = 0;

		System.out.println(" Local" + " DataProcessing Time per object (nano)= "
				+ (dataProcessingDuration / dataObjects.size() / 5) + " with qulified tuples:" + totalEmiitedCount
				+ "total query count = " + querycount + "total visted  = "
				+ FAST.totalVisited + "totalspatial overlapping  = "
				+ FAST.spatialOverlappingQuries);

		metadata += "\nAverage object keywords        = " + sumOfObjectsKeywords / dataObjects.size() / 5;
		metadata += "\nobjectSearchInvListNodeCounter = "
				+ FAST.objectSearchInvListNodeCounter / 5;
		metadata += "\nobjectSearchTrieNodeCounter    = "
				+ FAST.objectSearchTrieNodeCounter / 5;
		metadata += "\nTotal search node access       = "
				+ ((FAST.objectSearchTrieNodeCounter
						+ FAST.objectSearchInvListNodeCounter)) / 5;
		metadata += "\nobjectSearchInvListHashAccess  = "
				+ FAST.objectSearchInvListHashAccess / 5;
		metadata += "\nobjectSearchTrieHashAccess     = "
				+ FAST.objectSearchTrieHashAccess / 5;
		metadata += "\nTotoal trie access             = " + FAST.totalTrieAccess / 5;
		metadata += "\nAverage operations per trie    = "
				+ (FAST.objectSearchTrieNodeCounter
						+ FAST.objectSearchTrieHashAccess)
						/ (FAST.totalTrieAccess + 1);
		metadata += "\nTotal hash aceesses            = "
				+ ((FAST.objectSearchTrieHashAccess
						+ FAST.objectSearchInvListHashAccess)) / 5;
		metadata += "\nTotal operations               = "
				+ (((FAST.objectSearchTrieHashAccess
						+ FAST.objectSearchInvListHashAccess)
						+ (FAST.objectSearchTrieNodeCounter
								+ FAST.objectSearchInvListNodeCounter)))
						/ 5;

		toReturn = toReturn + "querytime," + (queryRegisterationduration / queries.size()) + ",objectime,"
				+ (dataProcessingDuration / dataObjects.size() / 5) + ",indexsize," + (indexMemorySize / 1024 / 1024)
				+ ",queriessize," + (queriesSize / 1024 / 1024) + ",numberofqueries," + queries.size() + "," + "\n";

		System.out.println(metadata);
		dataObjects = null;

		queries = null;
		return toReturn;
	}

	public static void verifyCorrectness(KeyWordTrieIndexMinimal trieIndex, DataObject obj,
			List<MinimalRangeQuery> result, FAST localIndex) {
		LinkedList<MinimalRangeQuery> trieResult = trieIndex.find(obj.getObjectText());
		int totalResult = 0;
		HashSet<MinimalRangeQuery> resultSet = new HashSet<MinimalRangeQuery>();
		HashSet<MinimalRangeQuery> trieResultHashSet = new HashSet<MinimalRangeQuery>();
		for (MinimalRangeQuery q : trieResult) {
			if (SpatialHelper.overlapsSpatially(obj.getLocation(), q.spatialRange)
					&& TextHelpers.containsTextually(obj.getObjectText(), q.queryText)) {
				totalResult++;
				trieResultHashSet.add(q);
			}
		}
		for (MinimalRangeQuery q : result) {
			resultSet.add(q);
		}
		if (totalResult < result.size()) {
			System.out.println("Object:" + result.size() + ":" + obj.toString());
			for (MinimalRangeQuery q : result) {
				if (!trieResultHashSet.contains(q)) {
					System.err.println("Error extra query");

				}

				if (!SpatialHelper.overlapsSpatially(obj.getLocation(), q.spatialRange)
						|| !TextHelpers.containsTextually(obj.getObjectText(), q.queryText)) {
					System.err.println("Error should repeat");

				}

			}
			localIndex.getReleventSpatialKeywordRangeQueries(obj, false);
			;

		} else if (totalResult > result.size()) {
			System.out.println("Object:" + result.size() + ":" + obj.toString());
			for (MinimalRangeQuery q : trieResultHashSet) {
				if (!resultSet.contains(q)) {
					if (q.queryId == 657746)
						System.out.println("missing");
					System.err.println("Error missing query" + q.toString());
					localIndex.getReleventSpatialKeywordRangeQueries(obj, true);
				}
			}
		}
	}
	public static double stdev(Double a[], int n) {

		if (n == 0)
			return 0.0;
		double sum = 0;
		double sq_sum = 0;
		for (int i = 0; i < n; ++i) {
			sum += a[i];
			sq_sum += a[i] * a[i];
		}
		double mean = sum / n;
		double variance = sq_sum / n - mean * mean;
		return Math.sqrt(variance);
	}

	public static ArrayList<DataObject> readDataObjects(String fileName, int numberOfDataObjects, int dataset) {
		ArrayList<DataObject> allObjects = new ArrayList<DataObject>();
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			DataObject obj;
			int i = 0;
			while (i < numberOfDataObjects && (line = br.readLine()) != null) {
				if (dataset == 0)
					obj = parseTweetToDataObject(line);
				else if (dataset == 1)
					obj = mapPOILineToDataObj(line);
				else if (dataset == 2) // sysnthtic dataset
					obj = parseSyntheticDatasetLine(line);
				else {
					obj = null;
					System.out.println("Invalid dataset error");
				}

				if (obj == null)
					continue;
				obj.hashedText = new HashSet<>(obj.getObjectText());
				// obj.setObjectText(null);
				allObjects.add(obj);
				i++;

			}
			br.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return allObjects;
	}

	static ArrayList<MinimalRangeQuery> readMinimalQueries(String fileName, int numberOfqueries,
			TextualPredicate textualPredicate, double spatialRange, int numberOfKeywords, boolean randomSpatialRange,
			boolean radomNumberOfKeywords, int dataset) {
		ArrayList<MinimalRangeQuery> allQueiries = new ArrayList<MinimalRangeQuery>();
		QueriesReader queriesSpout = new QueriesReader(null, 0);
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			Query query;
			MinimalRangeQuery minimalRangeQuery;
			Double queryspatialRange = spatialRange;
			int querynumberOfKeywords = numberOfKeywords;
			int i = 0;
			randomGenerator = new RandomGenerator(0);
			while (i < numberOfqueries) {
				if ((line = br.readLine()) == null) {
					fstream = new FileInputStream(fileName);
					br = new BufferedReader(new InputStreamReader(fstream));
					line = br.readLine();
				}
				if (randomSpatialRange)
					queryspatialRange = randomGenerator.nextDouble(0, spatialRange);
				if (radomNumberOfKeywords)
					querynumberOfKeywords = randomGenerator.nextInt(numberOfKeywords);
				if ("".equals(line))
					continue;
				if (dataset == 0) {
					query = queriesSpout.buildQuery(line, "querySrc", querynumberOfKeywords, "Tweets", null, null,
							QueryType.queryTextualRange, queryspatialRange, textualPredicate, null, null);
					if (query == null)
						continue;
					minimalRangeQuery = new MinimalRangeQuery();
					minimalRangeQuery.queryId = i;
					minimalRangeQuery.textualPredicate = textualPredicate;
					minimalRangeQuery.spatialRange = query.getSpatialRange();
					minimalRangeQuery.queryText = query.getQueryText();
					minimalRangeQuery.deleted = false;
					minimalRangeQuery.expireTime = Integer.MAX_VALUE;
					allQueiries.add(minimalRangeQuery);
					i++;
				} else if (dataset == 1) {
					query = queriesSpout.buildQueryFromPoi(line, "querySrc", querynumberOfKeywords, "Tweets", null,
							null, QueryType.queryTextualRange, queryspatialRange, textualPredicate, null, null);
					if (query == null)
						continue;
					minimalRangeQuery = new MinimalRangeQuery();
					minimalRangeQuery.queryId = i;
					minimalRangeQuery.textualPredicate = textualPredicate;
					minimalRangeQuery.spatialRange = query.getSpatialRange();
					minimalRangeQuery.queryText = query.getQueryText();
					minimalRangeQuery.deleted = false;
					minimalRangeQuery.expireTime = Integer.MAX_VALUE;
					allQueiries.add(minimalRangeQuery);
					i++;

				} else if (dataset == 2) {
					query = queriesSpout.buildQueryFromSynthetic(line, "querySrc", querynumberOfKeywords, "Tweets",
							null, null, QueryType.queryTextualRange, queryspatialRange, textualPredicate, null, null);
					if (query == null)
						continue;
					minimalRangeQuery = new MinimalRangeQuery();
					minimalRangeQuery.queryId = i;
					minimalRangeQuery.textualPredicate = textualPredicate;
					minimalRangeQuery.spatialRange = query.getSpatialRange();
					minimalRangeQuery.queryText = query.getQueryText();
					minimalRangeQuery.deleted = false;
					minimalRangeQuery.expireTime = Integer.MAX_VALUE;
					allQueiries.add(minimalRangeQuery);
					i++;

				}

			}
			br.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return allQueiries;
	}

	
	public static DataObject parseSyntheticDatasetLine(String line) {
		LatLong latLong = new LatLong();
		String[] lineParts = line.split(",");
		if (lineParts.length < 4)
			return null;
		// String id = tweetParts[0];
		Integer id = countId++;// tweetParts[0];
		if (countId >= Integer.MAX_VALUE)
			countId = 0;//
		double x = 0.0;
		double y = 0.0;

		try {
			x = Double.parseDouble(lineParts[1]);

			y = Double.parseDouble(lineParts[2]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		String textContent = "";
		int i = 3;
		while (i < lineParts.length)
			textContent = textContent + lineParts[i++] + " ";

		Point xy = new Point(x, y);
		Date date = new Date();

		DataObject obj = new DataObject(id, xy, textContent, date.getTime(), Command.addCommand);
		obj.setSrcId("Tweets");
		return obj;
	}

	public static DataObject parseTweetToDataObject(String tweet) {
		LatLong latLong = new LatLong();
		String[] tweetParts = tweet.split(",");
		if (tweetParts.length < 5)
			return null;
		// String id = tweetParts[0];
		Integer id = countId++;// tweetParts[0];
		if (countId >= Integer.MAX_VALUE)
			countId = 0;//
		double lat = 0.0;
		double lon = 0.0;

		try {
			lat = Double.parseDouble(tweetParts[2]);

			lon = Double.parseDouble(tweetParts[3]);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)
			return null;
		if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat)
			return null;
		if (lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong)
			return null;
		latLong.setLatitude(lat);
		latLong.setLongitude(lon);
		String textContent = "";
		int i = 5;
		while (i < tweetParts.length)
			textContent = textContent + tweetParts[i++] + " ";

		Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong);
		Date date = new Date();

		DataObject obj = new DataObject(id, xy, textContent, date.getTime(), Command.addCommand);
		obj.setSrcId("Tweets");
		return obj;
	}

	public static DataObject mapPOILineToDataObj(String line) {
		StringTokenizer stringTokenizer = new StringTokenizer(line, ",");
		String id = "", text = "";
		if (countId >= Integer.MAX_VALUE)
			countId = 0;
		Double lat, lon;
		if (stringTokenizer.hasMoreTokens()) {
			id = stringTokenizer.nextToken();
		} else
			return null;
		if (stringTokenizer.hasMoreTokens()) {
			lat = Double.parseDouble(stringTokenizer.nextToken());
		} else
			return null;
		if (stringTokenizer.hasMoreTokens()) {
			lon = Double.parseDouble(stringTokenizer.nextToken());
		} else
			return null;
		text = "";
		while (stringTokenizer.hasMoreTokens()) {
			text += (" " + stringTokenizer.nextToken());
		}
		if (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)
			return null;
		if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat)
			return null;
		if (lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong)
			return null;

		Point point = SpatialHelper.convertFromLatLonToXYPoint(new LatLong(lat, lon));
		DataObject dataObject = new DataObject();
		Date date = new Date();
		dataObject.setOriginalText(text);
		ArrayList<String> textContent = TextHelpers.transformIntoSortedArrayListOfString(text);
		dataObject.setLocation(point);
		dataObject.setObjectId(countId++);
		dataObject.setObjectText(textContent);
		dataObject.setSrcId("POIs");
		dataObject.setTimeStamp(date.getTime());
		return dataObject;

	}

	
	
	public static void appendToFile(String fileName, String data) {
		BufferedWriter bw = null;

		try {
			// APPEND MODE SET HERE
			bw = new BufferedWriter(new FileWriter(fileName, true));
			bw.write(data);
			bw.newLine();
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { // always close the file
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
					ioe2.printStackTrace();
				}
		}
	}

	public static void generateSpatiallyUniformDataset(String inputDataFile, String outputDataFile,
			int numberOfObjects) {
		try {

			FileInputStream fstream = new FileInputStream(inputDataFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			DataObject obj;
			int i = 0;
			while (i < numberOfObjects && (line = br.readLine()) != null) {
				Double x = randomGenerator.nextDouble(0, SpatioTextualConstants.xMaxRange);
				Double y = randomGenerator.nextDouble(0, SpatioTextualConstants.xMaxRange);
				obj = parseTweetToDataObject(line);
				if (obj != null && obj.getOriginalText() != null) {
					String newLine = "" + i + "," + x + "," + y + "," + obj.getOriginalText();

					appendToFile(outputDataFile, newLine);

					i++;
				}

			}
			br.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public static void generatetextuallyUniformDataset(String inputDataFile, String inputVocabFile, String outputDataFile,
			int numberOfObjects, int numofkeywords) {
		try {

			ArrayList<String> keywords = new ArrayList<String>();
			FileInputStream fstreamvocab = new FileInputStream(inputVocabFile);
			BufferedReader brvocab = new BufferedReader(new InputStreamReader(fstreamvocab));
			String lineVocab;
			while ((lineVocab = brvocab.readLine()) != null) {
				keywords.add(lineVocab);
			}
			brvocab.close();
			fstreamvocab.close();
			
			FileInputStream fstream = new FileInputStream(inputDataFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			DataObject obj;
			int i = 0;
			while (i < numberOfObjects && (line = br.readLine()) != null) {
				String textualContent="";
				obj = parseTweetToDataObject(line);
				int num = numofkeywords;
				if(numofkeywords==-1) {//
					num = 1+randomGenerator.nextInt( 20);
				}
				
				for(int j =0;j<num;j++) {
					int k = randomGenerator.nextInt(keywords.size()-1);
					textualContent = textualContent+" "+keywords.get(k);
				}
				if (obj != null && obj.getOriginalText() != null) {
					String newLine = "" + i + "," + obj.getLocation().X + "," + obj.getLocation().Y  + "," + textualContent;

					appendToFile(outputDataFile, newLine);

					i++;
				}

			}
			br.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public static void generateSpatiallySkewedDataset(String inputDataFile, String outputDataFile,
			int numberOfObjects) {
		try {

			FileInputStream fstream = new FileInputStream(inputDataFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			DataObject obj;
			int i = 0;
			while (i < numberOfObjects / 2 && (line = br.readLine()) != null) {
				Double x = nextSkewedBoundedDouble(0, SpatioTextualConstants.xMaxRange / 2, 4, -2);
				Double y = nextSkewedBoundedDouble(0, SpatioTextualConstants.xMaxRange / 2, 4, -2);
				obj = parseTweetToDataObject(line);
				if (obj != null && obj.getOriginalText() != null) {
					String newLine = "" + i + "," + x + "," + y + "," + obj.getOriginalText();

					appendToFile(outputDataFile, newLine);

					i++;
				}

			}

			while (i < numberOfObjects && (line = br.readLine()) != null) {
				Double x = nextSkewedBoundedDouble(SpatioTextualConstants.xMaxRange / 3,
						SpatioTextualConstants.xMaxRange, 4, -1);
				Double y = nextSkewedBoundedDouble(SpatioTextualConstants.xMaxRange / 3,
						SpatioTextualConstants.xMaxRange, 4, -1);
				obj = parseTweetToDataObject(line);
				if (obj != null && obj.getOriginalText() != null) {
					String newLine = "" + i + "," + x + "," + y + "," + obj.getOriginalText();

					appendToFile(outputDataFile, newLine);

					i++;
				}

			}
			br.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public static void generateVocabularyFile(String inputDataFile, String outputDataFile) {
		try {

			FileInputStream fstream = new FileInputStream(inputDataFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String line;
			DataObject obj;
			int i = 0;
			HashSet<String> keywords = new HashSet<String>();
			while ((line = br.readLine()) != null) {
				obj = parseTweetToDataObject(line);
				if (obj != null && obj.getOriginalText() != null) {
					for (String keyword : obj.getObjectText()) {
						keywords.add(keyword);
					}
					
				}

			}

			for (String keyword : keywords) {

				appendToFile(outputDataFile, keyword);
			}
			br.close();
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	static public double nextSkewedBoundedDouble(double min, double max, double skew, double bias) {
		// https://stackoverflow.com/questions/5853187/skewing-java-random-number-generation-toward-a-certain-number
		double range = max - min;
		double mid = min + range / 2.0;
		double unitGaussian = randomGenerator.nextGaussian();
		double biasFactor = Math.exp(bias);
		double retval = mid + (range * (biasFactor / (biasFactor + Math.exp(-unitGaussian / skew)) - 0.5));
		return retval;
	}

}

class QueryRankAwareStatsComparator implements Comparator<Entry<String, KeywordFrequencyStats>> {
	@Override
	public int compare(Entry<String, KeywordFrequencyStats> e1, Entry<String, KeywordFrequencyStats> e2) {
		int val1 = 0, val2 = 0;

		val1 = e1.getValue().queryCount;

		val2 = e2.getValue().queryCount;
		if (val1 < val2) {
			return 1;
		} else if (val1 == val2)
			return 0;
		else {
			return -1;
		}
	}
}

class ObjectCountCompartor implements Comparator<Entry<String, KeywordFrequencyStats>> {
	@Override
	public int compare(Entry<String, KeywordFrequencyStats> e1, Entry<String, KeywordFrequencyStats> e2) {
		int val1 = 0, val2 = 0;

		val1 = e1.getValue().objectVisitCount;

		val2 = e2.getValue().objectVisitCount;
		if (val1 < val2) {
			return 1;
		} else if (val1 == val2)
			return 0;
		else {
			return -1;
		}
	}
}

class RecencyScoreCompartor implements Comparator<Entry<String, KeywordFrequencyStats>> {
	@Override
	public int compare(Entry<String, KeywordFrequencyStats> e1, Entry<String, KeywordFrequencyStats> e2) {
		int val1 = 0, val2 = 0;

		val1 = e1.getValue().objectVisitCount * e1.getValue().queryCount;

		val2 = e2.getValue().objectVisitCount * e2.getValue().queryCount;
		if (val1 < val2) {
			return 1;
		} else if (val1 == val2)
			return 0;
		else {
			return -1;
		}
	}
}
