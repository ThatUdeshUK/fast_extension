package edu.purdue.cs.fast.parser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.fast.messages.LDataObject;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.experiments.Experiment;
import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.models.*;

import java.util.ArrayList;

public class Place {
    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("x")
    @Expose
    public double x;

    @SerializedName("y")
    @Expose
    public double y;

    @SerializedName("keywords")
    @Expose
    public ArrayList<String> keywords;

    @SerializedName("scaled")
    @Expose
    public boolean scaled;

    public ArrayList<String> keywords() {
        return keywords;
    }

    public Point coordinate() {
        return new Point(x, y);
    }

    public Rectangle toRectangle(int r, double maxRange) {
        return new Rectangle(
                new Point(
                        Math.max(0.0, x - r),
                        Math.max(0.0, y - r)
                ),
                new Point(
                        Math.min(maxRange, x + r),
                        Math.min(maxRange, y + r)
                )
        );
    }

    public Query toMinimalRangeQuery(int qid, int r, double maxRange, int numKeywords, int expireTimestamp,
                                     Experiment.IndexType indexType) {
        ArrayList<String> keywords = new ArrayList<>(keywords().subList(0, Math.min(keywords().size(), numKeywords)));

        if (indexType == Experiment.IndexType.FAST)
            return new MinimalRangeQuery(qid, keywords, toRectangle(r, maxRange), null, qid, expireTimestamp);
        else if (indexType == Experiment.IndexType.LFAST) {
            Rectangle spatialRange = toRectangle(r, maxRange);
            Point loc = new Point((spatialRange.min.x + spatialRange.max.x) / 2, (spatialRange.min.x + spatialRange.max.x) / 2);
            return new LMinimalRangeQuery(qid, keywords, loc, spatialRange, null, qid, expireTimestamp);
        } else {
            throw new RuntimeException("Not implemented!");
        }
    }

    public Query toKNNQuery(int qid, int numKeywords, int k, int expireTimestamp, Experiment.IndexType indexType) {
        ArrayList<String> keywords = new ArrayList<>(keywords().subList(0, Math.min(keywords().size(), numKeywords)));

        if (indexType == PlacesKNNExperiment.IndexType.FAST)
            return new KNNQuery(qid, keywords, coordinate(), k, null, qid, expireTimestamp);
        else if (indexType == PlacesKNNExperiment.IndexType.CkQST) {
            return new CkQuery(qid, keywords, coordinate().x, coordinate().y, k, qid, expireTimestamp);
        } else if (indexType == PlacesKNNExperiment.IndexType.AdoptCkQST) {
            Point loc = new Point(x, y);
            Rectangle spatialRange = toRectangle(515, 512);
            return new LMinimalRangeQuery(qid, keywords, loc, spatialRange, null, qid, expireTimestamp);
        } else {
            throw new RuntimeException("Not implemented!");
        }
    }

    public DataObject toDataObject(int oid, int expireTimestamp, Experiment.IndexType indexType) {
        if (indexType == Experiment.IndexType.FAST || indexType == Experiment.IndexType.CkQST)
            return new DataObject(
                    oid,
                    new Point(x, y),
                    keywords,
                    oid,
                    expireTimestamp
            );
        else if (indexType == Experiment.IndexType.LFAST || indexType == Experiment.IndexType.AdoptCkQST) {
            return new LDataObject(
                    oid,
                    new Point(x, y),
                    keywords,
                    oid,
                    expireTimestamp
            );
        } else {
            throw new RuntimeException("Not implemented!");
        }

    }
}

