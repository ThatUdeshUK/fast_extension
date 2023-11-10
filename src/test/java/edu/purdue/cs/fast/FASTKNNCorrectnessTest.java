package edu.purdue.cs.fast;

import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class FASTKNNCorrectnessTest {
    private final PlacesKNNExperiment experiment;

    public FASTKNNCorrectnessTest() {
        experiment = new PlacesKNNExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_2000.json").toString(),
                "places_knn_seacnn",
                1000,
                100,
                5,
                0.0,
                5
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
                .map((list) -> list.stream().map((q) -> q.id).toList())
                .toList();

        List<List<Integer>> groundTruths = readGroundTruth();

        for (int i = 0; i < groundTruths.size(); i++) {
            List<Integer> result = results.get(i);
            List<Integer> groundTruth = groundTruths.get(i);

            List<Integer> resultSorted = result.stream().sorted().toList();
            List<Integer> groundTruthSorted = groundTruth.stream().sorted().toList();
            if (result.size() != groundTruth.size()) {
                System.out.println("Results: \t\t" + resultSorted);
                System.out.println("Ground Truth: \t" + groundTruthSorted);
            }

            Assertions.assertArrayEquals(resultSorted.toArray(Integer[]::new), groundTruthSorted.toArray(Integer[]::new));
            System.out.println("Test passed: " + i);
        }
    }

    private List<List<Integer>> readGroundTruth() {
        String path = System.getProperty("user.dir") + "/data/places_knn_ground_truth_seed_7.csv";
        ArrayList<List<Integer>> out = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader);

            String line;
            while ((line = br.readLine()) != null) {
                String[] trimed = line.substring(1, line.length() - 1).split(", ");
                out.add(Arrays.stream(trimed).filter((q) -> !q.isBlank()).map(Integer::parseInt).toList());
            }
        } catch (IOException ignore) {

        }

        return out;
    }
}