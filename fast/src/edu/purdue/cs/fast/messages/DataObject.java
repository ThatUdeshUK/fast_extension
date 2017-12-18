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
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.purdue.cs.fast.messages;

import java.util.ArrayList;
import java.util.HashSet;

import edu.purdue.cs.fast.helper.Command;
import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.TextHelpers;

public class DataObject {
	private String srcId;
	private Integer objectId;
	private Point location;
	private String originalText;
	private ArrayList<String> objectText;
	public HashSet<String>hashedText;
	private Long timeStamp;
	private Rectangle relevantArea;
	private Command command;
	public boolean added;

	
	public DataObject(DataObject other){
		this.srcId=new String(other.srcId);
		this.objectId=other.objectId.intValue();
		this.location=new Point(other.location);
		this.originalText=new String(other.originalText);
		this.objectText=(ArrayList<String>) other.objectText.clone();
		this.command=other.command;
		this.added = false;
		this.hashedText = new HashSet<String>();
		
	}
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof DataObject))
			return false;
		return (this.srcId == ((DataObject) other).srcId && this.objectId == ((DataObject) other).objectId);
	}
	public boolean equalsLocation(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof DataObject))
			return false;
		return (this.srcId == ((DataObject) other).srcId && this.objectId == ((DataObject) other).objectId &&this.location.equals(((DataObject) other).location));
	}

	public DataObject() {
		location = new Point();
		objectText = new ArrayList<String>();
		added = false;
		hashedText = new HashSet<String>();
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public ArrayList<String> getObjectText() {
		return objectText;
	}

	public void setObjectText(ArrayList<String> objectText) {
		this.objectText = objectText;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public DataObject(Integer objectId, Point location, String originalText, Long timeStamp, Command command) {
		super();
	
		this.objectId = objectId;
		this.location = location;
		this.originalText = originalText;
		ArrayList<String> objectTextList = TextHelpers.transformIntoSortedArrayListOfString(originalText);
		this.objectText=objectTextList;
		this.timeStamp = timeStamp;
		this.command = command;
		this.added= false;
		this.hashedText = new HashSet<String>();
	}
	public Rectangle getRelevantArea() {
		return relevantArea;
	}

	public void setRelevantArea(Rectangle relevantArea) {
		this.relevantArea = relevantArea;
	}

	public void extendRelevantArea(Rectangle relevantArea) {
		if (this.relevantArea == null)
			this.relevantArea = relevantArea;
		else {
			if (this.relevantArea.getMin().getX() > relevantArea.getMin().getX())
				this.relevantArea.getMin().setX(relevantArea.getMin().getX());
			if (this.relevantArea.getMin().getY() > relevantArea.getMin().getY())
				this.relevantArea.getMin().setY(relevantArea.getMin().getY());

			if (this.relevantArea.getMax().getX() < relevantArea.getMax().getX())
				this.relevantArea.getMax().setX(relevantArea.getMax().getX());
			if (this.relevantArea.getMax().getY() < relevantArea.getMax().getY())
				this.relevantArea.getMax().setY(relevantArea.getMax().getY());

		}
	}

	@Override
	public String toString() {
		String output = "Data Object[: " + (getObjectId() == null ? "" :getObjectId() )
				+ " , " + "Command: "+ (getLocation() == null ? "" : getCommand())
				+ " , " + "Source: " + (getSrcId() == null  ? "" :getSrcId())
				+ " , " + "Text: " + (getObjectText() == null ? "" : getObjectText().toString()) 
				+ " , " + "Location: "+ (getLocation() == null ? "" : getLocation().toString()) 
				+"]";
		return output;
	}
	

}
