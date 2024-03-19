package edu.purdue.cs.fast.models;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextualPredicate;
import edu.purdue.cs.fast.structures.BoundedPriorityQueue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class KNNQuery extends Query {
    public static String CSV_HEADER = "id,x,y,keywords,k,ar,current_level,st,et";
    private final Rectangle spatialBox;
    public Point location;
    public double ar;
    public int k;
    public int kHat;
    public int currentLevel = -1;
    private BoundedPriorityQueue<DataObject> monitoredObjects;

    public KNNQuery(int id, List<String> keywords, Point location, int k, TextualPredicate predicate, long st, long et) {
        super(id, keywords, predicate, st, et);
        this.location = location;
        this.k = k;
        this.kHat = k; //+ FAST.config.RHO;
        this.ar = Double.MAX_VALUE;
        this.spatialBox = new Rectangle(0, 0, 0, 0);
    }

    /**
     * Push objects until there are `k` number of objects. Once it's filled upto `k`, the
     * `ar` of the KNNQuery is updated.
     *
     * @param obj DataObject to be added
     * @return Whether the KNN query currently monitors `k` objects or not
     */
    public boolean pushUntilK(DataObject obj) {
        return pushUntilK_(obj, k);
    }

    /**
     * Push objects until there are `k + rho` number of objects. Once it's filled upto `k + rho`,
     * the `ar` of the KNNQuery is updated.
     *
     * @param obj DataObject to be added
     * @return Whether the KNN query currently monitors `k + rho` objects or not
     */
    public boolean pushUntilKHat(DataObject obj) {
        return pushUntilK_(obj, kHat);
    }

    private boolean pushUntilK_(DataObject obj, int kStar) {
        if (monitoredObjects == null) {
            monitoredObjects = new BoundedPriorityQueue<>(kStar, new EuclideanComparator(location));
        }

        if (FAST.config.INCREMENTAL_DESCENT) {
            if (!monitoredObjects.contains(obj))
                monitoredObjects.add(obj);
            else return monitoredObjects.isFull();
        } else
            monitoredObjects.add(obj);

        boolean kStarFilled = monitoredObjects.isFull();
        if (kStarFilled) {
            assert monitoredObjects.peek() != null;

            Point o = monitoredObjects.peek().location;
            double maxX = o.x - location.x;
            double maxY = o.y - location.y;
            this.ar = Math.sqrt(maxX * maxX + maxY * maxY);
//            if (id == 7188) {
//                System.out.println("fast - obj:" + obj.id + ", objk:" + obj.keywords + ", loc:" + location + ", ar:" + SpatialHelper.getDistanceInBetween(location, monitoredObjects.peek().location) + ", keys:" + keywords);
//                System.out.println(SpatialHelper.getDistanceInBetween(location, obj.location));
//            }
        }

//        if (id == 65) {
//            System.out.println("obj:" + obj.id + ", objk:" + obj.keywords + ", loc:" + location + ", ar:" + ar + ", keys:" + keywords + ", cl:" + currentLevel);
//        }
        return kStarFilled;
    }

    /**
     * Whether there are `k` number of objects.
     *
     * @return Whether the KNN query currently monitors `k` objects or not
     */
    public boolean kFilled() {
        if (monitoredObjects == null) {
            return false;
        }

        return monitoredObjects.isFull();
    }

    /**
     * Calculate the optimal level for a KNN query based on it's AR
     * Used only in direct descend strategy
     *
     * @return Optimal level for the KNN query
     */
    public int calcMinSpatialLevel() {
        return Math.max((int) (Math.log((ar / FAST.context.localXstep)) / Math.log(2)), 0);
//        return Math.max((int) (Math.log((ar * 2 / FAST.context.localXstep)) / Math.log(2)), 0);
//        return Math.max(((int) (Math.log((ar * 2 / FAST.context.localXstep)) / Math.log(2)))+1, 0);
//        return Math.max((int) Math.ceil(Math.log((ar * 2 / FAST.context.localXstep)) / Math.log(2)), 0);
    }

    public PriorityQueue<DataObject> getMonitoredObjects() {
        return monitoredObjects;
    }

    @Override
    public Rectangle spatialBox() {
        this.spatialBox.min.x = location.x - ar;
        this.spatialBox.min.y = location.y - ar;
        this.spatialBox.max.x = location.x + ar;
        this.spatialBox.max.y = location.y + ar;
        return this.spatialBox;
    }

    @Override
    public String toString() {
        return "KNNQuery{" +
                "id=" + id +
                ", location=" + location +
                ", keywords=" + keywords +
                ", k=" + k +
                ", ar=" + ar +
                ", cl=" + currentLevel +
                ", et=" + et +
                '}';
    }

    public String toCSV() {
        String keyword_str = String.join("|", keywords);
        return id + "," + location.x + "," + location.y + "," + keyword_str +
                "," + k + "," + ar + "," + currentLevel + "," + st + "," + et;
    }

    public static class EuclideanComparator implements Comparator<DataObject> {
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
