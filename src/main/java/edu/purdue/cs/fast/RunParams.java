package edu.purdue.cs.fast;

import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.experiments.*;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RunParams {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        String name = "inc";
        if (args.length > 2)
            name = args[2];

        int k = 5;
        int numKeywords = 5;
        int numObjects = 1000000;
        int numQueries = 1000000;
        int fineGridGran = 512;
        int maxLevel = 9;
        int maxRange = 512;
        CleanMethod cleanMethod = CleanMethod.NO;

        double arThresh = 25.0;

        ArrayList<Integer> degParams = new ArrayList<>();
        degParams.add(0);
        degParams.add(10);
        degParams.add(25);
        degParams.add(50);
        degParams.add(75);
        degParams.add(100);
        degParams.add(250);

        for (int degRatio : degParams) {
            FAST fast = new FAST(
                    new Rectangle(
                            new Point(0.0, 0.0),
                            new Point(maxRange, maxRange)
                    ),
                    fineGridGran,
                    maxLevel
            );
            FAST.config.INCREMENTAL_DESCENT = true;
            FAST.config.KNN_DEGRADATION_RATIO = degRatio;
            FAST.config.KNN_DEGRADATION_AR = arThresh;
            fast.setPushToLowest(false);
            fast.setCleaning(cleanMethod);

            PlacesExperiment experiment;
            String ds = Paths.get(args[1], "data/places_dump_US.geojson").toString();
            experiment = new PlacesKNNExperiment(
                            Paths.get(args[0], "fast_knn_params_deg.csv").toString(),
                            ds, fast, getExpName(name), 0, 0, numQueries, numObjects, numKeywords, k, maxRange,
                            PlacesKNNExperiment.IndexType.FAST
                    );
            experiment.setSaveStats(false);
            experiment.run();

        }
    }

    private static String getExpName(String name) {
        return name;
    }
}
