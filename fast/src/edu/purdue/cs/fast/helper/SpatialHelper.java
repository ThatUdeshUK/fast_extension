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
package edu.purdue.cs.fast.helper;

import java.util.ArrayList;

public class SpatialHelper {
	private final static double QUARTERPI = Math.PI / 4.0;

	/**
	 * This function checks if a KNN query is fully satisfied internal to the
	 * current evaluator bolt
	 * 
	 * @param query
	 * @return
	 */
	

	/**
	 * Basic LInear Conversion
	 * 
	 * @param lonlat
	 * @return
	 */
	//	public static Point convertFromLatLonToXYPoint(LatLong lonlat){
	//		
	//		
	//
	//         Point xy = new Point();
	//         xy.setX( (lonlat.getLatitude()+90)/180 *SpatioTextualConstants.xMaxRange);
	//         xy.setY((lonlat.getLongitude()+180)/360*SpatioTextualConstants.yMaxRange);
	//		
	//         return xy;
	//	}
	public static Point convertFromLatLonToXYPoint(LatLong lonlat) {

		Point xy = new Point();
		xy.setY((lonlat.getLatitude() - SpatioTextualConstants.minLat) / (SpatioTextualConstants.maxLat - SpatioTextualConstants.minLat) * SpatioTextualConstants.yMaxRange);
		xy.setX((lonlat.getLongitude() - SpatioTextualConstants.minLong) / (SpatioTextualConstants.maxLong - SpatioTextualConstants.minLong) * SpatioTextualConstants.xMaxRange);

		return xy;
	}

	public static Point convertFromLatLonToXYPoint(LatLong lonlat, double minLat, double minLong, double maxLat, double maxLong) {

		Point xy = new Point();
		xy.setY((lonlat.getLatitude() - minLat) / (maxLat - minLat) * SpatioTextualConstants.yMaxRange);
		xy.setX((lonlat.getLongitude() - minLong) / (maxLong - minLong) * SpatioTextualConstants.xMaxRange);

		return xy;
	}

	public static LatLong convertFromXYToLatLonTo(Point xy) {

		LatLong latlong = new LatLong();
		latlong.setLatitude((xy.getY() / SpatioTextualConstants.yMaxRange * (SpatioTextualConstants.maxLat - SpatioTextualConstants.minLat)) + SpatioTextualConstants.minLat);
		latlong.setLongitude((xy.getX() / SpatioTextualConstants.xMaxRange * (SpatioTextualConstants.maxLong - SpatioTextualConstants.minLong)) + SpatioTextualConstants.minLong);

		return latlong;
	}

	/**
	 * Basic Linear Conversion
	 * 
	 * @param lonlat
	 * @return
	 */
	//	public static LatLong convertFromXYToLatLonTo(Point xy){
	//		
	//		 LatLong latlong = new LatLong();
	//		 latlong.setLatitude((xy.getX()/SpatioTextualConstants.xMaxRange*180)-90);
	//		 latlong.setLongitude((xy.getY()/SpatioTextualConstants.yMaxRange*360)-180);
	//		
	//         return latlong;
	//	}
	public static LatLong convertFromXYToLatLonTo(Point xy, double minLat, double minLong, double maxLat, double maxLong) {

		LatLong latlong = new LatLong();
		latlong.setLatitude((xy.getY() / SpatioTextualConstants.yMaxRange * (maxLat - minLat)) + minLat);
		latlong.setLongitude((xy.getX() / SpatioTextualConstants.xMaxRange * (maxLong - minLong)) + minLong);

		return latlong;
	}

	public static Double getArea(Rectangle rect) {
		return (rect.getMax().X - rect.getMin().X) * (rect.getMax().Y - rect.getMin().Y);
	}

	public static Double getDistanceInBetween(Point p1, Point p2) {
		Double dist = 0.0;
		dist = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
		return dist;

	}

