package edu.purdue.cs.fast.baselines.ckqst.models;


import java.util.Comparator;

public class XYPoint implements Comparable<Object> {

    public double x = Float.MIN_VALUE;
    public double y = Float.MIN_VALUE;

    public XYPoint() { }

    public XYPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + (int)x;
        hash = hash * 19 + (int)y;
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof XYPoint))
            return false;

        XYPoint xyzPoint = (XYPoint) obj;
        return compareTo(xyzPoint) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object o) {
        if ((o instanceof XYPoint)==false)
            throw new RuntimeException("Cannot compare object.");

        XYPoint p = (XYPoint) o;
        int xComp = X_COMPARATOR.compare(this, p);
        if (xComp != 0)
            return xComp;
        return Y_COMPARATOR.compare(this, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(x).append(", ");
        builder.append(y);
        builder.append(")");
        return builder.toString();
    }

    private static final Comparator<XYPoint> X_COMPARATOR = new Comparator<XYPoint>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(XYPoint o1, XYPoint o2) {
            if (o1.x < o2.x)
                return -1;
            if (o1.x > o2.x)
                return 1;
            return 0;
        }
    };

    private static final Comparator<XYPoint> Y_COMPARATOR = new Comparator<XYPoint>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(XYPoint o1, XYPoint o2) {
            if (o1.y < o2.y)
                return -1;
            if (o1.y > o2.y)
                return 1;
            return 0;
        }
    };
}
