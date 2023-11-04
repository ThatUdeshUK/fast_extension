package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;

import java.util.ArrayList;
import java.util.List;

public class Run {
    public static void main(String[] args) {
//        PlacesExperiment experiment = new PlacesExperiment(
//                "/u/antor/u13/ukumaras/Projects/fast/fast/results/output_places_US.csv",
//                "/u/antor/u13/ukumaras/Projects/FAST/data/places_dump_US.geojson"
//        );

        double srRate = 0.01;
        int k = 5;
        int numKeywords = 5;
        int numObjects = 100000;

        List<Integer> numQueriesList = new ArrayList<>();
        numQueriesList.add(100000);
        numQueriesList.add(500000);
        numQueriesList.add(1000000);
        numQueriesList.add(2500000);
//        numQueriesList.add(5000000);

        for (int numQueries: numQueriesList) {
            System.out.println("Running experiment -> No. queries: " + numQueries);
            PlacesKNNExperiment experiment = new PlacesKNNExperiment(
                    "/u/antor/u13/ukumaras/Projects/fast_index/fast/results/output_places_US_knn_sea_cnn.csv",
                    "/u/antor/u13/ukumaras/Projects/FAST/data/places_dump_US.geojson",
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
