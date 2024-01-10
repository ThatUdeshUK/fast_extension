package edu.purdue.cs.fast.baselines.ckqst.models;

import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.structures.BoundedPriorityQueue;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class CkQuery extends Query {
    public XYPoint location;
    public double sr;
    public BoundedPriorityQueue<Double> srs;
    public int k;

    public CkQuery(int id, List<String> keywords, double x, double y, int k, long st, long et) {
        super(id, keywords, null, st, et);
        this.location = new XYPoint(x, y);
        this.sr = Double.MAX_VALUE;
        this.k = k;
    }

    public boolean containsPoint(XYPoint p) {
        boolean isInRectangle = p.x >= location.x - sr && p.x <= location.x + sr &&
                p.y >= location.y - sr && p.y <= location.y + sr;

        if (isInRectangle) {
            return (p.x - location.x) * (p.x - location.x) + (p.y - location.y) * (p.y - location.y) <= sr * sr;
        }
        return false;
    }

    public void updateSR(CkObject obj) {
        // ASSUMPTION: Paper doesn't include details on how to maintain the SR. We are maintaining a priority queue.
        if (srs == null) {
            srs = new BoundedPriorityQueue<>(k, (a, b) -> {
                if (b - a > 0) {
                    return 1;
                } else if (b - a < 0) {
                    return -1;
                } else
                    return 0;
            });
        }

        double deltaY = obj.y - location.y;
        double deltaX = obj.x - location.x;
        double newSr = Math.sqrt(deltaY * deltaY + deltaX * deltaX);
        srs.add(newSr);
//        if (id == 3002 && obj.id <= 1000) {
//            System.out.println("DEBUG!");
//            System.out.println(srs);
//        }

        if (srs.size() >= k && !srs.isEmpty()) {
            sr = srs.peek();
        }
    }

    @Override
    public String toString() {
        return "CkQST{" +
                "id=" + id +
                ", keywords=" + keywords +
                ", (x, y)=(" + location.x + ", " + location.y + ")" +
                ", k=" + k +
                ", sr=" + sr +
                '}';
    }

    @Override
    public Rectangle spatialBox() {
        throw new RuntimeException("ERROR!!! THIS SHOULD NEVER HAPPEN!");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof CkQuery))
            return false;
        return (this.id == ((CkQuery) other).id);
    }

    static class ObjectComparator implements Comparator<CkObject> {
        private final XYPoint point;

        public ObjectComparator(XYPoint point) {
            this.point = point;
        }

        @Override
        public int compare(CkObject o1, CkObject o2) {
            double val1 = Math.pow(point.x - o1.x, 2) + Math.pow(point.y - o1.y, 2);
            double val2 = Math.pow(point.x - o2.x, 2) + Math.pow(point.y - o2.y, 2);

            return Double.compare(val2, val1);
        }
    }
}
