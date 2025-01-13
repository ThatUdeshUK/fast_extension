package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.fast.LFAST;
import edu.purdue.cs.fast.experiments.Experiment;
import edu.purdue.cs.fast.experiments.PlacesExperiment;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.ArrayList;

public class RunVanillaFAST {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        String name = "legacy_fast";
        if (args.length > 2)
            name = args[2];

//        double srRate = 0.01;
        int numKeywords = 5;
        int numObjects = 1000000;
        int numQueries = 5000000;
        int maxRange = 512;
        int maxHeight = 9;

//        ArrayList<Integer> numQueriesList = new ArrayList<>();
//        numQueriesList.add(100000);
//        numQueriesList.add(500000);
//        numQueriesList.add(1000000);
//        numQueriesList.add(2500000);
//        numQueriesList.add(5000000);

        ArrayList<Double> srRates = new ArrayList<>();
        // srRates.add(0.01);
        // srRates.add(0.02);
        // srRates.add(0.05);
        // srRates.add(0.10);
        // srRates.add(0.20);
        srRates.add(0.50);
//        for (int numQueries : numQueriesList) {
        for (Double srRate : srRates) {
            LFAST fast = new LFAST(
                    new Rectangle(
                            new Point(0.0, 0.0),
                            new Point(maxRange, maxRange)
                    ),
                    512,
                    9
            );

            String ds_name = "tweets_o200000_q10000000_scaled_max_keys3";
            // String ds = Paths.get(args[1], "data/exported/" + name + ".json").toString();
            String ds = Paths.get("/homes/ukumaras/scratch/twitter-data/" + ds_name + ".json").toString();
    
        //     String ds = Paths.get(args[1], "data/exported/places_o200000_q5000000_scaled.json").toString();
    
            PlacesExperiment experiment = new PlacesExperiment(
                    Paths.get(args[0], "output_places_US_legacy_fast_sr.csv").toString(),
                    ds, fast, name, 0, 0, numQueries, numObjects, numKeywords, srRate, maxRange,
                    Experiment.IndexType.LFAST
            );
            experiment.setSaveStats(true);
//            experiment.init();
            experiment.run();
//        }
        }
    }
}
