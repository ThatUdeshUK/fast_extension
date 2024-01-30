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

public class RunLFAST {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        String name = "legacy_fast";
        if (args.length > 2)
            name = args[2];

        double srRate = 0.01;
        int numKeywords = 5;
        int numObjects = 100000;
        int maxRange = 512;
        int maxHeight = 9;

        ArrayList<Integer> numQueriesList = new ArrayList<>();
        numQueriesList.add(100000);
        numQueriesList.add(500000);
        numQueriesList.add(1000000);
        numQueriesList.add(2500000);
        numQueriesList.add(5000000);
//        numQueriesList.add(10000000);
//        numQueriesList.add(20000000);

        for (int numQueries : numQueriesList) {
            LFAST fast = new LFAST(
                    new Rectangle(
                            new Point(0.0, 0.0),
                            new Point(maxRange, maxRange)
                    ),
                    512,
                    9
            );

            String ds = Paths.get(args[1], "data/places_dump_US.geojson").toString();
            PlacesExperiment experiment = new PlacesExperiment(
                    Paths.get(args[0], "output_places_US_legacy_fast.csv").toString(),
                    ds, fast, name, 0, 0, numQueries, numObjects, numKeywords, srRate, maxRange,
                    Experiment.IndexType.LFAST
            );
            experiment.setSaveStats(true);
//            experiment.init();
            experiment.run();
        }
    }
}
