package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.baselines.ckqst.models.CkObject;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.parser.Place;

import java.util.*;

public class PlacesKNNExperiment extends PlacesExperiment {
    protected final int k;
    protected KNNType knnType;

    public PlacesKNNExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                               int numQueries, int numObjects, int numKeywords, int k, int maxRange, KNNType knnType) {
        super(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, 0.0, maxRange);
        this.k = k;
        this.knnType = knnType;
    }

    public PlacesKNNExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                               int numPreObjects, int numPreQueries, int numQueries, int numObjects, int numKeywords,
                               int k, int maxRange, KNNType knnType) {
        super(outputPath, inputPath, index, name, numPreObjects, numPreQueries, numQueries, numObjects, numKeywords, 0.0, maxRange);
        this.k = k;
        this.knnType = knnType;
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            queries.add(place.toKNNQuery(i, numKeywords, k, numPreObjects + numPreObjects + numQueries + numObjects + 1, knnType));
        }
    }

    @Override
    protected Metadata generateMetadata() {
        Metadata metadata = new Metadata();
        metadata.add("num_queries", "" + numQueries);
        metadata.add("num_objects", "" + numObjects);
        metadata.add("k", "" + k);
        return metadata;
    }

    public enum KNNType {
        FAST,
        CkQST
    }
}
