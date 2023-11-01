package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesKNNExperiment extends PlacesExperiment {
    private final int k;

    public PlacesKNNExperiment(String outputPath, String inputPath) {
        super(outputPath, inputPath);

        this.name = "places_knn_00";
        this.numQueries = 2500000;
        this.numObjects = 100000;
        this.numKeywords = 3;

        this.k = 5;
    }

    public PlacesKNNExperiment(String outputPath, String inputPath, String name, int numQueries, int numObjects,
                               int numKeywords, double srRate, int k) {
        super(outputPath, inputPath);

        this.name = name;
        this.numQueries = numQueries;
        this.numObjects = numObjects;
        this.numKeywords = numKeywords;
        this.srRate = srRate;

        this.k = k;
    }

    @Override
    protected List<Query> generateQueries() {
        ArrayList<Query> queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toKNNQuery(i, numKeywords, k, numQueries + numObjects + 1));
        }
        return queries;
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
