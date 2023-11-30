package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesExpireExperiment extends PlacesExperiment {
    public PlacesExpireExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                                  int numQueries, int numObjects, int numKeywords, double srRate, int maxRange) {
        super(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, srRate, maxRange);
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        int r = (int) (maxRange * srRate);
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            int et = this.randomizer.nextInt(numObjects);
            queries.add(place.toMinimalRangeQuery(i, r, maxRange, numKeywords, numQueries +  et)); // Sets the random expiry
        }
    }
}
