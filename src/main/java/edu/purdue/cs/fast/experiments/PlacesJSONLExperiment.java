package edu.purdue.cs.fast.experiments;

import com.google.gson.Gson;
import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.parser.Place;
import edu.purdue.cs.fast.parser.PlaceObjectJSONL;
import edu.purdue.cs.fast.parser.PlaceQueryJSONL;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PlacesJSONLExperiment extends Experiment<Place> {
    protected final String queryPath;
    protected final String objectPath;
    protected int numQueries;
    protected int numObjects;
    protected int numKeywords;
    protected int maxRange;

    protected IndexType indexType = IndexType.FAST;

    public PlacesJSONLExperiment(String outputPath, String queryPath, String objectPath,
                                 SpatialKeywordIndex index, int numQueries, int numObjects, int numKeywords, int maxRange) {
        this.name = "fast_java";
        this.outputPath = outputPath;
        this.queryPath = queryPath;
        this.objectPath = objectPath;
        this.index = index;
        this.numQueries = numQueries;
        this.numObjects = numObjects;
        this.numKeywords = numKeywords;
        this.maxRange = maxRange;
    }

    private static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    @Override
    public void init() {
        Gson gson = new Gson();

        queries = new ArrayList<>();
        objects = new ArrayList<>();

        File queryFile = new File(queryPath);
        try {
            FileReader fileReader = new FileReader(queryFile);
            BufferedReader br = new BufferedReader(fileReader);

            Run.logger.info("Parsing the Query file!");
            long start = System.currentTimeMillis();
            int i = 0;
            while (queries.size() < numQueries) {
                String line = br.readLine();
                i++;
                PlaceQueryJSONL place = gson.fromJson(line, PlaceQueryJSONL.class);
                queries.add(place.toMinimalRangeQuery());
            }
            long end = System.currentTimeMillis();
            Run.logger.info("Done! Time=" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException("Wrong path is given: " + queryPath);
        }

        File objectFile = new File(queryPath);
        try {
            FileReader fileReader = new FileReader(objectFile);
            BufferedReader br = new BufferedReader(fileReader);

            Run.logger.info("Parsing the Object file!");
            long start = System.currentTimeMillis();
            int i = queries.size();
            while (objects.size() < numObjects) {
                String line = br.readLine();
                i++;
                PlaceObjectJSONL place = gson.fromJson(line, PlaceObjectJSONL.class);
                objects.add(place.toDataObject(i));
            }
            long end = System.currentTimeMillis();
            Run.logger.info("Done! Time=" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException("Wrong path is given: " + objectPath);
        }
    }

    @Override
    protected void generateQueries(List<Place> list) {

    }

    @Override
    protected void generateObjects(List<Place> list) {

    }

    @Override
    protected Metadata<String> generateMetadata() {
        Metadata metadata = new Metadata();
        metadata.add("num_queries", "" + numQueries);
        metadata.add("num_objects", "" + numObjects);
        return metadata;
    }

    @Override
    public void run() {
        init();
        Run.logger.info("Queries: " + queries.size() + "/" + numQueries);
        Run.logger.info("Objects: " + objects.size() + "/" + numObjects);

        System.gc();
        System.gc();
        System.gc();

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
