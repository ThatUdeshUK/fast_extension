package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


class CkQSTCorrectnessTest {
    private final PlacesKNNExperiment experiment;

    public CkQSTCorrectnessTest() {
        CkQST ckqst = new CkQST(512, 512, 9);

        experiment = new PlacesKNNExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_2000.json").toString(),
                ckqst,
                "places_knn_seacnn",
                100,
                0,
                1000,
                100,
                5,
                5,
                512,
                PlacesKNNExperiment.IndexType.CkQST
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
                System.out.println("Results: \t\t" + resultSorted);
                System.out.println("Ground Truth: \t" + groundTruthSorted);
            }

            Assertions.assertArrayEquals(resultSorted.toArray(), groundTruthSorted.toArray());
            System.out.println("Test passed: " + i);
        }
    }

    private List<List<Integer>> readGroundTruth() {
        System.out.println("Running naive FAST KNN as the ground truth");
        FAST goldFast = new FAST(
                new Config(),
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(512, 512)
                ),
                512,
                9
        );

        PlacesKNNExperiment goldExperiment = new PlacesKNNExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_2000.json").toString(),
                goldFast,
                "places_knn_seacnn",
                100,
                0,
                1000,
                100,
                5,
                5,
                512,
                PlacesKNNExperiment.IndexType.FAST
        );
        goldExperiment.setSeed(7);
        goldExperiment.setSaveStats(false);
        goldExperiment.init();

        System.gc();
        System.gc();
        System.gc();

        goldExperiment.create();
        goldExperiment.preloadObjects();
        goldExperiment.search();

        List<List<Integer>> results = goldExperiment.getResults().stream()
                .map((list) -> list.stream().map((q) -> q.id).collect(Collectors.toList()))
                .collect(Collectors.toList());

        System.out.println("Naive FAST KNN Done!");

        return results;
//        String path = System.getProperty("user.dir") + "/data/places_knn_ground_truth_seed_7.csv";
//        ArrayList<List<Integer>> out = new ArrayList<>();
//
//        try {
//            FileReader fileReader = new FileReader(path);
//            BufferedReader br = new BufferedReader(fileReader);
//
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] trimed = line.substring(1, line.length() - 1).split(", ");
//                out.add(Arrays.stream(trimed).filter((q) -> !q.isBlank()).map(Integer::parseInt).toList());
//            }
//        } catch (IOException ignore) {
//
//        }
//
//        return out;
    }
}