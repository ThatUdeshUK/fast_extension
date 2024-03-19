package edu.purdue.cs.fast.experiments;

import com.google.gson.Gson;
import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.parser.PlaceOld;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlacesOldExperiment extends Experiment<PlaceOld> {
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

    public PlacesOldExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
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

    public PlacesOldExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                               int numPreObjects, int numPreQueries, int numQueries, int numObjects, int numKeywords,
                               double srRate, int maxRange, IndexType indexType) {
        this(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, srRate, maxRange);
        this.numPreObjects = numPreObjects;
        this.numPreQueries = numPreQueries;
        this.indexType = indexType;
    }

    @Override
    public void init() {
        ArrayList<PlaceOld> places = new ArrayList<>();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

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

                PlaceOld place = gson.fromJson(line, PlaceOld.class);
                if (place != null && place.keywords() != null && !place.keywords().isEmpty()) {
                    double coordX = place.coordinate().x;
                    double coordY = place.coordinate().y;

                    if (coordX < minX) minX = coordX;
                    if (coordX > maxX) maxX = coordX;
                    if (coordY < minY) minY = coordY;
                    if (coordY > maxY) maxY = coordY;

                    Collections.sort(place.keywords());

                    places.add(place);
                }
            }
            long end = System.currentTimeMillis();
            Run.logger.info("Done! Time=" + (end - start));

            for (PlaceOld place : places) {
                place.scale(new Point(minX, minY), new Point(maxX, maxY), maxRange - 1);
            }

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
    protected void generateQueries(List<PlaceOld> places) {
        int r = (int) (maxRange * srRate);

        if (numPreQueries > 0) {
            this.preQueries = new ArrayList<>();
            for (int i = 0; i < numPreQueries; i++) {
                PlaceOld place = places.get(i);
                preQueries.add(place.toMinimalRangeQuery(i, r, maxRange, numKeywords, numPreQueries + numPreObjects + numQueries + numObjects + 1, indexType));
            }
        }

        this.queries = new ArrayList<>();
        for (int i = numPreQueries; i < numPreQueries + numQueries; i++) {
            PlaceOld place = places.get(i);
            queries.add(place.toMinimalRangeQuery(i, r, maxRange, numKeywords, numPreQueries + numPreObjects + numQueries + numObjects + 1, indexType));
        }
    }

    @Override
    protected void generateObjects(List<PlaceOld> places) {
        int totalQueries = numPreQueries + numQueries;
        if (numPreObjects > 0) {
            this.preObjects = new ArrayList<>();
            for (int i = totalQueries; i < totalQueries + numPreObjects; i++) {
                PlaceOld place = places.get(i);
                preObjects.add(place.toDataObject(i - totalQueries, totalQueries + numPreObjects + numObjects + 1, indexType));
            }
        }

        this.objects = new ArrayList<>();
        for (int i = totalQueries + numPreObjects; i < totalQueries + numPreObjects + numObjects; i++) {
            PlaceOld place = places.get(i);
            objects.add(place.toDataObject(i - totalQueries, totalQueries + numPreObjects + numObjects + 1, indexType));
        }
    }

    @Override
    protected Metadata generateMetadata() {
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

        Run.logger.info("Preloading objects and queries!");
        preloadObjects();
        preloadQueries();

        Run.logger.info("Creating index!");
        create();
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
}
