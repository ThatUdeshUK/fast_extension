package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.experiments.PlacesKNNExpireExperiment;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


class FASTKNNCleanTest {
    private final PlacesKNNExpireExperiment experiment;

    public FASTKNNCleanTest() {
        FAST fast = new FAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(512, 512)
                ),
                512,
                9
        );
        fast.setCleaning(CleanMethod.EXPIRE_KNN);

        experiment = new PlacesKNNExpireExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_10000.json").toString(),
                fast,
                "places_knn_seacnn",
                5000,
                2500,
                5,
                5,
                512,
                PlacesKNNExperiment.IndexType.FAST
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
                System.out.println();
                System.out.println("Results: \t\t" + resultSorted);
                System.out.println("Ground Truth: \t" + groundTruthSorted);
            }

            Assertions.assertArrayEquals(resultSorted.toArray(), groundTruthSorted.toArray());
            System.out.println("Test passed: " + i);
        }
    }

    private List<List<Integer>> readGroundTruth() {
        System.out.println("Running FAST KNN without cleaning as the ground truth");
        FAST goldFast = new FAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(512, 512)
                ),
                512,
                9
        );

        PlacesKNNExpireExperiment goldExperiment = new PlacesKNNExpireExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_10000.json").toString(),
                goldFast,
                "places_knn_seacnn",
                5000,
                2500,
                5,
                5,
                512,
                PlacesKNNExperiment.IndexType.FAST
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