	public static double getAreaInBetween(Point a, Point b, Point c) {
		return (b.X-a.X)*(c.Y-a.Y) - (b.Y-a.Y)*(c.X-a.X);

	}

	public static Double getMaxDistanceBetween(Point point, Rectangle recangle) {
		Point p1 = recangle.getMin();
		Point p2 = recangle.getMax();
		Point p3 = new Point(p1.getX(), p2.getY());
		Point p4 = new Point(p1.getY(), p2.getX());
		Double dist1 = getDistanceInBetween(point, p1);
		Double dist2 = getDistanceInBetween(point, p2);
		Double dist3 = getDistanceInBetween(point, p3);
		Double dist4 = getDistanceInBetween(point, p4);
		return Math.max(Math.max(dist1, dist2), Math.max(dist3, dist4));
	}

	public static Double getMinDistanceBetween(Point point, Rectangle recangle) {
		Double dx = Math.max(0.0, Math.max(recangle.getMin().getX() - point.getX(), point.getX() - recangle.getMax().getX()));
		Double dy = Math.max(0.0, Math.max(recangle.getMin().getY() - point.getY(), point.getY() - recangle.getMax().getY()));
		return Math.sqrt(dx * dx + dy * dy);
	}

	//	public static LatLong convertFromXYToLatLonTo(Point xy, Double latMin, Double lonMin, Double latMax, Double lonMax) {
	//
	//		LatLong latlong = new LatLong();
	//		latlong.setLatitude((xy.getX() / SpatioTextualConstants.xMaxRange * (latMax - latMin)) + latMin);
	//		latlong.setLongitude((xy.getY() / SpatioTextualConstants.yMaxRange * (lonMax - lonMin)) + lonMin);
	//
	//		return latlong;
	//	}

	public static Boolean insideSpatially(Rectangle bigRectangle, Rectangle smallRectangle) {
		if ((smallRectangle.getMin().getX() >= bigRectangle.getMin().getX() || Double.compare(smallRectangle.getMin().getX(), bigRectangle.getMax().getX()) == 0)
				&& (smallRectangle.getMax().getX() <= bigRectangle.getMax().getX() || Double.compare(smallRectangle.getMax().getX(), bigRectangle.getMin().getX()) == 0)
				&& (smallRectangle.getMin().getY() >= bigRectangle.getMin().getY() || Double.compare(smallRectangle.getMin().getY(), bigRectangle.getMax().getY()) == 0)
				&& (smallRectangle.getMax().getY() <= bigRectangle.getMax().getY() || Double.compare(smallRectangle.getMax().getY(), bigRectangle.getMin().getY()) == 0))
			return true;
		return false;
	}

	public static Point mapDataPointToIndexCellCoordinates(Point point, int xCellCount, int yCellCount, double xMaxRange, double yMaxRange) {

		Double xStep = xMaxRange / xCellCount;
		Double yStep = yMaxRange / yCellCount;
		Integer xCell = (int) (point.getX() / xStep);
		Integer yCell = (int) (point.getY() / yStep);
		if (xCell >= xCellCount)
			xCell = (int) ((xCellCount) - 1);
		if (yCell >= yCellCount)
			yCell = (int) ((yCellCount) - 1);
		if (xCell < 0)
			xCell = 0;
		if (yCell < 0)
			yCell = 0;
		return new Point((double) (xCell), (double) (yCell));
	}

