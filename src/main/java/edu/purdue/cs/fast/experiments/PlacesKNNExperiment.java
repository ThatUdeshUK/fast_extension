package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.parser.Place;
import edu.purdue.cs.fast.parser.PlaceOld;

import java.util.*;

public class PlacesKNNExperiment extends PlacesExperiment {
    protected final int k;
    public PlacesKNNExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                               int numQueries, int numObjects, int numKeywords, int k, int maxRange, IndexType indexType) {
        super(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, 0.0, maxRange);
        this.k = k;
        this.indexType = indexType;
    }

    public PlacesKNNExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                               int numPreObjects, int numPreQueries, int numQueries, int numObjects, int numKeywords,
                               int k, int maxRange, IndexType indexType) {
        super(outputPath, inputPath, index, name, numPreObjects, numPreQueries, numQueries, numObjects, numKeywords, 0.0, maxRange, indexType);
        this.k = k;
        this.indexType = indexType;
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toKNNQuery(i, numKeywords, k, numPreObjects + numPreObjects + numQueries + numObjects + 1, indexType));
        }
    }

    @Override
    protected Metadata<String> generateMetadata() {
        Metadata metadata = new Metadata();
        metadata.add("num_queries", "" + numQueries);
        metadata.add("num_objects", "" + numObjects);
        metadata.add("k", "" + k);
        if (indexType == IndexType.FAST) {
            metadata.add("knn_deg_ratio", "" + FAST.config.KNN_DEGRADATION_RATIO);
            metadata.add("knn_ar_thresh", "" + FAST.config.KNN_DEGRADATION_AR);
        }
        return metadata;
    }
}
