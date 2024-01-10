package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.experiments.*;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.ArrayList;

public class RunNaive {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        String name = "expiring";
        Workload workload = Workload.KNN_EXPIRE;
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
////        numQueriesList.add(5000000);

        for (int numQueries : numQueriesList) {
            NaiveFAST fast = new NaiveFAST(
                    new Rectangle(
                            new Point(0.0, 0.0),
                            new Point(maxRange, maxRange)
                    ),
                    fineGridGran,
                    maxLevel
            );

            PlacesExperiment experiment;
            String ds = Paths.get(args[1], "data/places_dump_US.geojson").toString();
            switch (workload) {
                case MBR_EXPIRE:
                    experiment = new PlacesExpireExperiment(
                            Paths.get(args[0], "results/output_places_US_mbr_exp.csv").toString(),
                            ds, fast, getExpName(name, cleanMethod), numQueries, numObjects, numKeywords, srRate, maxRange
                    );
                    break;
                case HYBRID:
                    experiment = new PlacesHybridExperiment(
                            Paths.get(args[0], "results/output_places_US_hybrid.csv").toString(),
                            ds, fast, getExpName(name, cleanMethod), numQueries, numObjects, numKeywords, srRate, k, knnRatio, maxRange
                    );
                    break;
                case KNN:
                    experiment = new PlacesKNNExperiment(
                            Paths.get(args[0], "results/output_places_US_knn.csv").toString(),
                            ds, fast, getExpName(name, cleanMethod), numQueries, numObjects, numKeywords, k, maxRange,
                            PlacesKNNExperiment.KNNType.FAST
                    );
                    break;
                case KNN_EXPIRE:
                    experiment = new PlacesKNNExpireExperiment(
                            Paths.get(args[0], "results/output_places_US_knn_exp.csv").toString(),
                            ds, fast, getExpName(name, cleanMethod), numQueries, numObjects, numKeywords, k, maxRange,
                            PlacesKNNExperiment.KNNType.FAST
                    );
                    break;
                case KNN_OBJ_EXPIRE:
                    experiment = new PlacesKNNObjExpireExperiment(
                            Paths.get(args[0], "results/output_places_US_knn_naive_obj_exp.csv").toString(),
                            ds, fast, getExpName(name, cleanMethod), numQueries, numObjects, numKeywords, k, maxRange,
                            PlacesKNNExperiment.KNNType.FAST
                    );
                    break;
                default:
                    experiment = new PlacesExperiment(
                            Paths.get(args[0], "results/output_places_US_mbr.csv").toString(),
                            ds, fast, getExpName(name, cleanMethod), numQueries, numObjects, numKeywords, srRate, maxRange
                    );
            }
            experiment.setSaveStats(true);
            experiment.run();

//            try {
//                FileWriter fw = new FileWriter("results/all_knn_" + workload.name() + "_" + numQueries + ".txt");
//                BufferedWriter bw = new BufferedWriter(fw);
//
//                bw.write("id,ar,currentLevel,x,y\n");
//                fast.allKNNQueries().forEach(query -> {
//                    try {
//                        bw.write("" + query.id + ',' + query.ar + ',' + query.currentLevel + ',' + query.location.x + ',' + query.location.y + "\n");
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//
//                bw.close();
//                fw.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
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
