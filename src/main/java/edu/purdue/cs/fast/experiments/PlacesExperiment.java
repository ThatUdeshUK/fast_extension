package edu.purdue.cs.fast.experiments;

import com.google.gson.Gson;
import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.parser.Place;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlacesExperiment extends Experiment {
    protected final String inputPath;
    protected int numQueries;
    protected int numObjects;
    protected int numKeywords;
    protected double srRate;

    protected ArrayList<Place> places;

    public PlacesExperiment(String outputPath, String inputPath) {
        this.name = "places";
        this.outputPath = outputPath;
        this.inputPath = inputPath;
        this.numQueries = 1000000;
        this.numObjects = 100000;
        this.numKeywords = 3;
        this.srRate = 0.01;

        int fineGridGran = 512;
        int maxLevel = 9;

        this.fast = new FAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(SpatioTextualConstants.xMaxRange, SpatioTextualConstants.yMaxRange)
                ),
                fineGridGran,
                maxLevel
        );
    }

    void loadData() {
        ArrayList<Place> places = new ArrayList<>();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        File file = new File(inputPath);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            int lineCount = numQueries + numObjects;

            System.out.print("Parsing the Places file -> ");
            long start = System.currentTimeMillis();
            Gson gson = new Gson();
            while (places.size() < lineCount) {
                Place place = gson.fromJson(br.readLine(), Place.class);
                if (place != null && place.keywords() != null && !place.keywords().isEmpty()) {
                    double coordX = place.coordinate().x;
                    double coordY = place.coordinate().y;

                    if (coordX < minX) minX = coordX;
                    if (coordX > maxX) maxX = coordX;
                    if (coordY < minY) minY = coordY;
                    if (coordY > maxY) maxY = coordY;

                    places.add(place);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("Done! Time=" + (end - start));

            for (Place place : places) {
                place.scale(new Point(minX, minY), new Point(maxX, maxY), SpatioTextualConstants.xMaxRange - 1);
            }

            System.out.print("Shuffling -> ");
            start = System.currentTimeMillis();
            Collections.shuffle(places);
            end = System.currentTimeMillis();
            System.out.println("Done! Time=" + (end - start));
        } catch (IOException ignore) {
        }

        this.places = places;
    }

    @Override
    protected List<Query> generateQueries() {
        ArrayList<Query> queries = new ArrayList<>();
        int r = (int) (SpatioTextualConstants.xMaxRange * srRate);
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toMinimalRangeQuery(i, r, SpatioTextualConstants.xMaxRange, numKeywords, numQueries + numObjects + 1));
        }
        return queries;
    }

    @Override
    protected List<DataObject> generateObjects() {
        ArrayList<DataObject> objects = new ArrayList<>();
        for (int i = numQueries + 1; i < numQueries + numObjects; i++) {
            Place place = places.get(i);
            objects.add(place.toDataObject(i));
        }
        return objects;
    }

    @Override
    public void run() {
        loadData();

        System.out.print("Creating index -> ");
        create();
        System.out.println("Done! Time=" + this.creationTime);

        System.out.print("Searching -> ");
        search();
        System.out.println("Done! Time=" + searchTime);

        List<String> headers = new ArrayList<>();
        headers.add("num_queries");
        headers.add("num_objects");
        headers.add("sr_rate");

        List<String> values = new ArrayList<>();
        values.add("" + numQueries);
        values.add("" + numObjects);
        values.add("" + srRate);

        try {
            save(headers, values);
        } catch (InvalidOutputFile e) {
            System.out.println(e.getMessage());
        }
    }
}