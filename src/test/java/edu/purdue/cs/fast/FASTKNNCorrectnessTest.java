package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.config.Config;
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


class FASTKNNCorrectnessTest {
    private final PlacesKNNExpireExperiment experiment;

    public FASTKNNCorrectnessTest() {
        FAST fast = new FAST(
                new Config(),
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
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_2000.json").toString(),
                fast,
                "places_knn_seacnn",
                1000,
                100,
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
                System.out.println("Results: \t\t" + resultSorted);
                System.out.println("Ground Truth: \t" + groundTruthSorted);
            }

            Assertions.assertArrayEquals(resultSorted.toArray(), groundTruthSorted.toArray());
            System.out.println("Test passed: " + i);
        }
    }

    private List<List<Integer>> readGroundTruth() {
        System.out.println("Running naive FAST KNN as the ground truth");
        NaiveFAST goldFast = new NaiveFAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(512, 512)
                ),
                512,
                9
        );

        PlacesKNNExpireExperiment goldExperiment = new PlacesKNNExpireExperiment(
                null,
                Paths.get(System.getProperty("user.dir") + "/data/places_dump_US_2000.json").toString(),
                goldFast,
                "places_knn_seacnn",
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
        goldExperiment.create();
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