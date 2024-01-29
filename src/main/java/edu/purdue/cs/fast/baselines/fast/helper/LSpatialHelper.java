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
package edu.purdue.cs.fast.baselines.fast.helper;

import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;

import java.util.ArrayList;

public class LSpatialHelper {
	private final static double QUARTERPI = Math.PI / 4.0;

	public static Double getArea(Rectangle rect) {
		return (rect.max.x - rect.min.x) * (rect.max.y - rect.min.y);
	}

	public static Double getDistanceInBetween(Point p1, Point p2) {
		Double dist = 0.0;
		dist = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
		return dist;

	}

	public static double getAreaInBetween(Point a, Point b, Point c) {
		return (b.x -a.x)*(c.y -a.y) - (b.y -a.y)*(c.x -a.x);

	}

	public static Double getMaxDistanceBetween(Point point, Rectangle recangle) {
		Point p1 = recangle.min;
		Point p2 = recangle.max;
		Point p3 = new Point(p1.x, p2.y);
		Point p4 = new Point(p1.y, p2.x);
		Double dist1 = getDistanceInBetween(point, p1);
		Double dist2 = getDistanceInBetween(point, p2);
		Double dist3 = getDistanceInBetween(point, p3);
		Double dist4 = getDistanceInBetween(point, p4);
		return Math.max(Math.max(dist1, dist2), Math.max(dist3, dist4));
	}

	public static Double getMinDistanceBetween(Point point, Rectangle recangle) {
		Double dx = Math.max(0.0, Math.max(recangle.min.x - point.x, point.x - recangle.max.x));
		Double dy = Math.max(0.0, Math.max(recangle.min.y - point.y, point.y - recangle.max.y));
		return Math.sqrt(dx * dx + dy * dy);
	}

	//	public static LatLong convertFromXYToLatLonTo(Point xy, Double latMin, Double lonMin, Double latMax, Double lonMax) {
	//
	//		LatLong latlong = new LatLong();
	//		latlong.setLatitude((xy.x / SpatioTextualConstants.xMaxRange * (latMax - latMin)) + latMin);
	//		latlong.setLongitude((xy.y / SpatioTextualConstants.yMaxRange * (lonMax - lonMin)) + lonMin);
	//
	//		return latlong;
	//	}

	public static Boolean insideSpatially(Rectangle bigRectangle, Rectangle smallRectangle) {
		if ((smallRectangle.min.x >= bigRectangle.min.x || Double.compare(smallRectangle.min.x, bigRectangle.max.x) == 0)
				&& (smallRectangle.max.x <= bigRectangle.max.x || Double.compare(smallRectangle.max.x, bigRectangle.min.x) == 0)
				&& (smallRectangle.min.y >= bigRectangle.min.y || Double.compare(smallRectangle.min.y, bigRectangle.max.y) == 0)
				&& (smallRectangle.max.y <= bigRectangle.max.y || Double.compare(smallRectangle.max.y, bigRectangle.min.y) == 0))
			return true;
		return false;
	}

