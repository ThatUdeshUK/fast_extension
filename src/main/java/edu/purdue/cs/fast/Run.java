package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;

import java.nio.file.Paths;
import java.util.List;

public class Run {
    public static void main(String[] args) {
        double srRate = 0.01;
        int k = 5;
        int numKeywords = 5;
        int numObjects = 100;

        List<Integer> numQueriesList = List.of(
//                100000,
//                500000,
                1000 //,
//                2500000 //,
//                5000000
        );

        for (int numQueries : numQueriesList) {
            PlacesKNNExperiment experiment = new PlacesKNNExperiment(
                    Paths.get(args[0], "results/output_places_US_knn_seacnn.csv").toString(),
                    Paths.get(args[1], "data/places_dump_US.geojson").toString(),
                    "places_knn",
                    numQueries,
                    numObjects,
                    numKeywords,
                    srRate,
                    k,
                    false
            );
            experiment.setSaveOutput();

            experiment.run();
        }
    }
}
