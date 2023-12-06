package edu.purdue.cs.fast.experiments;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.parser.Place;

import java.util.ArrayList;
import java.util.List;

public class PlacesKNNExpireExperiment extends PlacesKNNExperiment {
    public PlacesKNNExpireExperiment(String outputPath, String inputPath, SpatialKeywordIndex index, String name,
                                     int numQueries, int numObjects, int numKeywords, int k, int maxRange) {
        super(outputPath, inputPath, index, name, numQueries, numObjects, numKeywords, k, maxRange);
    }

    @Override
    protected void generateQueries(List<Place> places) {
        this.queries = new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {
            Place place = places.get(i);
            int et = this.randomizer.nextInt((int) (numObjects));
            queries.add(place.toKNNQuery(i, numKeywords, k, numQueries + et)); // Sets the random expiry
        }
    }

//    @Override
//    protected void generateObjects(List<Place> places) {
//        this.objects = new ArrayList<>();
//        for (int i = numQueries + 1; i < numQueries + numObjects; i++) {
//            Place place = places.get(i);
//            int et = this.randomizer.nextInt(i, numObjects + i);
//            System.out.println("Oid: " + i + " et: " + et);
//            objects.add(place.toDataObject(i, et)); // Sets the random expiry
//        }
//    }
}
