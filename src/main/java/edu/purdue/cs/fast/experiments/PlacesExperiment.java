package edu.purdue.cs.fast.experiments;

import com.google.gson.Gson;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.parser.Place;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlacesExperiment extends Experiment<Place> {
    protected final String inputPath;
    protected int numQueries;
    protected int numObjects;
    protected int numKeywords;
    protected double srRate;
    protected int maxRange;
    private final Random randomizer;

    public PlacesExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, int maxRange) {
        this.name = "places";
        this.outputPath = outputPath;
        this.inputPath = inputPath;
        this.numQueries = 1000000;
        this.numObjects = 100000;
        this.numKeywords = 3;
        this.srRate = 0.01;
        this.index = index;
        this.maxRange = maxRange;
        this.randomizer = new Random(seed);
    }

    @Override
    public void init() {
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
                place.scale(new Point(minX, minY), new Point(maxX, maxY), maxRange - 1);
            }

            System.out.print("Shuffling -> ");
            start = System.currentTimeMillis();
            Collections.shuffle(places, randomizer);
            end = System.currentTimeMillis();
            System.out.println("Done! Time=" + (end - start));
        } catch (IOException e) {
            throw new RuntimeException("Wrong path is given: " + inputPath);
        }

        generateQueries(places);
        generateObjects(places);
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        int r = (int) (maxRange * srRate);
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            int et = randomizer.nextInt(i, numQueries + numObjects + i);
            queries.add(place.toMinimalRangeQuery(i, r, maxRange, numKeywords, et));
        }
    }

    @Override
    protected void generateObjects(List<Place> places) {
        this.objects = new ArrayList<>();
        for (int i = numQueries + 1; i < numQueries + numObjects; i++) {
            Place place = places.get(i);
            int et = randomizer.nextInt(i, numQueries + numObjects + i);
            objects.add(place.toDataObject(i - numQueries - 1, et));
        }
    }

    @Override
    public void run() {
        init();

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
