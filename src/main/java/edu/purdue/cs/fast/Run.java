package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;

import java.util.List;

public class Run {
    public static void main(String[] args) {
        double srRate = 0.01;
        int k = 5;
        int numKeywords = 5;
        int numObjects = 100000;

        List<Integer> numQueriesList = List.of(
                100000,
                500000,
                1000000,
                2500000
        );

        for (int numQueries : numQueriesList) {
            PlacesKNNExperiment experiment = new PlacesKNNExperiment(
                    "./results/output_places_US_knn_seacnn.csv",
                    "./data/places_dump_US.geojson",
                    "places_knn",
                    numQueries,
                    numObjects,
                    numKeywords,
                    srRate,
                    k
            );

            experiment.run();
        }
    }
}
