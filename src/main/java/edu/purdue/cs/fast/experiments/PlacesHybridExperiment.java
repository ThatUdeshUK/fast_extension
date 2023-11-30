package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesHybridExperiment extends PlacesExperiment {
    private final double knnRatio;
    private final int k;

    public PlacesHybridExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                                  int numQueries, int numObjects, int numKeywords, double srRate, int k,
                                  double knnRatio, int maxRange) {
        super(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, srRate, maxRange);
        this.k = k;
        this.knnRatio = knnRatio;
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        int knnQueryCount = (int) (numQueries * knnRatio);
        for (int i = 0; i < knnQueryCount; i++) {
            Place place = places.get(i);
            queries.add(place.toKNNQuery(i, numKeywords, k, numQueries + numObjects + 1));
        }
        int r = (int) (this.maxRange * srRate);
        for (int i = knnQueryCount; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toMinimalRangeQuery(i, r, this.maxRange, numKeywords, numQueries + numObjects + 1));
        }
    }

    @Override
    public void run() {
        init();

        Run.logger.info("Creating index!");
        create();
        Run.logger.info("Creation Done! Time=" + this.creationTime);

        Run.logger.info("Searching!");
        search();
        Run.logger.info("Search Done! Time=" + searchTime);

        List<String> headers = new ArrayList<>();
        headers.add("num_queries");
        headers.add("num_objects");
        headers.add("sr_rate");
        headers.add("k");
        headers.add("knn_ratio");

        List<String> values = new ArrayList<>();
        values.add("" + numQueries);
        values.add("" + numObjects);
        values.add("" + srRate);
        values.add("" + k);
        values.add("" + knnRatio);

        try {
            save(headers, values);
        } catch (InvalidOutputFile e) {
            Run.logger.error(e.getMessage());
        }
    }
}
