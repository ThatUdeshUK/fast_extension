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

public class Rectangle implements Serializable {
    public Point min;
    public Point max;

    public Rectangle(Point min, Point max) {
        this.min = min;
        this.max = max;
    }

    public Rectangle(double minX, double minY, double maxX, double maxY) {
        this.min = new Point(minX, minY);
        this.max = new Point(maxX, maxY);
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Rectangle))
            return false;

        Rectangle c = (Rectangle) o;
        return (c.min.equals(this.min) && c.max.equals(this.max));
    }


}