	public static Point mapDataPointToIndexCellCoordinates(Point point, int xCellCount, int yCellCount, double xMaxRange, double yMaxRange) {

		Double xStep = xMaxRange / xCellCount;
		Double yStep = yMaxRange / yCellCount;
		Integer xCell = (int) (point.x / xStep);
		Integer yCell = (int) (point.y / yStep);
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

		if (rectangle.min.x < selfBounds.min.x)
			xmin = selfBounds.min.x;
		else
			xmin = rectangle.min.x;

		if (rectangle.min.y < selfBounds.min.y)
			ymin = selfBounds.min.y;
		else
			ymin = rectangle.min.y;

		if (rectangle.max.x > selfBounds.max.x)
			xmax = selfBounds.max.x;//to prevent exceeding index range
		else
			xmax = rectangle.max.x;

		if (rectangle.max.y > selfBounds.max.y)
			ymax = selfBounds.max.y;//to prevent exceeding index range
		else
			ymax = rectangle.max.y;

		if (xmax == selfBounds.max.x)
			xmax = selfBounds.max.x - 1;
		if (ymax == selfBounds.max.y)
			ymax = selfBounds.max.y - 1;

		xmin -= selfBounds.min.x;
		ymin -= selfBounds.min.y;
		xmax -= selfBounds.min.x;
		ymax -= selfBounds.min.y;

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

		if (rectangle.min.x < selfBounds.min.x)
			xmin = selfBounds.min.x;
		else
			xmin = rectangle.min.x;

		if (rectangle.min.y < selfBounds.min.y)
			ymin = selfBounds.min.y;
		else
			ymin = rectangle.min.y;

		if (rectangle.max.x > selfBounds.max.x)
			xmax = selfBounds.max.x;//to prevent exceeding index range
		else
			xmax = rectangle.max.x;

		if (rectangle.max.y > selfBounds.max.y)
			ymax = selfBounds.max.y;//to prevent exceeding index range
		else
			ymax = rectangle.max.y;

		if (xmax == selfBounds.max.x)
			xmax = selfBounds.max.x - 1;
		if (ymax == selfBounds.max.y)
			ymax = selfBounds.max.y - 1;

		xmin -= selfBounds.min.x;
		ymin -= selfBounds.min.y;
		xmax -= selfBounds.min.x;
		ymax -= selfBounds.min.y;

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
	//		if (		(point.x >= rectangle.min.x||Math.abs(point.x - rectangle.min.x)<.000001)
	//				&& (point.x <= rectangle.max.x||Math.abs(point.x - rectangle.max.x)<.000001 )
	//				&& (point.y >= rectangle.min.y|| Math.abs(point.y -rectangle.min.y)<.000001 )
	//				&& (point.y <= rectangle.max.y|| Math.abs(point.y - rectangle.max.y)<.000001)
	//				)
	//			return true;
	//		return false;
	//	}
	public static Boolean overlapsSpatially(Point point, Rectangle rectangle) {
		if (Double.compare(point.x, rectangle.min.x) < 0 || Double.compare(point.x, rectangle.max.x) > 0 || Double.compare(point.y, rectangle.min.y) < 0
				|| Double.compare(point.y, rectangle.max.y) > 0)
			return false;
		return true;
	}

	public static Boolean overlapsSpatially(Rectangle rectangle1, Rectangle rectangle2) {
		if ((rectangle1.min.x <= rectangle2.max.x || Math.abs(rectangle1.min.x - rectangle2.max.x) < .000001)
				&& (rectangle1.max.x >= rectangle2.min.x || Math.abs(rectangle1.max.x - rectangle2.min.x) < .000001)
				&& (rectangle1.min.y <= rectangle2.max.y || Math.abs(rectangle1.min.y - rectangle2.max.y) < .000001)
				&& (rectangle1.max.y >= rectangle2.min.y || Math.abs(rectangle1.max.y - rectangle2.min.y) < .000001))
			return true;
		return false;
	}

	public static Rectangle spatialIntersect(Rectangle rect1, Rectangle rect2) {
		return new Rectangle(
				new Point(
						Math.max(rect1.min.x, rect2.min.x), 
						Math.max(rect1.min.y, rect2.min.y)),
				new Point(
						Math.min(rect1.max.x, rect2.max.x), 
						Math.min(rect1.max.y, rect2.max.y)));
	}

	public static Rectangle union(Rectangle rect1, Rectangle rect2) {
		return new Rectangle(new Point(Math.min(rect1.min.x, rect2.min.x), Math.min(rect1.min.y, rect2.min.y)),
				new Point(Math.max(rect1.max.x, rect2.max.x), Math.max(rect1.max.y, rect2.max.y)));
	}

	public static Rectangle expand(Rectangle rect1, Double area) {
		return new Rectangle(new Point(Math.max(rect1.min.x - area, 0), Math.max(rect1.min.y - area, 0)),
				new Point(Math.min(rect1.max.x + area, LSpatioTextualConstants.xMaxRange), Math.min(rect1.max.y + area, LSpatioTextualConstants.yMaxRange)));
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
