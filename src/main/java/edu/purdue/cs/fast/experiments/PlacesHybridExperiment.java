package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesHybridExperiment extends PlacesExperiment {
    private final double knnRatio;
    private final int k;

    public PlacesHybridExperiment(String outputPath, String inputPath) {
        super(outputPath, inputPath);

        this.name = "places_knn_00";
        this.numQueries = 2500000;
        this.numObjects = 100000;
        this.numKeywords = 3;
        this.srRate = 0.01;

        this.k = 5;
        this.knnRatio = 0.0;
    }

    public PlacesHybridExperiment(String outputPath, String inputPath, String name, int numQueries, int numObjects,
                                  int numKeywords, double srRate, int k, double knnRatio) {
        super(outputPath, inputPath);

        this.name = name;
        this.numQueries = numQueries;
        this.numObjects = numObjects;
        this.numKeywords = numKeywords;
        this.srRate = srRate;

        this.k = k;
        this.knnRatio = knnRatio;
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        int knnQueryCount = (int) (numQueries * knnRatio);
        for (int i = 0; i < knnQueryCount; i++) {
            Place place = places.get(i);
            queries.add(place.toKNNQuery(i, numKeywords, k, numQueries + numObjects + 1));
        }
        int r = (int) (SpatioTextualConstants.xMaxRange * srRate);
        for (int i = knnQueryCount; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toMinimalRangeQuery(i, r, SpatioTextualConstants.xMaxRange, numKeywords, numQueries + numObjects + 1));
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
        headers.add("k");
        headers.add("knn_ratio");

        List<String> values = new ArrayList<>();
        values.add("" + numQueries);
        values.add("" + numObjects);
        values.add("" + srRate);
        values.add("" + k);
        values.add("" + knnRatio);

        try {
            save(headers, values);
        } catch (InvalidOutputFile e) {
            System.out.println(e.getMessage());
        }
    }
}