	public static ArrayList<Point> mapRectangleToIndexCellCoordinatesListAll(Rectangle rectangle, int xCellCount, int yCellCount, double xMaxRange, double yMaxRange) {

		Double localXstep = xMaxRange / xCellCount;
		Double localYstep = yMaxRange / yCellCount;

		Rectangle selfBounds = new Rectangle(new Point(0, 0), new Point(xMaxRange, yMaxRange));

		ArrayList<Point> partitions = new ArrayList<Point>();
		double xmin, ymin, xmax, ymax;

		if (rectangle == null || selfBounds == null)
			return partitions;

		if (rectangle.getMin().getX() < selfBounds.getMin().getX())
			xmin = selfBounds.getMin().getX();
		else
			xmin = rectangle.getMin().getX();

		if (rectangle.getMin().getY() < selfBounds.getMin().getY())
			ymin = selfBounds.getMin().getY();
		else
			ymin = rectangle.getMin().getY();

		if (rectangle.getMax().getX() > selfBounds.getMax().getX())
			xmax = selfBounds.getMax().getX();//to prevent exceeding index range
		else
			xmax = rectangle.getMax().getX();

		if (rectangle.getMax().getY() > selfBounds.getMax().getY())
			ymax = selfBounds.getMax().getY();//to prevent exceeding index range
		else
			ymax = rectangle.getMax().getY();

		if (xmax == selfBounds.getMax().getX())
			xmax = selfBounds.getMax().getX() - 1;
		if (ymax == selfBounds.getMax().getY())
			ymax = selfBounds.getMax().getY() - 1;

		xmin -= selfBounds.getMin().getX();
		ymin -= selfBounds.getMin().getY();
		xmax -= selfBounds.getMin().getX();
		ymax -= selfBounds.getMin().getY();

		int xMinCell = (int) (xmin / localXstep);
		int yMinCell = (int) (ymin / localYstep);
		int xMaxCell = (int) (xmax / localXstep);
		int yMaxCell = (int) (ymax / localYstep);

		for (Integer xCell = xMinCell; xCell <= xMaxCell; xCell++)
			for (Integer yCell = yMinCell; yCell <= yMaxCell; yCell++) {
				Point indexCell = new Point(xCell, yCell);
				partitions.add(indexCell);
			}

		return partitions;

	}

	public static ArrayList<Point> mapRectangleToIndexCellCoordinatesListMinMax(Rectangle rectangle, int xCellCount, int yCellCount, double xMaxRange, double yMaxRange) {

		Double localXstep = xMaxRange / xCellCount;
		Double localYstep = yMaxRange / yCellCount;

		Rectangle selfBounds = new Rectangle(new Point(0, 0), new Point(xMaxRange, yMaxRange));

		ArrayList<Point> partitions = new ArrayList<Point>();
		double xmin, ymin, xmax, ymax;

		if (rectangle == null || selfBounds == null)
			return partitions;

		if (rectangle.getMin().getX() < selfBounds.getMin().getX())
			xmin = selfBounds.getMin().getX();
		else
			xmin = rectangle.getMin().getX();

		if (rectangle.getMin().getY() < selfBounds.getMin().getY())
			ymin = selfBounds.getMin().getY();
		else
			ymin = rectangle.getMin().getY();

		if (rectangle.getMax().getX() > selfBounds.getMax().getX())
			xmax = selfBounds.getMax().getX();//to prevent exceeding index range
		else
			xmax = rectangle.getMax().getX();

		if (rectangle.getMax().getY() > selfBounds.getMax().getY())
			ymax = selfBounds.getMax().getY();//to prevent exceeding index range
		else
			ymax = rectangle.getMax().getY();

		if (xmax == selfBounds.getMax().getX())
			xmax = selfBounds.getMax().getX() - 1;
		if (ymax == selfBounds.getMax().getY())
			ymax = selfBounds.getMax().getY() - 1;

		xmin -= selfBounds.getMin().getX();
		ymin -= selfBounds.getMin().getY();
		xmax -= selfBounds.getMin().getX();
		ymax -= selfBounds.getMin().getY();

		int xMinCell = (int) (xmin / localXstep);
		int yMinCell = (int) (ymin / localYstep);
		int xMaxCell = (int) (xmax / localXstep);
		int yMaxCell = (int) (ymax / localYstep);

		Point indexCell = new Point(xMinCell, yMinCell);
		partitions.add(indexCell);
		indexCell = new Point(xMaxCell, yMaxCell);
		partitions.add(indexCell);

		return partitions;

	}

