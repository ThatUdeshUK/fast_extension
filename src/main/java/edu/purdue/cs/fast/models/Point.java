/**
 * Copyright Jul 5, 2015
 * Author : Ahmed Mahmood
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.purdue.cs.fast.models;
import java.io.Serializable;

public class Point implements Comparable<Object>, Serializable {
    public double x;
    public double y;

    public Point(Point other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Point))
            return false;

        Point c = (Point) o;
        return (Double.compare(c.x, this.x) == 0 && Double.compare(c.y, this.y) == 0);
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Point))
            throw new RuntimeException("Cannot compare object.");

        Point p = (Point) o;

        int xComp = 0;
        if (this.x < p.x)
            xComp = -1;
        if (this.x > p.x)
            xComp = 1;
        if (xComp != 0)
            return xComp;

        return Double.compare(this.y, p.y);
    }
}
