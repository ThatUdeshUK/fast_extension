package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesKNNExperiment extends PlacesExperiment {
    protected final int k;

    public PlacesKNNExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                               int numQueries, int numObjects, int numKeywords, double srRate, int k, int maxRange) {
        super(outputPath, inputPath, index, maxRange);

        this.name = name;
        this.numQueries = numQueries;
        this.numObjects = numObjects;
        this.numKeywords = numKeywords;
        this.srRate = srRate;

        this.k = k;
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toKNNQuery(i, numKeywords, k, numQueries + numObjects + 1));
        }
    }

    @Override
    public void run() {
        init();
        System.gc();
        System.gc();
        System.gc();

        System.out.print("Creating index -> ");
        create();
        System.out.println("Done! Time=" + this.creationTime);

        System.out.print("Searching -> ");
        search();
        System.out.println("Done! Time=" + searchTime);

        List<String> headers = new ArrayList<>();
        headers.add("num_queries");
        headers.add("num_objects");
        headers.add("k");

        List<String> values = new ArrayList<>();
        values.add("" + numQueries);
        values.add("" + numObjects);
        values.add("" + k);

        try {
            save(headers, values);
        } catch (InvalidOutputFile e) {
            System.out.println(e.getMessage());
        }
    }
}
