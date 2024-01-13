package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.ckqst.CkQST;
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

public class RunCkQST {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        String name = "ckqst";
        if (args.length > 2)
            name = args[2];

        int k = 5;
        int numKeywords = 5;
        int numPreObjects = 0;
        int numObjects = 1000000;
        int maxRange = 512;
        int maxHeight = 9;
        CleanMethod cleanMethod = CleanMethod.NO;

        ArrayList<Integer> numQueriesList = new ArrayList<>();
//        numQueriesList.add(1000);
//        numQueriesList.add(500000);
//        numQueriesList.add(1000000);
        numQueriesList.add(2500000);
//        numQueriesList.add(5000000);
//        numQueriesList.add(10000000);
//        numQueriesList.add(20000000);

        for (int numQueries : numQueriesList) {
            CkQST ckQST = new CkQST(maxRange, maxRange, maxHeight);

            String ds = Paths.get(args[1], "data/places_dump_US.geojson").toString();
            PlacesExperiment experiment = new PlacesKNNExperiment(
                    Paths.get(args[0], "output_places_US_ckqst_Obj1M.csv").toString(),
                    ds, ckQST, getExpName(name, cleanMethod), numPreObjects, 0, numQueries, numObjects, numKeywords, k, maxRange,
                    PlacesKNNExperiment.KNNType.CkQST
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