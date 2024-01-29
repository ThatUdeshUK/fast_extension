package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.ckqst.AdoptCkQST;
import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.experiments.Experiment;
import edu.purdue.cs.fast.experiments.PlacesExperiment;
import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.ArrayList;

public class RunAdoptCkQST {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        String name = "adopt";
        if (args.length > 2)
            name = args[2];

        int k = 5;
        int numKeywords = 5;
        int numPreObjects = 100000;
        int numObjects = 100000;
        int maxRange = 512;
        int maxHeight = 9;
        CleanMethod cleanMethod = CleanMethod.NO;

        ArrayList<Integer> numQueriesList = new ArrayList<>();
        numQueriesList.add(100000);
        numQueriesList.add(500000);
        numQueriesList.add(1000000);
        numQueriesList.add(2500000);
        numQueriesList.add(5000000);
//        numQueriesList.add(10000000);
//        numQueriesList.add(20000000);

        for (int numQueries : numQueriesList) {
            AdoptCkQST ckQST = new AdoptCkQST(maxRange, maxRange, maxHeight);

            String ds = Paths.get(args[1], "data/places_dump_US.geojson").toString();
            PlacesExperiment experiment = new PlacesKNNExperiment(
                    Paths.get(args[0], "output_places_US_adoptckqst_preloaded.csv").toString(),
                    ds, ckQST, getExpName(name, cleanMethod), numPreObjects, 0, numQueries, numObjects, numKeywords, k, maxRange,
                    PlacesKNNExperiment.IndexType.AdoptCkQST
            );
            experiment.setSaveStats(true);
            experiment.init();
            experiment.run();
        }
    }

    private static String getExpName(String name, CleanMethod m) {
        return name + "_" + m.name();
    }

    private enum Workload {
        MBR,
        MBR_EXPIRE,
        HYBRID,
        KNN,
        KNN_EXPIRE,

        KNN_OBJ_EXPIRE,
    }
}
