package edu.purdue.cs.fast.readers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import edu.purdue.cs.fast.helper.Command;
import edu.purdue.cs.fast.helper.LatLong;
import edu.purdue.cs.fast.helper.Point;
import edu.purdue.cs.fast.helper.QueryType;
import edu.purdue.cs.fast.helper.RandomGenerator;
import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.helper.SpatioTextualConstants;
import edu.purdue.cs.fast.helper.TextHelpers;
import edu.purdue.cs.fast.helper.TextualPredicate;
import edu.purdue.cs.fast.messages.Query;

public class QueriesReader extends FileSpout {
	public QueriesReader(Map spoutConf, Integer initialSleepDuration) {
		super(spoutConf, initialSleepDuration);

	}

	public static String SPATIAL_RANGE = "SPATIAL_RANGE"; //1  [10] 100 500 out of 10000
	public static String KEYWORD_COUNT = "KEYWORD_COUNT"; //1  [5] 10 20  
	public static String TOTAL_QUERY_COUNT = "TOTAL_QUERY_COUNT"; //1000 [100000]  1000000
	public static Double spatialRangeVal = 10.0;
	public static Integer keywordCountVal = 5;
	public static Integer totalQueryCountVal = 100000;
	public static Integer k = 5;
	public static QueryType queryType;
	public static String dataSrc1, dataSrc2;
	public static TextualPredicate textualPredicate1, textualPredicate2, joinTextualPredicate;
	public static Double distance;
	static ArrayList<LatLong> previousLocations;
	static ArrayList<String> previousTextList;
	static LatLong latLong;
	static int prevLocCount = 0;

	public static RandomGenerator r;
	static Integer i = new Integer(0);

	public void ack(Object msgId) {
	}

	public void close() {
	}

	public void fail(Object msgId) {
	}

