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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import edu.purdue.cs.fast.helper.Command;
import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.helper.TextualPredicate;


public class Query {
	private  String srcId;
	private  Integer queryId;
	public Boolean added;
	public Integer visitied;
		
	private  Point focalPoint;
	private  String  queryType;
	private  Integer  k;
	private  ArrayList<String>  queryText;
	private  ArrayList<String>  queryText2;
	private  Long    timeStamp;
	private  Rectangle spatialRange;
	private  String dataSrc;
	private  String dataSrc2; 
	private  Long removeTime;  
	
	private  Command command; //add , Update , Drop 
	private  TextualPredicate textualPredicate;
	private  TextualPredicate textualPredicate2;
	private  TextualPredicate joinTextualPredicate;
		
	private  Double distance ;
	private  Boolean continousQuery;
		
	private PriorityQueue<DataObject> topKQueue;  // Priority queue (max-heap).
	private HashMap<Integer, Integer> currentRanks;  // Records the current rank of each object in the top-k list.
	private ArrayList<Integer> pendingTopKTaskIds ;
	private Double farthestDistance;
	private HashMap<Integer, DataObject> currentObjects;
	
	
	
	public Long getRemoveTime() {
		return removeTime;
	}

	public void setRemoveTime(Long removeTime) {
		this.removeTime = removeTime;
	}
	private static Double maxFarthestDistance=Math.sqrt(SpatioTextualConstants.xMaxRange*SpatioTextualConstants.xMaxRange+
			SpatioTextualConstants.yMaxRange*SpatioTextualConstants.yMaxRange);//this is the maximum possible space between any two points indexed
	
	public void setFarthestDistance(Double farthestDistance) {
		this.farthestDistance = farthestDistance;
	}
	
	public TextualPredicate getJoinTextualPredicate() {
		return joinTextualPredicate;
	}

	public void setJoinTextualPredicate(TextualPredicate joinTextualPredicate) {
		this.joinTextualPredicate = joinTextualPredicate;
	}

	public TextualPredicate getTextualPredicate() {
		return textualPredicate;
	}
	
	public TextualPredicate getTextualPredicate2() {
		return textualPredicate2;
	}
	public void setTextualPredicate2(TextualPredicate textualPredicate2) {
		this.textualPredicate2 = textualPredicate2;
	}
	public void setTextualPredicate(TextualPredicate textualPredicate) {
		this.textualPredicate = textualPredicate;
	}
	public Boolean getContinousQuery() {
		return continousQuery;
	}
	public void setContinousQuery(Boolean continousQuery) {
		this.continousQuery = continousQuery;
	}
	
	public Query (){
		added=false;
		visitied = 0;
		focalPoint = new Point();
		queryText = new ArrayList<String>();
		spatialRange = new Rectangle(new Point(), new Point());
		this.farthestDistance = maxFarthestDistance;
		removeTime = null;
	}
	public Query (Query q){
		added=false;
		visitied = 0;
		this.farthestDistance = maxFarthestDistance;
		this.queryId=q.queryId;
		this.setCommand(q.command);
		this.srcId=q.srcId;
		this.setContinousQuery(q.continousQuery);
		this.setDataSrc(q.dataSrc);
		this.setDistance(q.distance);
		this.setQueryType(q.queryType);
		this.setTimeStamp(q.timeStamp);
		this.setSpatialRange(new Rectangle(q.spatialRange.getMin(),q.spatialRange.getMax()));
		this.setTextualPredicate(q.textualPredicate);
		this.setQueryText(q.queryText);
				
		
	}
	
	public Double getDistance() {
		return distance;
	}


	public void setDistance(Double distance) {
		this.distance = distance;
	}


	public ArrayList<String> getQueryText2() {
		return queryText2;
	}


	public void setQueryText2(ArrayList<String> queryText2) {
		this.queryText2 = queryText2;
	}


	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public Integer getQueryId() {
		return queryId;
	}
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
	
	public Integer getK() {
		return k;
	}
	public void setK(Integer k) {
		this.k = k;
	}
	public ArrayList<String>  getQueryText() {
		return queryText;
	}
	public void setQueryText(ArrayList<String>  queryText) {
		this.queryText = queryText;
	}
	public String getSrcId() {
		return srcId;
	}
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Point getFocalPoint() {
		return focalPoint;
	}
	public void setFocalPoint(Point focalPoint) {
		this.focalPoint = focalPoint;
	}
	public Rectangle getSpatialRange() {
		return spatialRange;
	}
	public void setSpatialRange(Rectangle spatialRange) {
		this.spatialRange = spatialRange;
	}


	public String getDataSrc() {
		return dataSrc;
	}


	public void setDataSrc(String dataSrc) {
		this.dataSrc = dataSrc;
	}


	public String getDataSrc2() {
		return dataSrc2;
	}


	public void setDataSrc2(String dataSrc2) {
		this.dataSrc2 = dataSrc2;
	}


	public Command getCommand() {
		return command;
	}


	public void setCommand(Command command) {
		this.command = command;
	}
	
	@Override
	public String toString(){
		String output = "Query[: "+(getQueryId()==null?"":getQueryId())
				+" , "+ "Source: "+(getSrcId()==null?"":getSrcId())
				+" , "+ "Type: "+(getQueryType()==null?"":getQueryType())
				+" , "+ "Text: "+(getQueryText()==null?"":getQueryText().toString())
				+" , "+ "focal: "+(getFocalPoint()==null?"":getFocalPoint().toString())
				+" , "+ "range: "+(getSpatialRange()==null?"":getSpatialRange().toString())
				+" , "+ "distance: "+(getDistance()==null?"":getDistance())
				+" , "+ "K: "+(getK()==null?"":getK())				
				+"]";
		return output;
	}
	
	
	
	public static String getUniqueIDFromQuerySourceAndQueryId(String querySourceId, Integer queryId)
	{
		return querySourceId+SpatioTextualConstants.queryIdDelimiter+queryId;
	}
	public String getUniqueIDFromQuerySourceAndQueryId(){
		return getUniqueIDFromQuerySourceAndQueryId(this.getSrcId(), this.getQueryId());
		
	}
	public static String getSrcIdFromUniqueQuerySrcQueryId(String src_query_id)
	{
		return src_query_id.split(SpatioTextualConstants.queryIdDelimiter)[0];
	}
	public static String getQueryIdFromUniqueQuerySrcQueryId(String src_query_id)
	{
		return src_query_id.split(SpatioTextualConstants.queryIdDelimiter)[1];
	}
	// Returns a representation of the changes in the top-k list (if any).
	//the incoming object must satisfy the KNN query textual predicate criteria
	//TODO address the nature of volatile, current object, 
	//TODO this code needs a lot of refactoring
	//TODO the farthest distance may need to be extended to support the updates of object going out of 
	public Double getFarthestDistance() {
		return this.farthestDistance;
				
	}
	public Integer getKNNlistSize() {
		return topKQueue.size();
				
	}
	
	
}
