package edu.purdue.cs.fast.baselines.quadtree;

import edu.purdue.cs.fast.baselines.ckqst.models.AxisAlignedBoundingBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.Serializable;

/**
 * A quadtree is a tree data structure in which each internal node has exactly four children. Quadtrees 
 * are most often used to partition a two dimensional space by recursively subdividing it into four 
 * quadrants or regions. The regions may be square or rectangular, or may have arbitrary shapes.
 * <a href="http://en.wikipedia.org/wiki/Quadtree">...</a>
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
@SuppressWarnings("unchecked")
public abstract class BaseQuadTree<Q, O> {

    /**
     * Get the root node.
     *
     * @return Root QuadNode.
     */
    protected abstract BaseQuadNode<Q, O> getRoot();

    /**
     * Search query in the quadtree.
     */
    public abstract Collection<O> search(Q q);

    /**
     * Insert object into tree.
     *
     * @param o to be inserted.
     */
    public abstract boolean insert(O o);

    /**
     * Remove object from tree.
     *
     * @param o to be removed.
     */
    public abstract boolean remove(O o);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }
    
    protected static abstract class BaseQuadNode<Q, O> implements Comparable<BaseQuadNode<Q, O>>, Serializable {

        protected final AxisAlignedBoundingBox aabb;

        protected BaseQuadNode<Q, O> northWest = null;
        protected BaseQuadNode<Q, O> northEast = null;
        protected BaseQuadNode<Q, O> southWest = null;
        protected BaseQuadNode<Q, O> southEast = null;

        protected BaseQuadNode(AxisAlignedBoundingBox aabb) {
            this.aabb = aabb;
        }

        /**
         * Insert object into tree.
         *
         * @param o Geometric object to insert into tree.
         * @return True if successfully inserted.
         */
        public abstract boolean insert(O o);

        /**
         * Remove object from tree.
         *
         * @param o Geometric object to remove from tree.
         * @return True if successfully removed.
         */
        public abstract boolean remove(O o);

        /**
         * How many GeometricObjects this node contains.
         *
         * @return Number of GeometricObjects this node contains.
         */
        protected abstract int size();

        /**
         * Get aabb of the node.
         *
         * @return instance of AABB.
         */
        public AxisAlignedBoundingBox getAabb() {
            return aabb;
        }

        /**
         * Find all objects which appear within given query.
         *
         * @param q Query used for searching.
         * @param geometricObjectsInRange Geometric objects inside the bounding box. 
         */
        protected abstract void search(Q q, Collection<O> geometricObjectsInRange);

        /**
         * Is current node a leaf node.
         * @return True if node is a leaf node.
         */
        public boolean isLeaf() {
            return (northWest==null && northEast==null && southWest==null && southEast==null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int hash = aabb.hashCode();
            hash = hash * 13 + ((northWest!=null)?northWest.hashCode():1);
            hash = hash * 17 + ((northEast!=null)?northEast.hashCode():1);
            hash = hash * 19 + ((southWest!=null)?southWest.hashCode():1);
            hash = hash * 23 + ((southEast!=null)?southEast.hashCode():1);
            return hash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof BaseQuadTree.BaseQuadNode))
                return false;

            BaseQuadNode<Q, O> qNode = (BaseQuadNode<Q, O>) obj;
            return this.compareTo(qNode) == 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(BaseQuadNode o) {
            return this.aabb.compareTo(o.aabb);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return aabb.toString();
        }
    }

    protected static class TreePrinter {

        public static <Q, O> String getString(BaseQuadTree<Q, O> tree) {
            if (tree.getRoot() == null) return "Tree has no nodes.";
            return getString(tree.getRoot(), "", true);
        }

        public static <Q, O> String getString(BaseQuadNode<Q, O> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix).append(isTail ? "└── " : "├── ").append(" node={").append(node.toString()).append("}\n");
            List<BaseQuadNode<Q, O>> children = null;
            if (node.northWest != null || node.northEast != null || node.southWest != null || node.southEast != null) {
                children = new ArrayList<>(4);
                if (node.southWest != null) children.add(node.southWest);
                if (node.southEast != null) children.add(node.southEast);
                if (node.northWest != null) children.add(node.northWest);
                if (node.northEast != null) children.add(node.northEast);
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
