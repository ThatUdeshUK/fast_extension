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
import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.structures.KeywordFrequency;
import edu.purdue.cs.fast.parser.LatLong;
import edu.purdue.cs.fast.helper.ObjectSizeCalculator;
import edu.purdue.cs.fast.helper.QueryType;
import edu.purdue.cs.fast.helper.RandomGenerator;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.helper.TextualPredicate;
import edu.purdue.cs.fast.parser.ImportableQuery;
import edu.purdue.cs.fast.parser.QueriesReader;


/**
 * This class tests the global and local index performance and test the value of
 * partitioning
 *
 * @author ahmed
 */
public class Test {
    static Integer countId = 0;
    static Integer queryCountId = 0;
    static Integer finegGridGran = 64;
    public static Integer xMaxRange = 512;
    public static Integer yMaxRange = 512;
    public static final String vocabPath = "C:\\datasets\\synthetic\\vocab.csv";
    public static final String tweetsQueriesPath = "C:\\datasets\\tweets\\tweetsForQueries.csv";
    public static final String tweetsObjectsPath = "C:\\datasets\\tweets\\sampletweets.csv";
    public static final String spatiallyUniformTweetsQueriesPath = "C:\\datasets\\synthetic\\spatiallUniformTweetsForQueries.csv";
    public static final String spatiallySkewedTweetsQueriesPath = "C:\\datasets\\synthetic\\spatiallSkewedTweetsForQueries.csv";
    public static final String textuallyUniformTweetsQueriesPath = "C:\\datasets\\synthetic\\textuallyUniformTweetsQueriesPath.csv";


    public static final String spatiallyUniformTweetsObjectsPath = "C:\\datasets\\synthetic\\spatiallUniformtSampletweets.csv";
    public static final String spatiallySkewedQueryLikeTweetsObjectsPath = "C:\\datasets\\synthetic\\spatiallSkewedQueryLiketSampletweets.csv";
    public static final String spatiallySkewedQueryOppositeTweetsObjectsPath = "C:\\datasets\\synthetic\\spatiallSkewedQueryOppositetSampletweets.csv";
    public static final String textuallyUniformTweetsObjectPath = "C:\\datasets\\synthetic\\textuallyUniformTweetsObjectPath.csv";

    public static RandomGenerator randomGenerator = new RandomGenerator(0);

    public static void main(String[] args) throws Exception {

//		testFast("results/results.csv", tweetsQueriesPath,tweetsObjectsPath, 5000000, 100000, false, 5, 20, 100.0, 3, 512, 8, 0);

        exportTweets();
        // testFast("results/results.csv", null, null, 5000000, 100000, false, 5, 20, 100.0, 3, 512, 8, 0);
    }

    static void exportTweets() {
        FileWriter fw = null;
        BufferedWriter bw = null;

        String tweetsFile = "/homes/ukumaras/scratch/twitter-data/all.csv";
        // String queriesFile = "/homes/ukumaras/scratch/twitter-data/queries.csv";
        String outputFile = "/homes/ukumaras/scratch/twitter-data/tweets.csv";
        TextualPredicate txtPredicate = TextualPredicate.CONTAINS;
        ArrayList<DataObject> dataObjects = readDataObjects(tweetsFile, 30000000, 0);
        System.out.println("Done reading data");

        try {
            fw = new FileWriter(outputFile, false);
            bw = new BufferedWriter(fw);
            for (DataObject o: dataObjects) {
                if (o.keywords.size() > 1) {
                    bw.write(o.toCSV() + "\n");
                }
            }   bw.close();
            fw.close();
        } catch (IOException ex) {
        } finally {
            try {
                bw.close();
                fw.close();
            } catch (IOException ex) {
            }
        }
    }

