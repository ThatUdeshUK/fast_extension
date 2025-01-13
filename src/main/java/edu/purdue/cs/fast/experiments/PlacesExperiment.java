package edu.purdue.cs.fast.experiments;

import com.google.gson.Gson;
import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.config.Context;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.parser.Place;
import edu.purdue.cs.fast.structures.KeywordFrequency;

import java.io.*;
import java.util.*;

public class PlacesExperiment extends Experiment<Place> {
    protected final String inputPath;
    protected final Random randomizer;
    protected int numPreQueries;
    protected int numPreObjects;
    protected int numQueries;
    protected int numObjects;
    protected int numKeywords;
    protected double srRate;
    protected int maxRange;

    protected IndexType indexType = IndexType.FAST;

    public PlacesExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                            int numQueries, int numObjects, int numKeywords, double srRate, int maxRange) {
        this.name = name;
        this.outputPath = outputPath;
        this.inputPath = inputPath;
        this.numQueries = numQueries;
        this.numObjects = numObjects;
        this.numKeywords = numKeywords;
        this.srRate = srRate;
        this.index = index;
        this.maxRange = maxRange;
        this.randomizer = new Random(seed);
    }

    public PlacesExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                            int numPreObjects, int numPreQueries, int numQueries, int numObjects, int numKeywords,
                            double srRate, int maxRange, IndexType indexType) {
        this(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, srRate, maxRange);
        this.numPreObjects = numPreObjects;
        this.numPreQueries = numPreQueries;
        this.indexType = indexType;
    }

    @Override
    public void init() {
        ArrayList<Place> places = new ArrayList<>();

        File file = new File(inputPath);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            int lineCount = numPreQueries + numPreObjects + numQueries + numObjects;

            Run.logger.info("Parsing the Places file!");
            long start = System.currentTimeMillis();
            Gson gson = new Gson();
            int nullLines = 0;
            while (places.size() < lineCount) {
                String line = br.readLine();
                if (line == null) {
                    nullLines++;

                    if (nullLines > 1000)
                        throw new RuntimeException("EOF! File can't produce the requested number of lines.");
                } else
                    nullLines = 0;

                Place place = gson.fromJson(line, Place.class);
                if (place != null && place.keywords() != null && !place.keywords().isEmpty()) {
                    Collections.sort(place.keywords());
                    places.add(place);
                }
            }
            long end = System.currentTimeMillis();
            Run.logger.info("Done! Time=" + (end - start));

            Run.logger.info("Shuffling!");
            start = System.currentTimeMillis();
            Collections.shuffle(places, randomizer);
            end = System.currentTimeMillis();
            Run.logger.info("Shuffling Done! Time=" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException("Wrong path is given: " + inputPath);
        }

        Run.logger.info("Imported Places records: " + places.size());
        generateQueries(places);
        generateObjects(places);
    }

    @Override
    protected void generateQueries(List<Place> places) {
        int r = (int) (maxRange * srRate);

        if (numPreQueries > 0) {
            this.preQueries = new ArrayList<>();
            for (int i = 0; i < numPreQueries; i++) {
                Place place = places.get(i);
                preQueries.add(place.toMinimalRangeQuery(i, r, maxRange, numKeywords, numPreQueries + numPreObjects + numQueries + numObjects + 1, indexType));
            }
        }

        this.queries = new ArrayList<>();
        for (int i = numPreQueries; i < numPreQueries + numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toMinimalRangeQuery(i, r, maxRange, numKeywords, numPreQueries + numPreObjects + numQueries + numObjects + 1, indexType));
        }
    }

    @Override
    protected void generateObjects(List<Place> places) {
        int totalQueries = numPreQueries + numQueries;
        if (numPreObjects > 0) {
            this.preObjects = new ArrayList<>();
            for (int i = totalQueries; i < totalQueries + numPreObjects; i++) {
                Place place = places.get(i);
                preObjects.add(place.toDataObject(i - totalQueries, totalQueries + numPreObjects + numObjects + 1, indexType));
            }
        }

        this.objects = new ArrayList<>();
        for (int i = totalQueries + numPreObjects; i < totalQueries + numPreObjects + numObjects; i++) {
            Place place = places.get(i);
            objects.add(place.toDataObject(i - totalQueries, totalQueries + numPreObjects + numObjects + 1, indexType));
        }
    }

    @Override
    protected Metadata<String> generateMetadata() {
        Metadata metadata = new Metadata();
        metadata.add("num_queries", "" + numQueries);
        metadata.add("num_objects", "" + numObjects);
        metadata.add("sr_rate", "" + srRate);
        return metadata;
    }


    public void exportPlaces(String exportDir) throws InvalidOutputFile {
        init();
        preloadObjects();
        File qFile = new File(exportDir + "Places_Queries_" + numQueries + ".csv");
        File oFile = new File(exportDir + "Places_Objects_" + numObjects + ".csv");
        if (!qFile.isDirectory() && !oFile.isDirectory()) {
            try {
                // Export queries
                FileWriter fw = new FileWriter(qFile);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(KNNQuery.CSV_HEADER + "\n");
                for (Query query : queries) {
                    if (query instanceof KNNQuery) {
                        ((FAST) index).insertQuery(query);
                        bw.write(((KNNQuery) query).toCSV() + "\n");
                    } else {
                        throw new RuntimeException("Can't export queries of this type.");
                    }
                }
                bw.close();
                fw.close();

                // Export objects
                fw = new FileWriter(oFile);
                bw = new BufferedWriter(fw);

                bw.write(DataObject.CSV_HEADER + "\n");
                for (DataObject obj : objects) {
                    bw.write(obj.toCSV() + "\n");
                }
                bw.close();
                fw.close();
            } catch (IOException e) {
                throw new InvalidOutputFile(outputPath);
            }
        } else {
            throw new InvalidOutputFile(outputPath);
        }
    }

    @Override
    public void run() {
        init();
        if (preObjects != null)
            Run.logger.info("Preload Objects: " + preObjects.size() + "/" + numPreObjects);
        if (preQueries != null)
            Run.logger.info("Preload Queries: " + preQueries.size() + "/" + numPreQueries);
        Run.logger.info("Queries: " + queries.size() + "/" + numQueries);
        Run.logger.info("Objects: " + objects.size() + "/" + numObjects);

        System.gc();
        System.gc();
        System.gc();

        boolean bootstrap = false;
        boolean bootstraped = false;
        if (bootstrap) {
            Run.logger.info("Preloading objects and queries!");
            preloadObjects();
            preloadQueries();

            Run.logger.info("Creating index!");
            create();
            try {
                FileOutputStream fos = new FileOutputStream("./" + this.name + ".out");
                PrintWriter pw = new PrintWriter(fos);
                pw.write(toString((FAST) this.index));
                pw.close();
                fos.close();

                fos = new FileOutputStream("./" + this.name + ".config.out");
                pw = new PrintWriter(fos);
                pw.write(toString(FAST.config));
                pw.close();
                fos.close();

                fos = new FileOutputStream("./" + this.name + ".context.out");
                pw = new PrintWriter(fos);
                pw.write(toString(FAST.context));
                Context.objectSearcher = (query) -> (PriorityQueue<DataObject>) ((FAST) index).objIndex.search(query);
                pw.close();
                fos.close();

                fos = new FileOutputStream("./" + this.name + ".keymap.out");
                pw = new PrintWriter(fos);
                pw.write(toString(FAST.keywordFrequencyMap));
                pw.close();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (bootstraped) {
            try {
                FileReader fr = new FileReader("./" + this.name + ".out");
                BufferedReader br = new BufferedReader(fr);
                index = (FAST) fromString(br.readLine());
                br.close();
                fr.close();

                fr = new FileReader("./" + this.name + ".config.out");
                br = new BufferedReader(fr);
                FAST.config = (Config) fromString(br.readLine());
                    // FAST.config.CLEANING_INTERVAL = 1000;
                br.close();
                fr.close();

                fr = new FileReader("./" + this.name + ".context.out");
                br = new BufferedReader(fr);
                FAST.context = (Context) fromString(br.readLine());
                br.close();
                fr.close();
                
                fr = new FileReader("./" + this.name + ".keymap.out");
                br = new BufferedReader(fr);
                FAST.keywordFrequencyMap = (HashMap<String, KeywordFrequency>) fromString(br.readLine());
                br.close();
                fr.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            Run.logger.info("Preloading objects and queries!");
            preloadObjects();
            preloadQueries();

            Run.logger.info("Creating index!");
            create();
        }

        Run.logger.info("Creation Done! Time=" + this.creationTime);

        Run.logger.info("Searching!");
        search();
        Run.logger.info("Search Done! Time=" + searchTime);

        try {
            save();
        } catch (InvalidOutputFile e) {
            Run.logger.error(e.getMessage());
        }
    }

    private static Object fromString( String s ) throws IOException ,
                                                       ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
   }

    /** Write the object to a Base64 string. */
    private static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
}
