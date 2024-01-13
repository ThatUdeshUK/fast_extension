package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


class CkQSTxFASTCorrectnessTest {

    private final int numQueries = 100000;
    private final int numObjects = 100000;
    private final PlacesKNNExperiment experiment;

    public CkQSTxFASTCorrectnessTest() {
        CkQST ckqst = new CkQST(512, 512, 9);

        experiment = new PlacesKNNExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/../data/places_dump_US.geojson").toString(),
                ckqst,
                "places_knn_seacnn",
                numObjects,
                0,
                numQueries,
                numObjects,
                5,
                5,
                512,
                PlacesKNNExperiment.KNNType.CkQST
        );
        experiment.setSeed(7);
        experiment.setSaveStats(false);
    }

    @Test
    public void testCorrectness() {
        experiment.run();

        List<List<Integer>> results = experiment.getResults().stream()
                .map((list) -> list.stream().map((q) -> q.id).collect(Collectors.toList()))
                .collect(Collectors.toList());

        List<List<Integer>> groundTruths = readGroundTruth();

        for (int i = 0; i < groundTruths.size(); i++) {
            List<Integer> result = results.get(i);
            List<Integer> groundTruth = groundTruths.get(i);

            List<Integer> resultSorted = result.stream().sorted().collect(Collectors.toList());
            List<Integer> groundTruthSorted = groundTruth.stream().sorted().collect(Collectors.toList());
            if (result.size() != groundTruth.size()) {
//                System.out.println("Results: \t\t" + resultSorted);
//                System.out.println("Ground Truth: \t" + groundTruthSorted);
                HashSet<Integer> inter = new HashSet<>(groundTruthSorted);
                resultSorted.forEach(inter::remove);
                System.out.println("Missing: \t" + inter);

                HashSet<Integer> inter2 = new HashSet<>(resultSorted);
                groundTruthSorted.forEach(inter2::remove);
                System.out.println("Extras: \t" + inter2);
            }

            Assertions.assertArrayEquals(resultSorted.toArray(), groundTruthSorted.toArray());
            System.out.println("Test passed: " + i);
        }
    }

    private List<List<Integer>> readGroundTruth() {
        System.out.println("Running FAST KNN as the ground truth");
        FAST fast = new FAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(512, 512)
                ),
                512,
                9
        );

        PlacesKNNExperiment fastExperiment = new PlacesKNNExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/../data/places_dump_US.geojson").toString(),
                fast,
                "places_knn_seacnn",
                numObjects,
                0,
                numQueries,
                numObjects,
                5,
                5,
                512,
                PlacesKNNExperiment.KNNType.FAST
        );
        fastExperiment.setSeed(7);
        fastExperiment.setSaveStats(false);
        fastExperiment.init();

        System.gc();
        System.gc();
        System.gc();

        fastExperiment.create();
        fastExperiment.preloadObjects();
        fastExperiment.search();

        List<List<Integer>> results = fastExperiment.getResults().stream()
                .map((list) -> list.stream().map((q) -> q.id).collect(Collectors.toList()))
                .collect(Collectors.toList());

        System.out.println("Naive FAST KNN Done!");

        return results;
    }
}