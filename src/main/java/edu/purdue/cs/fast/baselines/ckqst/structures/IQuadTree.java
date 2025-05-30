package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.Morton;
import edu.purdue.cs.fast.baselines.ckqst.models.AxisAlignedBoundingBox;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.baselines.quadtree.BaseQuadTree;
import edu.purdue.cs.fast.helper.MutableInt;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.structures.BoundedPriorityQueue;
import edu.purdue.cs.fast.structures.KeywordFrequency;

import java.util.*;
import java.io.Serializable;

public class IQuadTree extends BaseQuadTree<Query, DataObject> implements Serializable {
    private final AxisAlignedBoundingBox aabb;
    private final HashMap<String, ILQuadNode> roots;
//    public static HashMap<String, KeywordFrequency> keywordFrequencyMap;


    public IQuadTree(double x, double y, double width, double height, int capacity, int maxTreeHeight) {
        Point xyPoint = new Point(x, y);
        aabb = new AxisAlignedBoundingBox(xyPoint, width, height);
        ILQuadNode.maxCapacity = capacity;
        ILQuadNode.maxHeight = maxTreeHeight;
        this.roots = new HashMap<>();
//        this.keywordFrequencyMap = new HashMap<>();
    }

    @Override
    protected BaseQuadNode<Query, DataObject> getRoot() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public boolean insert(DataObject object) {
        for (String keyword : object.keywords) {
//            KeywordFrequency stats = keywordFrequencyMap.get(keyword);
//            if (stats == null) {
//                stats = new KeywordFrequency(1, 1, 0);
//                keywordFrequencyMap.put(keyword, stats);
//            } else {
//                stats.queryCount++;
//            }

            if (roots.containsKey(keyword)) {
                roots.get(keyword).insert(object);
            } else {
                ILQuadNode newNode = new ILQuadNode(aabb, keyword);
                newNode.insert(object);
                roots.put(keyword, newNode);
            }
        }
        return true;
    }

    @Override
    public boolean remove(DataObject object) {
        for (String keyword : object.keywords) {
            if (roots.containsKey(keyword))
                roots.get(keyword).remove(object);
        }
        return true;
    }

    public double getStatusByMorton(String keyword, Morton morton) {
        return roots.get(keyword).getStatusByMorton(morton);
    }

