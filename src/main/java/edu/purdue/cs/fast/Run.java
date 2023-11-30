package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.*;
import edu.purdue.cs.fast.helper.CleanMethod;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Run {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {

        Workload workload = Workload.MBR_EXPIRE;
        double srRate = 0.01;
        int k = 5;
        double knnRatio = 0.05;
        int numKeywords = 5;
        int numObjects = 100000;
        int fineGridGran = 512;
        int maxLevel = 9;
        int maxRange = 512;
        boolean pushToLowest = false;
        CleanMethod cleanMethod = CleanMethod.NO;

        ArrayList<Integer> numQueriesList = new ArrayList<>();
        numQueriesList.add(100000);
        numQueriesList.add(500000);
        numQueriesList.add(1000000);
        numQueriesList.add(2500000);
//        numQueriesList.add(5000000);

        for (int numQueries : numQueriesList) {
            FAST fast = new FAST(
                    new Rectangle(
                            new Point(0.0, 0.0),
                            new Point(maxRange, maxRange)
                    ),
                    fineGridGran,
                    maxLevel
            );
            fast.setPushToLowest(pushToLowest);
            fast.setCleaning(cleanMethod);

            PlacesExperiment experiment;
            String ds = Paths.get(args[1], "data/places_dump_US.geojson").toString();
            switch (workload) {
                case MBR_EXPIRE:
                    experiment = new PlacesExpireExperiment(
                            Paths.get(args[0], "results/output_places_US_mbr_exp.csv").toString(),
                            ds, fast, cleanMethod.name(), numQueries, numObjects, numKeywords, srRate, maxRange
                    );
                    break;
                case HYBRID:
                    experiment = new PlacesHybridExperiment(
                            Paths.get(args[0], "results/output_places_US_hybrid.csv").toString(),
                            ds, fast, cleanMethod.name(), numQueries, numObjects, numKeywords, srRate, k, knnRatio, maxRange
                    );
                    break;
                case KNN:
                    experiment = new PlacesKNNExperiment(
                            Paths.get(args[0], "results/output_places_US_knn.csv").toString(),
                            ds, fast, cleanMethod.name(), numQueries, numObjects, numKeywords, k, maxRange
                    );
                    break;
                case KNN_EXPIRE:
                    experiment = new PlacesKNNExpireExperiment(
                            Paths.get(args[0], "results/output_places_US_knn_exp.csv").toString(),
                            ds, fast, cleanMethod.name(), numQueries, numObjects, numKeywords, k, maxRange
                    );
                    break;
                default:
                    experiment = new PlacesExperiment(
                            Paths.get(args[0], "results/output_places_US_mbr.csv").toString(),
                            ds, fast, cleanMethod.name(), numQueries, numObjects, numKeywords, srRate, maxRange
                    );
            }
            experiment.setSaveStats(true);
            experiment.run();
        }
    }

    private enum Workload {
        MBR,
        MBR_EXPIRE,
        HYBRID,
        KNN,
        KNN_EXPIRE
    }
}
