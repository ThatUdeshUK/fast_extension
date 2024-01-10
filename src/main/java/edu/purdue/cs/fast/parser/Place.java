package edu.purdue.cs.fast.parser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.*;

import java.util.ArrayList;

public class Place {
    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("geometry")
    @Expose
    public Geometry geometry;

    @SerializedName("properties")
    @Expose
    public Properties properties;

    public ArrayList<String> keywords() {
        return properties.tags;
    }

    public Point coordinate() {
        return new Point(geometry.coordinates.get(0), geometry.coordinates.get(1));
    }

    public void scale(Point min, Point max, double scaledTo) {
        geometry.coordinates.set(0, (geometry.coordinates.get(0) - min.x) * scaledTo / (max.x - min.x));
        geometry.coordinates.set(1, (geometry.coordinates.get(1) - min.y) * scaledTo / (max.y - min.y));
    }

    public Rectangle toRectangle(int r, double maxRange) {
        return new Rectangle(
                new Point(
                        Math.max(0.0, geometry.coordinates.get(0) - r),
                        Math.max(0.0, geometry.coordinates.get(1) - r)
                ),
                new Point(
                        Math.min(maxRange, geometry.coordinates.get(0) + r),
                        Math.min(maxRange, geometry.coordinates.get(1) + r)
                )
        );
    }

    public MinimalRangeQuery toMinimalRangeQuery(int qid, int r, double maxRange, int numKeywords, int expireTimestamp) {
        ArrayList<String> keywords = new ArrayList<>(properties.tags.subList(0, Math.min(properties.tags.size(), numKeywords)));
        return new MinimalRangeQuery(qid, keywords, toRectangle(r, maxRange), null, qid, expireTimestamp);
    }

    public Query toKNNQuery(int qid, int numKeywords, int k, int expireTimestamp, PlacesKNNExperiment.KNNType knnType) {
        ArrayList<String> keywords = new ArrayList<>(properties.tags.subList(0, Math.min(properties.tags.size(), numKeywords)));

        if (knnType == PlacesKNNExperiment.KNNType.FAST)
            return new KNNQuery(qid, keywords, coordinate(), k, null, qid, expireTimestamp);
        else if (knnType == PlacesKNNExperiment.KNNType.CkQST) {
            return new CkQuery(qid, keywords, coordinate().x, coordinate().y, k, qid, expireTimestamp);
        } else {
            throw new RuntimeException("Not implemented!");
        }
    }

    public DataObject toDataObject(int oid, int expireTimestamp) {
        return new DataObject(
                oid,
                new Point(geometry.coordinates.get(0), geometry.coordinates.get(1)),
                properties.tags,
                oid,
                expireTimestamp
        );
    }
}

class Geometry {
    @SerializedName("type")
    @Expose
    public String type;

    @SerializedName("coordinates")
    @Expose
    public ArrayList<Double> coordinates;
}

class Properties {
    @SerializedName("tags")
    @Expose
    public ArrayList<String> tags;
}

