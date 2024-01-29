package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.SpatialKeywordIndex;
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
            queries.add(place.toKNNQuery(i, numKeywords, k, numPreObjects + numPreQueries + numQueries + numObjects + 1, IndexType.FAST));
        }
        int r = (int) (this.maxRange * srRate);
        for (int i = knnQueryCount; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toMinimalRangeQuery(i, r, this.maxRange, numKeywords, numPreObjects + numPreQueries + numQueries + numObjects + 1, IndexType.FAST));
        }
    }

    @Override
    protected Metadata generateMetadata() {
        Metadata metadata = new Metadata();
        metadata.add("num_queries", "" + numQueries);
        metadata.add("num_objects", "" + numObjects);
        metadata.add("k", "" + k);
        metadata.add("knn_ratio", "" + knnRatio);
        return metadata;
    }
}
