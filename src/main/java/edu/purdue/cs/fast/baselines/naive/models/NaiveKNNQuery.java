package edu.purdue.cs.fast.baselines.naive.models;

import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.TextualPredicate;
import edu.purdue.cs.fast.models.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class NaiveKNNQuery extends Query {
    public Point location;
    public double ar;
    public int k;
    public PriorityQueue<DataObject> monitoredObjects;

    public NaiveKNNQuery(int id, List<String> keywords, Point location, int k, TextualPredicate predicate, int st, int et) {
        super(id, keywords, predicate, st, et);
        this.location = location;
        this.k = k;
        this.ar = Double.MAX_VALUE;
    }

    public static NaiveKNNQuery fromKNNQuery(KNNQuery query) {
        return new NaiveKNNQuery(
                query.id,
                query.keywords,
                query.location,
                query.k,
                query.predicate,
                (int) query.st,
                (int) query.et
        );
    }

    public void pushWithLimitK(DataObject obj) {
        if (monitoredObjects == null) {
            monitoredObjects = new PriorityQueue<>(k, new EuclideanComparator(location));
        }

        List<DataObject> toRemove = new ArrayList<>();
        monitoredObjects.forEach(query -> {
            if (query.et < obj.st) {
                toRemove.add(query);
            }
        });
        toRemove.forEach(o -> monitoredObjects.remove(o));

        if (!monitoredObjects.contains(obj)) {
            monitoredObjects.add(obj);
            if (monitoredObjects.size() > k) {
                monitoredObjects.poll();
            }
//            if (id == 65) {
//                System.out.println("naive - obj:" + obj.id + ", objk:" + obj.keywords + ", loc:" + location + ", ar:" + SpatialHelper.getDistanceInBetween(location, monitoredObjects.peek().location) + ", keys:" + keywords);
//            }
        }
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

    @Override
    public Rectangle spatialBox() {
        return null;
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
