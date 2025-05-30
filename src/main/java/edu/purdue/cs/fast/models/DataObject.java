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

import java.util.List;
import java.io.Serializable;

public class DataObject extends Query implements Serializable {
//    public int id;
    public static String CSV_HEADER = "id,x,y,keywords,st,et";

    public Point location;
    private final Rectangle pointBox;
//    public List<String> keywords;
//    public Long st;
//    public Long et;

    public DataObject(Integer id, Point location, List<String> keywords, long st, long et) {
        super(id, keywords, null, st, et);
//        this.id = id;
        this.location = location;
        this.pointBox = new Rectangle(location, location);
//        this.keywords = keywords;
//        this.st = st;
//        this.et = et;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "id=" + id +
                ", location=" + location +
                ", keywords=" + keywords +
                ", st=" + st +
                ", et=" + et +
                '}';
    }

    public String toCSV() {
        String keyword_str = String.join("|", keywords);
        return id + "," + location.x + "," + location.y + "," + keyword_str +
                "," + st + "," + et;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof DataObject))
            return false;
        return (this.id == ((DataObject) other).id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public Rectangle spatialBox() {
        return pointBox;
    }
}
