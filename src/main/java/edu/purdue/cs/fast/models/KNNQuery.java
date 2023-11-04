package edu.purdue.cs.fast.models;

import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.helper.TextualPredicate;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class KNNQuery extends Query {
    public Point location;
    public double ar;
    public int k;
    public PriorityQueue<DataObject> monitoredObjects;

    public KNNQuery(int id, List<String> keywords, Point location, int k, TextualPredicate predicate, int et) {
        super(id, keywords, predicate, et);
        this.location = location;
        this.k = k;
        this.ar = SpatioTextualConstants.xMaxRange;
    }

    public boolean pushUntilK(DataObject obj) {
        if (monitoredObjects == null) {
            monitoredObjects = new PriorityQueue<>(k, new EuclideanComparator(location));
        }

        monitoredObjects.add(obj);
        return monitoredObjects.size() >= k;
    }

    @Override
    public String toString() {
        return "KNNQuery{" +
                "id=" + id +
                ", keywords=" + keywords +
                ", location=" + location +
                ", k=" + k +
                ", predicate=" + predicate +
                ", et=" + et +
                ", deleted=" + deleted +
                '}';
    }

    static class EuclideanComparator implements Comparator<DataObject> {
        private final Point point;

        public EuclideanComparator(Point point) {
            this.point = point;
        }

        @Override
        public int compare(DataObject o1, DataObject o2) {
            double val1 = Math.pow(point.x - o1.location.x, 2) + Math.pow(point.y - o1.location.y, 2);
            double val2 = Math.pow(point.x - o2.location.x, 2) + Math.pow(point.y - o2.location.y, 2);

            return Double.compare(val2, val1);
        }
    }
}