	@Override
	public void nextTuple() {
		//	super.nextTuple();
		if (i >= this.totalQueryCountVal) {
			try {
				if (previousLocations != null || br != null) {
					System.out.println("Used previous loc: " + prevLocCount);
					br.close();
					previousLocations.clear();
					previousLocations = null;
					System.gc();
					System.gc();
					System.runFinalization();
					br = null;
				}
				Thread.sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		String line = "";
		try {

			// Read File Line By Line
			if ((line = br.readLine()) == null) {
				System.out.println("null line");
				br.close();
				connectToFS();

			}
			if ("".equals(line)) {
				System.out.println("empty line");
				if ((line = br.readLine()) == null) {
					System.out.println("null line");
					br.close();
					connectToFS();

				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;

		}
		if (line == null || line.isEmpty() || "".equals(line)) {
			System.out.println("null line");
			return;
		}
		//System.out.println(tweet);

		emitQuery(line, i);

		//	sleep();

	}

	//	private void emitQuery(String line, Integer msgId) {
	//
	//		Query q = buildQuery(line);
	//		for (int j = 0; j < 4 && i < this.totalQueryCountVal; j++) {
	//			Query qold = q;
	//			q = new Query(qold);
	//			q.setQueryId(i+selfTaskIndex*totalQueryCountVal);
	//			this.collector.emit(new Values(q.getQueryId(), q
	//			), i);
	//			i = i + 1;
	//			
	//		}
	//
	//	}
	private void emitQuery(String line, Integer msgId) {
		//System.out.println("Tweet line:"+line);
		Query q = buildQuery(line);
		//for (int j = 0; j < 4 && i < this.totalQueryCountVal; j++) {
		//	Query qold = q;
		//	q = new Query(qold);
		if (q == null) {
			System.out.println("null Query");
			return;

		}
		q.setQueryId(i + selfTaskIndex * totalQueryCountVal);
		i = i + 1;
		//		this.collector.emit(new Values(q.getQueryType(), q.getQueryId(), q.getFocalPoint().getX(), q.getFocalPoint().getY(), q.getSpatialRange().getMin().getX(), q.getSpatialRange().getMin().getY(), q.getSpatialRange().getMax().getX(),
		//				q.getSpatialRange().getMax().getY(), q.getK(), TextHelpers.convertArrayListOfStringToText(q.getQueryText()), TextHelpers.convertArrayListOfStringToText(q.getQueryText2()), q.getTimeStamp(), q.getDataSrc(), q.getDataSrc2(),
		//				q.getCommand(), q.getDistance(), q.getTextualPredicate(), q.getTextualPredicate2(), q.getJoinTextualPredicate(),q.getRemoveTime()
		//
		//		));

		//}

	}

	public Query buildQuery(String line, String scId, int keywordCount, String data1, String data2, Double distance, QueryType queryType, Double spatialRangeVal, TextualPredicate textualPredicate1, TextualPredicate textualPredicate2,
			Integer k) {
		if (previousLocations == null || selfTaskId == null || selfTaskIndex == null) {
			previousLocations = new ArrayList<LatLong>();
			r = new RandomGenerator(0);
			selfTaskId = 0;
			selfTaskIndex = 0;
			i = 0;
		}

		keywordCountVal = keywordCount;
		dataSrc1 = data1;
		//dataSrc2 = data2;
		//QueriesFileSystemSpout.distance = distance;
		QueriesReader.queryType = queryType;
		QueriesReader.spatialRangeVal = spatialRangeVal;
		QueriesReader.textualPredicate1 = textualPredicate1;
		//QueriesFileSystemSpout.textualPredicate2 = textualPredicate2;
		Query q = buildQuery(line);
		if (q != null) {
			q.setQueryId(i + selfTaskIndex * totalQueryCountVal);
			i = i + 1;
			q.setSrcId(scId);
		}
		return q;
	}
	public Query buildQueryFromSynthetic(String line, String scId, int keywordCount, String data1, String data2, Double distance, QueryType queryType, Double spatialRangeVal, TextualPredicate textualPredicate1, TextualPredicate textualPredicate2,
			Integer k) {
		if (previousLocations == null || selfTaskId == null || selfTaskIndex == null) {
			previousLocations = new ArrayList<LatLong>();
			r = new RandomGenerator(0);
			selfTaskId = 0;
			selfTaskIndex = 0;
			i = 0;
		}

		keywordCountVal = keywordCount;
		dataSrc1 = data1;
		//dataSrc2 = data2;
		//QueriesFileSystemSpout.distance = distance;
		QueriesReader.queryType = queryType;
		QueriesReader.spatialRangeVal = spatialRangeVal;
		QueriesReader.textualPredicate1 = textualPredicate1;
		//QueriesFileSystemSpout.textualPredicate2 = textualPredicate2;
		Query q = buildQueryfromSynthetic(line);
		if (q != null) {
			q.setQueryId(i + selfTaskIndex * totalQueryCountVal);
			i = i + 1;
			q.setSrcId(scId);
		}
		return q;
	}
	public Query buildQueryFromPoi(String line, String scId, int keywordCount, String data1, String data2, Double distance, QueryType queryType, Double spatialRangeVal, TextualPredicate textualPredicate1, TextualPredicate textualPredicate2,
			Integer k) {
		if (previousLocations == null || selfTaskId == null || selfTaskIndex == null) {
			previousLocations = new ArrayList<LatLong>();
			r = new RandomGenerator(0);
			selfTaskId = 0;
			selfTaskIndex = 0;
			i = 0;
		}

		keywordCountVal = keywordCount;
		dataSrc1 = data1;
		//dataSrc2 = data2;
		//QueriesFileSystemSpout.distance = distance;
		QueriesReader.queryType = queryType;
		QueriesReader.spatialRangeVal = spatialRangeVal;
		QueriesReader.textualPredicate1 = textualPredicate1;
		//QueriesFileSystemSpout.textualPredicate2 = textualPredicate2;
		Query q = buildQueryFromPoi(line);
		if (q != null) {
			q.setQueryId(i + selfTaskIndex * totalQueryCountVal);
			i = i + 1;
			q.setSrcId(scId);
		}
		return q;
	}
	

	public Query buildQuery(String line) {
		//String[] tweetParts = line.split(",");
				try {
					int from = 0, to = 0;
					to = line.indexOf(',');
					//		if (tweetParts.length < 5) {
					//			System.out.println("Improper tweet format <5:" + line);
					//			return null;
					//		}

					if (from == -1 || to == -1)
						return null;
					String idstring = line.substring(from, to);
					from = to;
					to = line.indexOf(',', from + 1);
					if (to == -1)
						return null;
					String dateString = line.substring(from + 1, to);
					from = to;
					to = line.indexOf(',', from + 1);

					double lat = 0.0;
					double lon = 0.0;

					if (from == -1 || to == -1)
						return null;
					lat = Double.parseDouble(line.substring(from + 1, to));
					from = to;
					to = line.indexOf(',', from + 1);
					if (to == -1)
						return null;
					lon = Double.parseDouble(line.substring(from + 1, to));

					if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat || lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong
							|| (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)) {

						if (previousLocations.size() > 0) {
							latLong = previousLocations.get(r.nextInt(previousLocations.size()));
							prevLocCount++;
						} else {
							System.out.println("out of bounds of entire space lat lon:" + lat + "," + lon + "," + line);
							return null;
						}
					} else {
						latLong = new LatLong(lat, lon);
						previousLocations.add(latLong);

					}
					from = to;
					to = line.indexOf(',', from + 1);
					from = to;
					to = line.indexOf(',', from + 1);

					String textContent = line.substring(from + 1);
					ArrayList<String> queryText1 = new ArrayList<String>();
					ArrayList<String> textList = TextHelpers.transformIntoSortedArrayListOfString(textContent);
					if (textList.size() < keywordCountVal) {
						//return null;
						if (previousTextList == null)
							return null;
						textList.addAll(previousTextList);
					} else {
						previousTextList = textList;
					}
					//Collections.shuffle(textList, new Random(0));
					for (int j = 0; j < keywordCountVal; j++) {
						queryText1.add(textList.get(j));
						//queryText1.add(textList.get(r.nextInt(textList.size())));
						//queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
					}
					Collections.sort(queryText1);
					//			for (int j = 0; j < keywordCountVal; j++) {
					//
					//				queryText1.add(textList.get(j));
					//			//	queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
					//			}
					Integer id = i + selfTaskIndex * totalQueryCountVal;
					Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong);
					Date date = new Date();
					ArrayList<String> queryText2 = new ArrayList<String>();
					Query q = new Query();
					q.setQueryId(id);
					q.setCommand(Command.addCommand);
					//	q.setContinousQuery(true);
					q.setDataSrc(dataSrc1);
					//	q.setDistance(distance);
					q.setTimeStamp(date.getTime());
					q.setRemoveTime(Long.MAX_VALUE);
					if (queryType.equals(QueryType.queryTextualRange)) {
						q.setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
						q.setTextualPredicate(textualPredicate1);
						if (TextualPredicate.BOOLEAN_EXPR.equals(textualPredicate1)) {
							if (keywordCountVal == 1) {
								q.setTextualPredicate(TextualPredicate.OVERlAPS);
								q.setQueryText(queryText1);
							} else if (keywordCountVal == 2) {
								q.setTextualPredicate(TextualPredicate.CONTAINS);
								q.setQueryText(queryText1);
							} else {
								ArrayList<ArrayList<String>> complexKeywords = new ArrayList<ArrayList<String>>();
								int howManySplitsSecontions = r.nextInt(Math.max(keywordCountVal / 2, 2));
								int start = 0;
								int batchSize = keywordCountVal / (howManySplitsSecontions + 1);
								int j = 0;
								for (; j < howManySplitsSecontions; j++) {
									complexKeywords.add(new ArrayList<String>());
									for (int k = 0; k < batchSize; k++) {
										complexKeywords.get(j).add(queryText1.get(start++));
									}
								}
								//add the remaing keywords in the last batch;
								while (complexKeywords.size() <= howManySplitsSecontions) {
									complexKeywords.add(new ArrayList<String>());
								}
								while (start < keywordCountVal)
									complexKeywords.get(j).add(queryText1.get(start++));
							}

						} else {
							q.setQueryText(queryText1);
						}
					} 
					return q;
				} catch (Exception e) {
					System.out.println("unable to parse line" + line);
					e.printStackTrace();
					return null;
				}
		}
	public Query buildQueryfromSynthetic(String line) {
		//String[] tweetParts = line.split(",");
		try {
			String[] lineParts = line.split(",");
			if (lineParts.length < 4)
				return null;
			// String id = tweetParts[0];
			Integer id = Integer.parseInt(lineParts[0]);
	
			double x = 0.0;
			double y = 0.0;

			try {
				x = Double.parseDouble(lineParts[1]);

				y = Double.parseDouble(lineParts[2]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			String textContent = "";
			int i = 3;
			while (i < lineParts.length)
				textContent = textContent + lineParts[i++] + " ";
			ArrayList<String> queryText1 = new ArrayList<String>();
			ArrayList<String> textList = TextHelpers.transformIntoSortedArrayListOfString(textContent);
			if (textList.size() < keywordCountVal) {
				//return null;
				if (previousTextList == null)
					return null;
				textList.addAll(previousTextList);
			} else {
				previousTextList = textList;
			}
			//Collections.shuffle(textList, new Random(0));
			for (int j = 0; j < keywordCountVal; j++) {
				queryText1.add(textList.get(j));
				//queryText1.add(textList.get(r.nextInt(textList.size())));
				//queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
			}
			Collections.sort(queryText1);
			//			for (int j = 0; j < keywordCountVal; j++) {
			//
			//				queryText1.add(textList.get(j));
			//			//	queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
			//			}
		//	Integer id = i + selfTaskIndex * totalQueryCountVal;
			Point xy = new Point(x,y);//SpatialHelper.convertFromLatLonToXYPoint(latLong);
			Date date = new Date();
			ArrayList<String> queryText2 = new ArrayList<String>();
			Query q = new Query();
			q.setQueryId(id);
			q.setCommand(Command.addCommand);
			//	q.setContinousQuery(true);
			q.setDataSrc(dataSrc1);
			//	q.setDistance(distance);
			q.setTimeStamp(date.getTime());
			q.setRemoveTime(Long.MAX_VALUE);
			if (queryType.equals(QueryType.queryTextualRange)) {
				q.setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
				q.setTextualPredicate(textualPredicate1);
				if (TextualPredicate.BOOLEAN_EXPR.equals(textualPredicate1)) {
					if (keywordCountVal == 1) {
						q.setTextualPredicate(TextualPredicate.OVERlAPS);
						q.setQueryText(queryText1);
					} else if (keywordCountVal == 2) {
						q.setTextualPredicate(TextualPredicate.CONTAINS);
						q.setQueryText(queryText1);
					} else {
						ArrayList<ArrayList<String>> complexKeywords = new ArrayList<ArrayList<String>>();
						int howManySplitsSecontions = r.nextInt(Math.max(keywordCountVal / 2, 2));
						int start = 0;
						int batchSize = keywordCountVal / (howManySplitsSecontions + 1);
						int j = 0;
						for (; j < howManySplitsSecontions; j++) {
							complexKeywords.add(new ArrayList<String>());
							for (int k = 0; k < batchSize; k++) {
								complexKeywords.get(j).add(queryText1.get(start++));
							}
						}
						//add the remaing keywords in the last batch;
						while (complexKeywords.size() <= howManySplitsSecontions) {
							complexKeywords.add(new ArrayList<String>());
						}
						while (start < keywordCountVal)
							complexKeywords.get(j).add(queryText1.get(start++));
					}

				} else {
					q.setQueryText(queryText1);
				}
			} 
			return q;
		} catch (Exception e) {
			System.out.println("unable to parse line" + line);
			e.printStackTrace();
			return null;
		}
	}

	public Query buildQueryFromPoi(String line) {
		//String[] tweetParts = line.split(",");
		try {
			StringTokenizer stringTokenizer = new StringTokenizer(line, ",");
			String idString = "", text = "";
			Double lat, lon;
			if (stringTokenizer.hasMoreTokens()) {
				idString = stringTokenizer.nextToken();
			} else
				return null;
			if (stringTokenizer.hasMoreTokens()) {
				lat = Double.parseDouble(stringTokenizer.nextToken());
			} else
				return null;
			if (stringTokenizer.hasMoreTokens()) {
				lon = Double.parseDouble(stringTokenizer.nextToken());
			} else
				return null;
			text = "";
			while (stringTokenizer.hasMoreTokens()) {
				text +=(" "+ stringTokenizer.nextToken());
			} 

			if (lat < SpatioTextualConstants.minLat || lat > SpatioTextualConstants.maxLat || lon < SpatioTextualConstants.minLong || lon > SpatioTextualConstants.maxLong
					|| (Double.compare(lat, 0.0) == 0 && Double.compare(lon, 0.0) == 0)) {

				if (previousLocations.size() > 0) {
					latLong = previousLocations.get(r.nextInt(previousLocations.size()));
					prevLocCount++;
				} else {
					System.out.println("out of bounds of entire space lat lon:" + lat + "," + lon + "," + line);
					return null;
				}
			} else {
				latLong = new LatLong(lat, lon);
				previousLocations.add(latLong);

			}
			

			String textContent =text;
			ArrayList<String> queryText1 = new ArrayList<String>();
			ArrayList<String> textList = TextHelpers.transformIntoArrayListOfString(textContent);
			if (textList.size() < keywordCountVal) {
				if (previousTextList == null)
					return null;
				textList.addAll(previousTextList);
			} else {
				previousTextList = textList;
			}

			for (int j = 0; j < keywordCountVal; j++) {
				queryText1.add(textList.get(j));
				//queryText1.add(textList.get(r.nextInt(textList.size())));
				//queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
			}
			Collections.sort(queryText1);
			//			for (int j = 0; j < keywordCountVal; j++) {
			//
			//				queryText1.add(textList.get(j));
			//			//	queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
			//			}
			Integer id = i + selfTaskIndex * totalQueryCountVal;
			Point xy = SpatialHelper.convertFromLatLonToXYPoint(latLong);
			Date date = new Date();
			ArrayList<String> queryText2 = new ArrayList<String>();
			Query q = new Query();
			q.setQueryId(id);
			q.setCommand(Command.addCommand);
			//	q.setContinousQuery(true);
			q.setDataSrc(dataSrc1);
			//	q.setDistance(distance);
			q.setTimeStamp(date.getTime());
			q.setRemoveTime(Long.MAX_VALUE);
			if (queryType.equals(QueryType.queryTextualRange)) {
				q.setSpatialRange(new Rectangle(xy, new Point(xy.getX() + spatialRangeVal, xy.getY() + spatialRangeVal)));
				q.setTextualPredicate(textualPredicate1);
				if (TextualPredicate.BOOLEAN_EXPR.equals(textualPredicate1)) {
					if (keywordCountVal == 1) {
						q.setTextualPredicate(TextualPredicate.OVERlAPS);
						q.setQueryText(queryText1);
					} else if (keywordCountVal == 2) {
						q.setTextualPredicate(TextualPredicate.CONTAINS);
						q.setQueryText(queryText1);
					} else {
						ArrayList<ArrayList<String>> complexKeywords = new ArrayList<ArrayList<String>>();
						int howManySplitsSecontions = r.nextInt(Math.max(keywordCountVal / 2, 2));
						int start = 0;
						int batchSize = keywordCountVal / (howManySplitsSecontions + 1);
						int j = 0;
						for (; j < howManySplitsSecontions; j++) {
							complexKeywords.add(new ArrayList<String>());
							for (int k = 0; k < batchSize; k++) {
								complexKeywords.get(j).add(queryText1.get(start++));
							}
						}
						//add the remaing keywords in the last batch;
						while (complexKeywords.size() <= howManySplitsSecontions) {
							complexKeywords.add(new ArrayList<String>());
						}
						while (start < keywordCountVal)
							complexKeywords.get(j).add(queryText1.get(start++));
					}

				} else {
					q.setQueryText(queryText1);
				}
			} 
			return q;
		} catch (Exception e) {
			System.out.println("unable to parse line" + line);
			e.printStackTrace();
			return null;
		}
	}

	//	private Query buildQuery(String line) {
	//		StringTokenizer stringTokenizer = new StringTokenizer(line, ",");
	//
	//		Integer id = stringTokenizer.hasMoreTokens() ? Integer.parseInt(stringTokenizer.nextToken()) : i;
	//		Double xCoord = 0.0;
	//		Double yCoord = 0.0;
	//		try {
	//			xCoord = stringTokenizer.hasMoreTokens() ? Double.parseDouble(stringTokenizer.nextToken()) : 0.0;
	//
	//			yCoord = stringTokenizer.hasMoreTokens() ? Double.parseDouble(stringTokenizer.nextToken()) : 0.0;
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		String textContent = "";
	//		while (stringTokenizer.hasMoreTokens())
	//			textContent = textContent + stringTokenizer.nextToken() + " ";
	//		String[] keywordsArr = textContent.split(" ");
	//		ArrayList<String> queryText1 = new ArrayList<String>();
	//		ArrayList<String> queryText2 = new ArrayList<String>();
	//
	//		for (int i = 0; i < keywordCountVal; i++) {
	//			queryText1.add(keywordsArr[i]);
	//			queryText2.add(keywordsArr[keywordsArr.length - i - 1]);
	//		}
	//		queryText1=	 TextHelpers.sortTextArrayList(queryText1);
	//		Date date = new Date();
	//
	//		Query q = new Query();
	//		q.setQueryId(id);
	//		q.setCommand(Command.addCommand);
	//		q.setContinousQuery(true);
	//		q.setDataSrc(dataSrc1);
	//		q.setDistance(distance);
	//		q.setQueryType(queryType);
	//		q.setTimeStamp(date.getTime());
	//		if (queryType.equals(QueryType.queryTextualRange)) {
	//			q.setSpatialRange(new Rectangle(new Point(xCoord, yCoord), new Point(xCoord + this.spatialRangeVal, yCoord + this.spatialRangeVal)));
	//			q.setTextualPredicate(textualPredicate1);
	//			q.setQueryText(queryText1);
	//		} else if (queryType.equals(QueryType.queryTextualKNN)) {
	//			q.setFocalPoint(new Point(xCoord, yCoord));
	//			q.setTextualPredicate(textualPredicate1);
	//			q.setQueryText(queryText1);
	//			q.setK(k);
	//		} else if (queryType.equals(QueryType.queryTextualSpatialJoin)) {
	//			q.setDataSrc2(dataSrc2);
	//			q.setSpatialRange(new Rectangle(new Point(xCoord, yCoord), new Point(xCoord + this.spatialRangeVal, yCoord + this.spatialRangeVal)));
	//			q.setTextualPredicate(textualPredicate1);
	//			q.setTextualPredicate2(textualPredicate2);
	//			q.setQueryText(queryText1);
	//			q.setQueryText(queryText2);
	//			q.setDistance(distance);
	//		}
	//		return q;
	//	}
	


}
