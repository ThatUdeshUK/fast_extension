package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;

import java.nio.file.Paths;
import java.util.List;

public class Run {
    public static void main(String[] args) {
        double srRate = 0.01;
        int k = 5;
        int numKeywords = 5;
        int numObjects = 100000;
        int fineGridGran = 512;
        int maxLevel = 9;
        int maxRange = 512;
        boolean pushToLowest = false;

        List<Integer> numQueriesList = List.of(
//                100000,
//                500000,
                1000000 //,
//                2500000,
//                5000000
        );

        for (int numQueries : numQueriesList) {
            FAST fast = new FAST(
                    new Rectangle(
                            new Point(0.0, 0.0),
                            new Point(maxRange, maxRange)
                    ),
                    fineGridGran,
                    maxLevel
            );
            if (pushToLowest)
                fast.setPushToLowest();

            PlacesKNNExperiment experiment = new PlacesKNNExperiment(
                    Paths.get(args[0], "results/output_places_US_knn_seacnn_mem.csv").toString(),
                    Paths.get(args[1], "data/places_dump_US.geojson").toString(),
                    fast,
                    "places_knn_seacnn",
                    numQueries,
                    numObjects,
                    numKeywords,
                    srRate,
                    k,
                    maxRange
            );
//            experiment.setSaveOutput();
            experiment.setSaveStats(false);

            experiment.run();
        }
    }
}