    static void testFast(String opFile, String qFile, String tweetFile, Integer numOfQueries, Integer numOfObjs,
                         Boolean getMemSize, Integer expansionThreshold, Integer degThreshold, Double spaRange,
                         Integer numOfKeywords, Integer gridGan, Integer maxLevl, Integer dataset) throws Exception {
        String outputFile = "results/ptpperformance.csv";
        if (opFile != null)
            outputFile = opFile;
        String tweetsFile = "/homes/ukumaras/scratch/twitter-data/data_objects.csv";
        if (tweetFile != null)
            tweetsFile = tweetFile;
        String queriesFile = "/homes/ukumaras/scratch/twitter-data/queries.csv";
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

        FAST.config.TRIE_SPLIT_THRESHOLD = threshold;
        FAST.config.DEGRADATION_RATIO = degerationThreshold;

        FAST localHybridPyramidIndexExperiment = new FAST(
                new Config(),
                new Rectangle(new Point(0.0, 0.0),
                        new Point(xMaxRange, yMaxRange)),
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
        int queryTasks = 0;
        double sumQueryKeywords = 0;
        double sumOfObjectsKeywords = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (MinimalRangeQuery q : queries) {
//            localIndex.addContinuousBoundedQuery(q);
            sumQueryKeywords += q.keywords.size();
        }

        stopwatch.stop();
        KeyWordTrieIndexMinimal trieIndex = new KeyWordTrieIndexMinimal();
        if (verifyCorrectness)
            for (MinimalRangeQuery q : queries) {
                trieIndex.insert(q.keywords, q);
            }

        queryRegisterationduration = stopwatch.elapsed(TimeUnit.NANOSECONDS);

        metadata += "\nImportableQuery register Time per query (nanos)    = " + queryRegisterationduration / queries.size();
        metadata += "\nTotal query evalautors                   = " + queryTasks;
        metadata += "\nqueryInsertInvListNodeCounter            = "
                + FAST.context.queryInsertInvListNodeCounter;
        metadata += "\nqueryInsertTrieNodeCounter               = "
                + FAST.context.queryInsertTrieNodeCounter;
        metadata += "\nTotal node insertions                    = "
                + (FAST.context.queryInsertTrieNodeCounter
                + FAST.context.queryInsertInvListNodeCounter);
        metadata += "\ntotalQueryInsertionsIncludingReplications= "
                + FAST.context.totalQueryInsertionsIncludingReplications;
        metadata += "\nAverage query replications               = "
                + FAST.context.totalQueryInsertionsIncludingReplications
                / (double) queries.size();
        metadata += "\nnumberOfHashEntries                      = "
                + FAST.context.numberOfHashEntries;
//        metadata += "\nnumberOfTrieNodes                        = "
//                + FAST.context.numberOfTrieNodes;
//        metadata += "\nAverage ranked inv list length           = " + localIndex.getAverageRankedInvListSize();
        metadata += "\nAverage query keywords size              = " + sumQueryKeywords / queries.size();

        stopwatch = Stopwatch.createStarted();
        int querycount = 0;
        for (int i = 0; i < 5; i++) {

            for (DataObject obj : dataObjects) {

                sumOfObjectsKeywords += obj.keywords.size();
                List<Query> result = localIndex.insertObject(obj);
                if (verifyCorrectness)
                    // if (result.size() > 0) {
//                    verifyCorrectness(trieIndex, obj, result, localIndex);
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
                + FAST.context.totalVisited + "totalspatial overlapping  = "
                + FAST.context.spatialOverlappingQueries);

        metadata += "\nAverage object keywords        = " + sumOfObjectsKeywords / dataObjects.size() / 5;
        metadata += "\nobjectSearchInvListNodeCounter = "
                + FAST.context.objectSearchInvListNodeCounter / 5;
        metadata += "\nobjectSearchTrieNodeCounter    = "
                + FAST.context.objectSearchTrieNodeCounter / 5;
        metadata += "\nTotal search node access       = "
                + ((FAST.context.objectSearchTrieNodeCounter
                + FAST.context.objectSearchInvListNodeCounter)) / 5;
        metadata += "\nobjectSearchInvListHashAccess  = "
                + FAST.context.objectSearchInvListHashAccess / 5;
        metadata += "\nobjectSearchTrieHashAccess     = "
                + FAST.context.objectSearchTrieHashAccess / 5;
        metadata += "\nTotoal trie access             = " + FAST.context.totalTrieAccess / 5;
        metadata += "\nAverage operations per trie    = "
                + (FAST.context.objectSearchTrieNodeCounter
                + FAST.context.objectSearchTrieHashAccess)
                / (FAST.context.totalTrieAccess + 1);
        metadata += "\nTotal hash aceesses            = "
                + ((FAST.context.objectSearchTrieHashAccess
                + FAST.context.objectSearchInvListHashAccess)) / 5;
        metadata += "\nTotal operations               = "
                + (((FAST.context.objectSearchTrieHashAccess
                + FAST.context.objectSearchInvListHashAccess)
                + (FAST.context.objectSearchTrieNodeCounter
                + FAST.context.objectSearchInvListNodeCounter)))
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
        LinkedList<MinimalRangeQuery> trieResult = trieIndex.find(obj.keywords);
        int totalResult = 0;
        HashSet<MinimalRangeQuery> resultSet = new HashSet<MinimalRangeQuery>();
        HashSet<MinimalRangeQuery> trieResultHashSet = new HashSet<MinimalRangeQuery>();
        for (MinimalRangeQuery q : trieResult) {
            if (SpatialHelper.overlapsSpatially(obj.location, q.spatialRange)
                    && TextHelpers.containsTextually(obj.keywords, q.keywords)) {
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

                if (!SpatialHelper.overlapsSpatially(obj.location, q.spatialRange)
                        || !TextHelpers.containsTextually(obj.keywords, q.keywords)) {
                    System.err.println("Error should repeat");

                }

            }
            localIndex.insertObject(obj);
            ;

        } else if (totalResult > result.size()) {
            System.out.println("Object:" + result.size() + ":" + obj.toString());
            for (MinimalRangeQuery q : trieResultHashSet) {
                if (!resultSet.contains(q)) {
                    if (q.id == 657746)
                        System.out.println("missing");
                    System.err.println("Error missing query" + q.toString());
                    localIndex.insertObject(obj);
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
                // obj.setKeywords(null);
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
            ImportableQuery importableQuery;
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
                    importableQuery = queriesSpout.buildQuery(line, "querySrc", querynumberOfKeywords, "Tweets", null, null,
                            QueryType.queryTextualRange, queryspatialRange, textualPredicate, null, null);
                    if (importableQuery == null)
                        continue;
                    minimalRangeQuery = new MinimalRangeQuery(i, importableQuery.getQueryText(), importableQuery.spatialRange, textualPredicate, 0, Integer.MAX_VALUE);
                    allQueiries.add(minimalRangeQuery);
                    i++;
                } else if (dataset == 1) {
                    importableQuery = queriesSpout.buildQueryFromPoi(line, "querySrc", querynumberOfKeywords, "Tweets", null,
                            null, QueryType.queryTextualRange, queryspatialRange, textualPredicate, null, null);
                    if (importableQuery == null)
                        continue;
                    minimalRangeQuery = new MinimalRangeQuery(i, importableQuery.getQueryText(), importableQuery.spatialRange, textualPredicate, 0, Integer.MAX_VALUE);
                    allQueiries.add(minimalRangeQuery);
                    i++;

                } else if (dataset == 2) {
                    importableQuery = queriesSpout.buildQueryFromSynthetic(line, "querySrc", querynumberOfKeywords, "Tweets",
                            null, null, QueryType.queryTextualRange, queryspatialRange, textualPredicate, null, null);
                    if (importableQuery == null)
                        continue;
                    minimalRangeQuery = new MinimalRangeQuery(i, importableQuery.getQueryText(), importableQuery.spatialRange, textualPredicate, 0, Integer.MAX_VALUE);
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

        DataObject obj = new DataObject(id, xy, TextHelpers.transformIntoSortedArrayListOfString(textContent), date.getTime(), Integer.MAX_VALUE);
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

        Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong, xMaxRange, yMaxRange);
        Date date = new Date();

        DataObject obj = new DataObject(id, xy, TextHelpers.transformIntoSortedArrayListOfString(textContent), date.getTime(), Integer.MAX_VALUE);
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

        Point point = SpatialHelper.convertFromLatLonToXYPoint(new LatLong(lat, lon), xMaxRange, yMaxRange);
        Date date = new Date();

        DataObject dataObject = new DataObject(countId++, point, TextHelpers.transformIntoSortedArrayListOfString(text), date.getTime(), Integer.MAX_VALUE);
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

//    public static void generateSpatiallyUniformDataset(String inputDataFile, String outputDataFile,
//                                                       int numberOfObjects) {
//        try {
//
//            FileInputStream fstream = new FileInputStream(inputDataFile);
//            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
//            String line;
//            DataObject obj;
//            int i = 0;
//            while (i < numberOfObjects && (line = br.readLine()) != null) {
//                Double x = randomGenerator.nextDouble(0, SpatioTextualConstants.xMaxRange);
//                Double y = randomGenerator.nextDouble(0, SpatioTextualConstants.xMaxRange);
//                obj = parseTweetToDataObject(line);
//                if (obj != null && obj.getOriginalText() != null) {
//                    String newLine = "" + i + "," + x + "," + y + "," + obj.getOriginalText();
//
//                    appendToFile(outputDataFile, newLine);
//
//                    i++;
//                }
//
//            }
//            br.close();
//            fstream.close();
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }

//    public static void generatetextuallyUniformDataset(String inputDataFile, String inputVocabFile, String outputDataFile,
//                                                       int numberOfObjects, int numofkeywords) {
//        try {
//
//            ArrayList<String> keywords = new ArrayList<String>();
//            FileInputStream fstreamvocab = new FileInputStream(inputVocabFile);
//            BufferedReader brvocab = new BufferedReader(new InputStreamReader(fstreamvocab));
//            String lineVocab;
//            while ((lineVocab = brvocab.readLine()) != null) {
//                keywords.add(lineVocab);
//            }
//            brvocab.close();
//            fstreamvocab.close();
//
//            FileInputStream fstream = new FileInputStream(inputDataFile);
//            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
//            String line;
//            DataObject obj;
//            int i = 0;
//            while (i < numberOfObjects && (line = br.readLine()) != null) {
//                String textualContent = "";
//                obj = parseTweetToDataObject(line);
//                int num = numofkeywords;
//                if (numofkeywords == -1) {//
//                    num = 1 + randomGenerator.nextInt(20);
//                }
//
//                for (int j = 0; j < num; j++) {
//                    int k = randomGenerator.nextInt(keywords.size() - 1);
//                    textualContent = textualContent + " " + keywords.get(k);
//                }
//                if (obj != null && obj.getOriginalText() != null) {
//                    String newLine = "" + i + "," + obj.location.X + "," + obj.location.Y + "," + textualContent;
//
//                    appendToFile(outputDataFile, newLine);
//
//                    i++;
//                }
//
//            }
//            br.close();
//            fstream.close();
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }

//    public static void generateSpatiallySkewedDataset(String inputDataFile, String outputDataFile,
//                                                      int numberOfObjects) {
//        try {
//
//            FileInputStream fstream = new FileInputStream(inputDataFile);
//            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
//            String line;
//            DataObject obj;
//            int i = 0;
//            while (i < numberOfObjects / 2 && (line = br.readLine()) != null) {
//                Double x = nextSkewedBoundedDouble(0, SpatioTextualConstants.xMaxRange / 2, 4, -2);
//                Double y = nextSkewedBoundedDouble(0, SpatioTextualConstants.xMaxRange / 2, 4, -2);
//                obj = parseTweetToDataObject(line);
//                if (obj != null && obj.getOriginalText() != null) {
//                    String newLine = "" + i + "," + x + "," + y + "," + obj.getOriginalText();
//
//                    appendToFile(outputDataFile, newLine);
//
//                    i++;
//                }
//
//            }
//
//            while (i < numberOfObjects && (line = br.readLine()) != null) {
//                Double x = nextSkewedBoundedDouble(SpatioTextualConstants.xMaxRange / 3,
//                        SpatioTextualConstants.xMaxRange, 4, -1);
//                Double y = nextSkewedBoundedDouble(SpatioTextualConstants.xMaxRange / 3,
//                        SpatioTextualConstants.xMaxRange, 4, -1);
//                obj = parseTweetToDataObject(line);
//                if (obj != null && obj.getOriginalText() != null) {
//                    String newLine = "" + i + "," + x + "," + y + "," + obj.getOriginalText();
//
//                    appendToFile(outputDataFile, newLine);
//
//                    i++;
//                }
//
//            }
//            br.close();
//            fstream.close();
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }

//    public static void generateVocabularyFile(String inputDataFile, String outputDataFile) {
//        try {
//
//            FileInputStream fstream = new FileInputStream(inputDataFile);
//            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
//            String line;
//            DataObject obj;
//            int i = 0;
//            HashSet<String> keywords = new HashSet<String>();
//            while ((line = br.readLine()) != null) {
//                obj = parseTweetToDataObject(line);
//                if (obj != null && obj.getOriginalText() != null) {
//                    for (String keyword : obj.keywords) {
//                        keywords.add(keyword);
//                    }
//
//                }
//
//            }
//
//            for (String keyword : keywords) {
//
//                appendToFile(outputDataFile, keyword);
//            }
//            br.close();
//            fstream.close();
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }

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

class QueryRankAwareStatsComparator implements Comparator<Entry<String, KeywordFrequency>> {
    @Override
    public int compare(Entry<String, KeywordFrequency> e1, Entry<String, KeywordFrequency> e2) {
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

class ObjectCountCompartor implements Comparator<Entry<String, KeywordFrequency>> {
    @Override
    public int compare(Entry<String, KeywordFrequency> e1, Entry<String, KeywordFrequency> e2) {
        int val1 = 0, val2 = 0;

        val1 = e1.getValue().visitCount;

        val2 = e2.getValue().visitCount;
        if (val1 < val2) {
            return 1;
        } else if (val1 == val2)
            return 0;
        else {
            return -1;
        }
    }
}

class RecencyScoreCompartor implements Comparator<Entry<String, KeywordFrequency>> {
    @Override
    public int compare(Entry<String, KeywordFrequency> e1, Entry<String, KeywordFrequency> e2) {
        int val1 = 0, val2 = 0;

        val1 = e1.getValue().visitCount * e1.getValue().queryCount;

        val2 = e2.getValue().visitCount * e2.getValue().queryCount;
        if (val1 < val2) {
            return 1;
        } else if (val1 == val2)
            return 0;
        else {
            return -1;
        }
    }
}
