package edu.purdue.cs.fast.parser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import edu.purdue.cs.fast.baselines.fast.messages.LDataObject;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.models.*;

import java.util.ArrayList;


public class PlaceQueryJSONL {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("min_x")
    @Expose
    public double min_x;

    @SerializedName("min_y")
    @Expose
    public double min_y;

    @SerializedName("max_x")
    @Expose
    public double max_x;

    @SerializedName("max_y")
    @Expose
    public double max_y;

    @SerializedName("keywords")
    @Expose
    public ArrayList<String> keywords;

    @SerializedName("et")
    @Expose
    public long et;

    public Query toMinimalRangeQuery() {
        Rectangle spatialRange = new Rectangle(min_x, min_y, max_x, max_y);
        Point loc = new Point((spatialRange.min.x + spatialRange.max.x) / 2, (spatialRange.min.x + spatialRange.max.x) / 2);
        return new LMinimalRangeQuery(id, keywords, loc, spatialRange, null, 0, et);
    }
}

