package edu.purdue.cs.fast.parser;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import edu.purdue.cs.fast.baselines.fast.messages.LDataObject;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.Rectangle;

import java.util.ArrayList;


public class PlaceObjectJSONL {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("x")
    @Expose
    public double x;

    @SerializedName("y")
    @Expose
    public double y;

    @SerializedName("keywords")
    @Expose
    public ArrayList<String> keywords;

    public DataObject toDataObject(long st) {
        return new LDataObject(
                id,
                new Point(x, y),
                keywords,
                st,
                Long.MAX_VALUE
        );
    }
}