	//	public static Boolean overlapsSpatially(Point point, Rectangle rectangle) {
	//		if (		(point.getX() >= rectangle.getMin().getX()||Math.abs(point.getX() - rectangle.getMin().getX())<.000001)
	//				&& (point.getX() <= rectangle.getMax().getX()||Math.abs(point.getX() - rectangle.getMax().getX())<.000001 )
	//				&& (point.getY() >= rectangle.getMin().getY()|| Math.abs(point.getY() -rectangle.getMin().getY())<.000001 )
	//				&& (point.getY() <= rectangle.getMax().getY()|| Math.abs(point.getY() - rectangle.getMax().getY())<.000001)
	//				)
	//			return true;
	//		return false;
	//	}
	public static Boolean overlapsSpatially(Point point, Rectangle rectangle) {
		if (Double.compare(point.getX(), rectangle.getMin().getX()) < 0 || Double.compare(point.getX(), rectangle.getMax().getX()) > 0 || Double.compare(point.getY(), rectangle.getMin().getY()) < 0
				|| Double.compare(point.getY(), rectangle.getMax().getY()) > 0)
			return false;
		return true;
	}

	public static Boolean overlapsSpatially(Rectangle rectangle1, Rectangle rectangle2) {
		if ((rectangle1.getMin().getX() <= rectangle2.getMax().getX() || Math.abs(rectangle1.getMin().getX() - rectangle2.getMax().getX()) < .000001)
				&& (rectangle1.getMax().getX() >= rectangle2.getMin().getX() || Math.abs(rectangle1.getMax().getX() - rectangle2.getMin().getX()) < .000001)
				&& (rectangle1.getMin().getY() <= rectangle2.getMax().getY() || Math.abs(rectangle1.getMin().getY() - rectangle2.getMax().getY()) < .000001)
				&& (rectangle1.getMax().getY() >= rectangle2.getMin().getY() || Math.abs(rectangle1.getMax().getY() - rectangle2.getMin().getY()) < .000001))
			return true;
		return false;
	}

	public static Rectangle spatialIntersect(Rectangle rect1, Rectangle rect2) {
		return new Rectangle(
				new Point(
						Math.max(rect1.getMin().getX(), rect2.getMin().getX()), 
						Math.max(rect1.getMin().getY(), rect2.getMin().getY())),
				new Point(
						Math.min(rect1.getMax().getX(), rect2.getMax().getX()), 
						Math.min(rect1.getMax().getY(), rect2.getMax().getY())));
	}

	public static Rectangle union(Rectangle rect1, Rectangle rect2) {
		return new Rectangle(new Point(Math.min(rect1.getMin().getX(), rect2.getMin().getX()), Math.min(rect1.getMin().getY(), rect2.getMin().getY())),
				new Point(Math.max(rect1.getMax().getX(), rect2.getMax().getX()), Math.max(rect1.getMax().getY(), rect2.getMax().getY())));
	}

	public static Rectangle expand(Rectangle rect1, Double area) {
		return new Rectangle(new Point(Math.max(rect1.getMin().getX() - area, 0), Math.max(rect1.getMin().getY() - area, 0)),
				new Point(Math.min(rect1.getMax().getX() + area, SpatioTextualConstants.xMaxRange), Math.min(rect1.getMax().getY() + area, SpatioTextualConstants.yMaxRange)));
	}



	/**
	 * Splits a partition into two partitions.
	 * 
	 * @param parent
	 *            The partition to be split.
	 * @param splitPosition
	 *            The position at which the split is to be done.
	 * @param isHorizontal
	 *            Whether the split is horizontal or vertical.
	 * @param costEstimator
	 *            This parameter is used when estimating the size and cost of
	 *            the resulting partitions.
	 * @return
	 */
	
	
}
