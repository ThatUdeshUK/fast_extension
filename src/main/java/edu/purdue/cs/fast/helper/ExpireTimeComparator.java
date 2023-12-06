package edu.purdue.cs.fast.helper;

import edu.purdue.cs.fast.models.DataObject;

import java.util.Comparator;

public class ExpireTimeComparator implements Comparator<DataObject> {
    public ExpireTimeComparator() {}

    @Override
    public int compare(DataObject o1, DataObject o2) {
        return Double.compare(o1.et, o2.et);
    }
}