    @Override
    public Collection<DataObject> search(Query q) {
        int k = -1;
        Point location = null;
        if (q instanceof CkQuery) {
            k = ((CkQuery) q).k;
            location = ((CkQuery) q).location;
        } else if (q instanceof KNNQuery) {
            k = ((KNNQuery) q).k;
            location = ((KNNQuery) q).location;
        } else if (q instanceof LMinimalRangeQuery) {
            k = ((LMinimalRangeQuery) q).k;
            location = ((LMinimalRangeQuery) q).location;
        }
        BoundedPriorityQueue<DataObject> results = new BoundedPriorityQueue<>(
                k,
                new KNNQuery.EuclideanComparator(location)
        );
        double lambda = Double.MAX_VALUE;
        HashMap<DataObject, MutableInt> hits = new HashMap<>();
//        HashMap<Integer, Integer> hits = new HashMap<>();
        DeltaComparator deltaComparator = new DeltaComparator(location);
        PriorityQueue<ILQuadNode> H = new PriorityQueue<>(deltaComparator);    // Line 1

        for (String keyword : q.keywords) {                     // Line 2
            if (roots.containsKey(keyword)) {
                H.add(roots.get(keyword));                      // Line 3
            } else
                return results; // ILQuadTree doesn't have all the keywords
        }

        for (ILQuadNode e; (e = H.poll()) != null; ) {          // Line 4
            if (!e.objects.isEmpty()) {                         // Line 6: e is a black node
                boolean signCheck = true;

                for (String kj : q.keywords) {                  // Line 8
                    if (!kj.equals(e.keyword)) {
                        if (roots.containsKey(kj)) {
                            // Line 9: CheckSignature
                            double other = roots.get(kj).getStatusByMorton(e.mortonCode);
                            if (other == 0) {
                                signCheck = false;
                                break;
                            }
                        } else {
                            signCheck = false;
                            break;
                        }
                    }
                }

                if (signCheck) {                                // Line 10
                    for (DataObject o : e.objects) {
//                        Integer oid = Integer.valueOf(o.id);
//                        Integer oHit = hits.merge(oid, 1, Integer::sum);
                        MutableInt oHit = hits.get(o);
                        if (oHit == null) {
                            oHit = new MutableInt(1);
                            hits.put(o, oHit);
                        } else
                            oHit.increment();
                        if (oHit.getValue() == q.keywords.size()) {
                            results.add(o);
                            if (results.isFull()) {
                                assert results.peek() != null;
                                lambda = SpatialHelper.getDistanceInBetween(location, results.peek().location);
                            }
                        }
                    }
                }
            } else if (!e.isLeaf()) {                           // Line 17: Non leaf node
                for (ILQuadNode child : e.getChildren()) {
                    double eMinDist = deltaComparator.getMinDist(child);
                    if (!(child.isLeaf() && child.objects.isEmpty()) && eMinDist < lambda) { // Line 19
                        H.add(child);                           // Line 20
                    }
                }
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, ILQuadNode> entry : roots.entrySet()) {
            out.append(entry.getKey()).append(" :\n").append(TreePrinter.getString(entry.getValue(), "", true));
        }
        return out.toString();
    }

    public static class ILQuadNode extends BaseQuadNode<Query, DataObject> implements Serializable {
        protected static int maxCapacity = 0;
        protected static int maxHeight = 0;
        private static int ilQuadNodes = 0;
        public final String keyword;
        //        public String morton = "";
        public Morton mortonCode;
        protected List<DataObject> objects = new LinkedList<>();
        protected int height = 1;
        protected LinkedList<ILQuadNode> children;
        private int id = 0;

        public ILQuadNode(AxisAlignedBoundingBox aabb, String keyword) {
            super(aabb);
            this.mortonCode = new Morton();
            this.keyword = keyword;
            this.id = ilQuadNodes;
            ilQuadNodes++;
        }

        public ILQuadNode(AxisAlignedBoundingBox aabb, String keyword, Morton code) {
            super(aabb);
            this.mortonCode = code;
            this.keyword = keyword;
            this.id = ilQuadNodes;
            ilQuadNodes++;
        }

        @Override
        public boolean insert(DataObject o) {
            // Ignore objects which do not belong in this quad tree
            if (!aabb.containsPoint(o.location) || (isLeaf() && objects.contains(o)))
                return false; // object cannot be added

            // If there is space in this quad tree, add the object here
            if ((height == maxHeight) || (isLeaf() && objects.size() < maxCapacity)) {
                objects.add(o);
                return true;
            }

            // Otherwise, we need to subdivide then add the point to whichever node will accept it
            if (isLeaf() && height < maxHeight)
                subdivide();
            return insertIntoChildren(o);
        }

        private void subdivide() {
            double h = aabb.height / 2d;
            double w = aabb.width / 2d;

            AxisAlignedBoundingBox aabbSW = new AxisAlignedBoundingBox(aabb, w, h);
            southWest = new ILQuadNode(aabbSW, keyword, mortonCode.getChild(0));
            ((ILQuadNode) southWest).height = height + 1;

            Point xySE = new Point(aabb.x + w, aabb.y);
            AxisAlignedBoundingBox aabbSE = new AxisAlignedBoundingBox(xySE, w, h);
            southEast = new ILQuadNode(aabbSE, keyword, mortonCode.getChild(1));
            ((ILQuadNode) southEast).height = height + 1;

            Point xyNW = new Point(aabb.x, aabb.y + h);
            AxisAlignedBoundingBox aabbNW = new AxisAlignedBoundingBox(xyNW, w, h);
            northWest = new ILQuadNode(aabbNW, keyword, mortonCode.getChild(2));
            ((ILQuadNode) northWest).height = height + 1;

            Point xyNE = new Point(aabb.x + w, aabb.y + h);
            AxisAlignedBoundingBox aabbNE = new AxisAlignedBoundingBox(xyNE, w, h);
            northEast = new ILQuadNode(aabbNE, keyword, mortonCode.getChild(3));
            ((ILQuadNode) northEast).height = height + 1;

            // points live in leaf nodes, so distribute
            for (DataObject p : objects)
                insertIntoChildren(p);
            objects.clear();
        }

        private boolean insertIntoChildren(DataObject object) {
            // A point can only live in one child.
            if (northWest.insert(object)) return true;
            if (northEast.insert(object)) return true;
            if (southWest.insert(object)) return true;
            return southEast.insert(object);
        }

        @Override
        public boolean remove(DataObject object) {
            if (!aabb.containsPoint(object.location))
                return false;

            // If in this AABB and in this node
            if (objects.remove(object))
                return true;

            // If this node has children
            if (!isLeaf()) {
                // If in this AABB but in a child branch
                boolean removed = removeFromChildren(object);
                if (!removed)
                    return false;

                // Try to merge children
                merge();

                return true;
            }

            return false;
        }

        private void merge() {
            // If the children aren't leafs, you cannot merge
            if (!((ILQuadNode) northWest).isLeaf() || !((ILQuadNode) northEast).isLeaf() ||
                    !((ILQuadNode) southWest).isLeaf() || !((ILQuadNode) southEast).isLeaf())
                return;

            // Children and leafs, see if you can remove point and merge into this node
            int nw = ((ILQuadNode) northWest).size();
            int ne = ((ILQuadNode) northEast).size();
            int sw = ((ILQuadNode) southWest).size();
            int se = ((ILQuadNode) southEast).size();
            int total = nw + ne + sw + se;

            // If all the children's point can be merged into this node
            if ((size() + total) < maxCapacity) {
                this.objects.addAll(((ILQuadNode) northWest).objects);
                this.objects.addAll(((ILQuadNode) northEast).objects);
                this.objects.addAll(((ILQuadNode) southWest).objects);
                this.objects.addAll(((ILQuadNode) southEast).objects);

                this.northWest = null;
                this.northEast = null;
                this.southWest = null;
                this.southEast = null;
            }
        }

        private boolean removeFromChildren(DataObject object) {
            // A point can only live in one child.
            if (northWest.remove(object)) return true;
            if (northEast.remove(object)) return true;
            if (southWest.remove(object)) return true;
            return southEast.remove(object);
        }

        public List<ILQuadNode> getChildren() {
            if (children == null)
                children = new LinkedList<>();

            if (!isLeaf() && children.isEmpty()) {
                children.add((ILQuadNode) southWest);
                children.add((ILQuadNode) southEast);
                children.add((ILQuadNode) northWest);
                children.add((ILQuadNode) northEast);
            }

            return children;
        }

        /**
         * Get the status of the node using the morton code.
         *
         * @param m Morton code for the node.
         * @return Int specifying the node type (1:black node, 0:white node, -1: node unreachable)
         */
        public int getStatusByMorton(Morton m) {
            ILQuadNode node = this;

            int code = m.getCode();
            int level = m.getLevel();
            for (int i = 1; i <= level; i++) {
                int shift = 2 * (level - i);
                int prefixCode = code >> shift;

                for (ILQuadNode child : node.getChildren()) {
                    if (child.mortonCode.getCode() == prefixCode) {
                        node = child;
                    }
                }
            }

            if (node.mortonCode.getLevel() == m.getLevel()) {
                if (node.objects.isEmpty() && node.isLeaf())
                    return 0;
                return 1;
            }
            return -1;
        }

        @Override
        protected void search(Query query, Collection<DataObject> results) {
            throw new RuntimeException("Not implemented!");
        }

        @Override
        protected int size() {
            return objects.size();
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(mortonCode.getString()).append(": ");
            for (DataObject object : objects) {
                s.append(object.id).append(", ");
            }
            return s.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            ILQuadNode that = (ILQuadNode) o;
            return Objects.equals(keyword, that.keyword) && Objects.equals(mortonCode, that.mortonCode);
        }

        @Override
        public int hashCode() {
//            return Objects.hash(keyword, morton);
//            return (keyword + morton).hashCode();
            return id;
        }
    }

    static class DeltaComparator implements Comparator<ILQuadNode> {
        private final Point location;
        private final HashMap<ILQuadNode, Double> distMap;

        DeltaComparator(Point location) {
            this.location = location;
            this.distMap = new HashMap<>();
        }

        @Override
        public int compare(ILQuadNode o1, ILQuadNode o2) {
            double o1Min = getMinDistSqr(o1);
            double o2Min = getMinDistSqr(o2);

            return Double.compare(o1Min, o2Min);
        }

        public double getMinDist(ILQuadNode o1) {
            return Math.sqrt(getMinDistSqr(o1));
        }

        private double getMinDistSqr(ILQuadNode o1) {
            Double o1Min = distMap.get(o1);
            ;
            if (o1Min == null) {
                o1Min = calcMinDist(o1);
                distMap.put(o1, o1Min);
            }
            return o1Min;
        }

        private double calcMinDist(ILQuadNode o1) {
            double x = location.x;
            double y = location.y;
            double x_min = o1.getAabb().x;
            double y_min = o1.getAabb().y;
            double x_max = x_min + o1.getAabb().width - 0.001;
            double y_max = y_min + o1.getAabb().height - 0.001;

            if (x < x_min) {
                if (y < y_min) return hypotSqr(x_min - x, y_min - y);
                if (y <= y_max) return Math.pow(x_min - x, 2);
                return hypotSqr(x_min - x, y_max - y);
            } else if (x <= x_max) {
                if (y < y_min) return Math.pow(y_min - y, 2);
                if (y <= y_max) return 0;
                return Math.pow(y - y_max, 2);
            } else {
                if (y < y_min) return hypotSqr(x_max - x, y_min - y);
                if (y <= y_max) return Math.pow(x - x_max, 2);
                return hypotSqr(x_max - x, y_max - y);
            }
        }

        private double hypotSqr(double xDelta, double yDelta) {
            return Math.pow(xDelta, 2) + Math.pow(yDelta, 2);
        }
    }
}
