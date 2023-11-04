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
        this.ar = Double.MAX_VALUE;
    }

    /**
     * Push to the monitored priority queue until there are `k` number of objects. Once it's filled upto `k`, the
     * `ar` of the KNNQuery is updated.
     *
     * @param obj DataObject to be added
     * @return Whether the KNN query currently monitors `k` objects or not
     */
    public boolean pushUntilK(DataObject obj) {
        if (monitoredObjects == null) {
            monitoredObjects = new PriorityQueue<>(k, new EuclideanComparator(location));
        }

        monitoredObjects.add(obj);
        boolean kFilled = monitoredObjects.size() >= k;
        if (monitoredObjects.size() > k) {
            monitoredObjects.poll();
        }

        if (kFilled) {
            assert monitoredObjects.peek() != null;

            Point o = monitoredObjects.peek().location;
            double maxX = o.x - location.x;
            double maxY = o.y - location.y;
            this.ar = Math.sqrt(maxX * maxX + maxY * maxY);
        }
        return kFilled;
    }

    @Override
    public Rectangle spatialBox() {
        return new Rectangle(location.x - ar, location.y - ar, location.x + ar, location.y + ar);
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
