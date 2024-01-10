package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.baselines.ckqst.models.AxisAlignedBoundingBox;
import edu.purdue.cs.fast.baselines.ckqst.models.CkObject;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.ckqst.models.XYPoint;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

/**
 * A quadtree is a tree data structure in which each internal node has exactly four children. Quadtrees
 * are most often used to partition a two dimensional space by recursively subdividing it into four
 * quadrants or regions. The regions may be square or rectangular, or may have arbitrary shapes.
 * <p>
 * http://en.wikipedia.org/wiki/Quadtree
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
@SuppressWarnings("unchecked")
public class CostBasedQuadTree {
    private QuadNode root;

    public CostBasedQuadTree(double x, double y, double width, double height, int maxHeight) {
        XYPoint xyPoint = new XYPoint(x, y);
        AxisAlignedBoundingBox aabb = new AxisAlignedBoundingBox(xyPoint, width, height);
        QuadNode.maxHeight = maxHeight;
        root = new QuadNode(aabb);
    }

    /**
     * Get the root node.
     *
     * @return Root QuadNode.
     */
    protected QuadNode getRoot() {
        return root;
    }

    /**
     * Stream object through the quadtree.
     */
    public Collection<Query> searchObject(CkObject object) {
        Set<Query> results = new HashSet<>();
        this.root.searchObject(object, results);
        return results;
    }

    /**
     * Insert a query into tree.
     *
     * @param query Query to be inserted into the tree.
     */
    public boolean insert(CkQuery query) {
        return this.root.insert(query);
    }

    /**
     * Remove a query from tree.
     *
     * @param query Query to be removed from the tree.
     */
    public boolean remove(CkQuery query) {
        return this.root.remove(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }

    protected static class QuadNode implements Comparable<QuadNode> {
        protected final AxisAlignedBoundingBox aabb;
        protected QuadNode northWest = null;
        protected QuadNode northEast = null;
        protected QuadNode southWest = null;
        protected QuadNode southEast = null;
        protected static int maxHeight = 0;
        protected OrderedInvertedIndex textualIndex = new OrderedInvertedIndex();
        protected int height = 1;

        protected QuadNode(AxisAlignedBoundingBox aabb) {
            this.aabb = aabb;
        }

        /**
         * Insert query into tree.
         *
         * @param q Query object to insert into tree.
         * @return True if successfully inserted.
         */
        protected boolean insert(CkQuery q) {
//            if (q.id == 3002)
//                System.out.println("DEBUG! Ins!");
            // Ignore objects which do not belong in this quad tree
            if (!aabb.containsQuery(q) && height > 1)
                return false; // object cannot be added

            if (height == maxHeight) {
                textualIndex.insertQueryPL(q);
                return true;
            } else if (isMinimal(q)) {
                double nodeVCost = verifyCost(q);
                double nodeUCost = updateCost(q);

                double childrenVCost = 0;
                double childrenUCost = 0;
                if (isLeaf())
                    subdivide();
                childrenVCost += northEast.verifyCost(q);
                childrenVCost += northWest.verifyCost(q);
                childrenVCost += southEast.verifyCost(q);
                childrenVCost += southWest.verifyCost(q);

                childrenUCost += northEast.updateCost(q);
                childrenUCost += northWest.updateCost(q);
                childrenUCost += southEast.updateCost(q);
                childrenUCost += southWest.updateCost(q);

                if (childrenVCost + CkQST.thetaU * childrenUCost < nodeVCost + CkQST.thetaU * nodeUCost) {
                    northEast.textualIndex.insertQueryPL(q);
                    northWest.textualIndex.insertQueryPL(q);
                    southEast.textualIndex.insertQueryPL(q);
                    southWest.textualIndex.insertQueryPL(q);
                    return true;
                }

                textualIndex.insertQueryPL(q);
                return true;
            }

            // Otherwise, we need to subdivide then add the point to whichever node will accept it
            if (isLeaf())
                subdivide();
            return insertIntoChildren(q);
        }

        private boolean isMinimal(CkQuery q) {
            if (aabb.quadContainsQuery(q, 0)) return false;
            if (aabb.quadContainsQuery(q, 1)) return false;
            if (aabb.quadContainsQuery(q, 2)) return false;
            return !aabb.quadContainsQuery(q, 3);
        }

        private double verifyCost(CkQuery q) {
            double num_o_N = 1; // the number of objects falling to this node in a unit time

            double p_V_q = textualIndex.verifyProb(q);

            double e_V_q = 1;
            if (q.keywords.size() > 2) {
                e_V_q = textualIndex.estVerifyCost(q);
            }

            return num_o_N * p_V_q * e_V_q;
        }

        private double updateCost(CkQuery q) {
            return textualIndex.updateCost(q);
        }

        private void subdivide() {
            double h = aabb.height / 2d;
            double w = aabb.width / 2d;

            AxisAlignedBoundingBox aabbNW = new AxisAlignedBoundingBox(aabb, w, h);
            northWest = new QuadNode(aabbNW);
            northWest.height = height + 1;

            XYPoint xyNE = new XYPoint(aabb.x + w, aabb.y);
            AxisAlignedBoundingBox aabbNE = new AxisAlignedBoundingBox(xyNE, w, h);
            northEast = new QuadNode(aabbNE);
            northEast.height = height + 1;

            XYPoint xySW = new XYPoint(aabb.x, aabb.y + h);
            AxisAlignedBoundingBox aabbSW = new AxisAlignedBoundingBox(xySW, w, h);
            southWest = new QuadNode(aabbSW);
            southWest.height = height + 1;

            XYPoint xySE = new XYPoint(aabb.x + w, aabb.y + h);
            AxisAlignedBoundingBox aabbSE = new AxisAlignedBoundingBox(xySE, w, h);
            southEast = new QuadNode(aabbSE);
            southEast.height = height + 1;
        }

        private boolean insertIntoChildren(CkQuery q) {
            // A point can only live in one child.
            if (northWest.insert(q)) return true;
            if (northEast.insert(q)) return true;
            if (southWest.insert(q)) return true;
            return southEast.insert(q);
        }

        /**
         * Remove object from tree.
         *
         * @param q Query object to remove from tree.
         * @return True if successfully removed.
         */
        protected boolean remove(CkQuery q) {
            // If not in this AABB, don't do anything
            if (!aabb.containsPoint(q.location))
                return false;

            // If in this AABB and in this node
            if (textualIndex.remove(q))
                return true;

            // If this node has children
            if (!isLeaf()) {
                // If in this AABB but in a child branch
                return removeFromChildren(q);
            }

            return false;
        }

        private boolean removeFromChildren(CkQuery q) {
            // A point can only live in one child.
            if (northWest.remove(q)) return true;
            if (northEast.remove(q)) return true;
            if (southWest.remove(q)) return true;
            return southEast.remove(q);
        }

        /**
         * How many Queries this node contains.
         *
         * @return Number of Queries this node contains.
         */
        protected int size() {
            return textualIndex.countQueries;
        }

        /**
         * Find all objects which appear within a range.
         *
         * @param object  Object to be matched with the queries contained in this node.
         * @param results Queries matching with the streamed object.
         */
        protected void searchObject(CkObject object, Collection<Query> results) {
            // Automatically abort if the range does not collide with this quad
            if (!aabb.containsPoint(object))
                return;

            // TODO - Search ordered inverted index
            textualIndex.searchObject(object, results);
//            for (CkQuery q : points) {
//                if (q.containsPoint(q))
//                    pointsInRange.add(q);
//            }

            // Otherwise, add the points from the children
            if (!isLeaf()) {
                northWest.searchObject(object, results);
                northEast.searchObject(object, results);
                southWest.searchObject(object, results);
                southEast.searchObject(object, results);
            }
        }

        /**
         * Is current node a leaf node.
         *
         * @return True if node is a leaf node.
         */
        protected boolean isLeaf() {
            return (northWest == null && northEast == null && southWest == null && southEast == null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int hash = aabb.hashCode();
            hash = hash * 13 + ((northWest != null) ? northWest.hashCode() : 1);
            hash = hash * 17 + ((northEast != null) ? northEast.hashCode() : 1);
            hash = hash * 19 + ((southWest != null) ? southWest.hashCode() : 1);
            hash = hash * 23 + ((southEast != null) ? southEast.hashCode() : 1);
            return hash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof QuadNode))
                return false;

            QuadNode qNode = (QuadNode) obj;
            return this.compareTo(qNode) == 0;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("rawtypes")
        @Override
        public int compareTo(QuadNode o) {
            return this.aabb.compareTo(o.aabb);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(aabb.toString()).append(textualIndex.toString());
            return builder.toString();
        }
    }

    protected static class TreePrinter {

        public static String getString(CostBasedQuadTree tree) {
            if (tree.getRoot() == null) return "Tree has no nodes.";
            return getString(tree.getRoot(), "", true);
        }

        private static <T extends XYPoint> String getString(QuadNode node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix).append(isTail ? "└── " : "├── ").append(" node={").append(node.toString()).append("}\n");
            List<QuadNode> children = null;
            if (node.northWest != null || node.northEast != null || node.southWest != null || node.southEast != null) {
                children = new ArrayList<>(4);
                if (node.northWest != null) children.add(node.northWest);
                if (node.northEast != null) children.add(node.northEast);
                if (node.southWest != null) children.add(node.southWest);
                if (node.southEast != null) children.add(node.southEast);
            }
            if (children != null) {
                for (int i = 0; i < children.size() - 1; i++) {
                    builder.append(getString(children.get(i), prefix + (isTail ? "    " : "│   "), false));
                }
                if (!children.isEmpty()) {
                    builder.append(getString(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true));
                }
            }

            return builder.toString();
        }
    }
}
