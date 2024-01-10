package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesKNNObjExpireExperiment extends PlacesKNNExpireExperiment {
    public PlacesKNNObjExpireExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                                        int numQueries, int numObjects, int numKeywords, int k, int maxRange, KNNType knnType) {
        super(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, k, maxRange, knnType);
    }

    @Override
    protected void generateObjects(List<Place> places) {
        this.objects = new ArrayList<>();
        for (int i = numQueries + 1; i < numQueries + numObjects; i++) {
            Place place = places.get(i);
            int et = this.randomizer.nextInt(numObjects / 10);
            objects.add(place.toDataObject(i, i+et)); // Sets the random expiry
        }
    }
}
