package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.baselines.fast.LFAST;
import edu.purdue.cs.fast.baselines.naivembr.NaiveMBR;
import edu.purdue.cs.fast.experiments.Experiment;
import edu.purdue.cs.fast.experiments.PlacesExperiment;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


class FASTvsCkMBRCorrectnessTest {
    private final PlacesExperiment experiment;
    private final int numObjects = 10000;
    private final int numQueries = 10000;

    public FASTvsCkMBRCorrectnessTest() {
        LFAST fast = new LFAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(512, 512)
                ),
                512,
                9
        );

        experiment = new PlacesExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/../data/places_dump_US.geojson").toString(),
                fast,
                "places_knn_seacnn",
                0,
                0,
                numQueries,
                numObjects,
                5,
                0.01,
                512,
                Experiment.IndexType.LFAST
        );
        experiment.setSeed(7);
        experiment.setSaveStats(false);
    }

    @Test
    public void testCorrectness() {
        experiment.init();
        System.out.println("Test data loaded!");
        experiment.create();
        System.out.println("Index Created!");
        experiment.search();
        System.out.println("Search Completed!");

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
                System.out.println("Results: \t\t" + resultSorted);
                System.out.println("Ground Truth: \t" + groundTruthSorted);
            }

            Assertions.assertArrayEquals(resultSorted.toArray(), groundTruthSorted.toArray());
            System.out.println("Test passed: " + i);
        }
    }

    private List<List<Integer>> readGroundTruth() {
        System.out.println("Running naive FAST KNN as the ground truth");
        CkQST gold = new CkQST(512, 512, 9);

        PlacesExperiment goldExperiment = new PlacesExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/../data/places_dump_US.geojson").toString(),
                gold,
                "places_knn_seacnn",
                0,
                0,
                numQueries,
                numObjects,
                5,
                0.01,
                512,
                Experiment.IndexType.LFAST
        );
        goldExperiment.setSeed(7);
        goldExperiment.setSaveStats(false);

        goldExperiment.init();
        goldExperiment.create();
        goldExperiment.search();

        List<List<Integer>> results = goldExperiment.getResults().stream()
                .map((list) -> list.stream().map((q) -> q.id).collect(Collectors.toList()))
                .collect(Collectors.toList());

        System.out.println("Naive FAST KNN Done!");

        return results;
    }
}