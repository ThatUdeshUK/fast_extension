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
package edu.purdue.cs.fast.baselines.fast.messages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.purdue.cs.fast.baselines.fast.helper.LCommand;
import edu.purdue.cs.fast.baselines.fast.helper.LTextHelpers;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;

public class LDataObject extends DataObject {
    public HashSet<String> hashedText;
    public boolean added;
    private String originalText;
    private Rectangle relevantArea;
    private LCommand command;

    public LDataObject(Integer id, Point location, List<String> keywords, long st, long et) {
        super(id, location, keywords, st, et);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof LDataObject))
            return false;
        return (this.id == ((LDataObject) other).id);
    }

    public boolean equalsLocation(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof LDataObject))
            return false;
        return (this.id == ((LDataObject) other).id && this.location.equals(((LDataObject) other).location));
    }

    public LCommand getCommand() {
        return command;
    }

    public void setCommand(LCommand command) {
        this.command = command;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Long getSt() {
        return st;
    }

    public void setSt(Long st) {
        this.st = st;
    }

    @Override
    public String toString() {
        String output = "Data Object[: " + id
                + " , " + "Command: " + (getLocation() == null ? "" : getCommand())
                + " , " + "Source: " + (getId())
                + " , " + "Text: " + (getKeywords() == null ? "" : getKeywords().toString())
                + " , " + "Location: " + (getLocation() == null ? "" : getLocation().toString())
                + "]";
        return output;
    }


}
