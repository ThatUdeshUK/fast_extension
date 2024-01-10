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
package edu.purdue.cs.fast.baselines.ckqst.models;

import edu.purdue.cs.fast.models.DataObject;

import java.util.List;
import java.util.Objects;

public class CkObject extends XYPoint {
    public int id;
    public List<String> keywords;
    public Long st;
    public Long et;

    public CkObject(Integer id, double x, double y, List<String> keywords, long st, long et) {
        super(x, y);
        this.id = id;
        this.keywords = keywords;
        this.st = st;
        this.et = et;
    }

    public CkObject(DataObject dataObject) {
        this(dataObject.id, dataObject.location.x, dataObject.location.y, dataObject.keywords,
                dataObject.st, dataObject.et);
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "id=" + id +
                ", (x, y)=(" + x + ", " + y + ")" +
                ", keywords=" + keywords +
                ", st=" + st +
                ", et=" + et +
                '}';
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof CkObject))
            return false;
        return (this.id == ((CkObject) other).id);
    }

}
