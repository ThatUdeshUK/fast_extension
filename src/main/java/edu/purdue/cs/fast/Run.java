package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;

import java.nio.file.Paths;
import java.util.List;

public class Run {
    public static void main(String[] args) {
        double srRate = 0.01;
        int k = 5;
        int numKeywords = 5;
        int numObjects = 100000;

        List<Integer> numQueriesList = List.of(
//                100000,
//                500000,
//                1000000,
                2500000
        );

        for (int numQueries : numQueriesList) {
            PlacesKNNExperiment experiment = new PlacesKNNExperiment(
                    Paths.get(args[0], "results/output_places_US_knn_seacnn_fixed_v2.csv").toString(),
                    Paths.get(args[1], "data/places_dump_US.geojson").toString(),
                    "places_knn_seacnn",
                    numQueries,
                    numObjects,
                    numKeywords,
                    srRate,
                    k
            );
//            experiment.setPushToLowest();
//            experiment.setSaveOutput();
            experiment.setSaveStats(false);

            experiment.run();
        }
    }
}
