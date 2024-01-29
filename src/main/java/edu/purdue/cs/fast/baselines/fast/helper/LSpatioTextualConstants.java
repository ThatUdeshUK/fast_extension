/**
r * Copyright Jul 5, 2015
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

public class LSpatioTextualConstants {
	//Properties file which has all the configurable parameters required for execution of this Topology.
	public static final String CONFIG_PROPERTIES_FILE = "resources/config.properties";
	public static final String CLUSTER_CONFIG_PROPERTIES_FILE = "resources/clusterconfig.properties";

	//**********************************************************************************
	//**********************Spatio-textual index constants
	public static String IndexIDExtension = "_index";
	public static String Index_Bolt_STreamIDExtension_Query = "_index_bolt_extension_query";
	public static String Index_Bolt_STreamIDExtension_Data = "_index_bolt_extension_data";
	public static String Index_Bolt_STreamIDExtension_Control = "_index_bolt_extension_control";
	public static String Index_Index_STreamIDExtension_Query = "_index_index_extension_query";
	public static String Index_Index_STreamIDExtension_Data = "_index_index_extension_data";
	public static String Index_Index_STreamIDExtension_Control = "_index_index_extension_control";
	public static String Bolt_Index_STreamIDExtension_Query = "_bolt_index_extension_query";
	public static String Bolt_Index_STreamIDExtension_Data = "_bolt_index_extension_data";
	public static String Bolt_Index_STreamIDExtension_Control = "_bolt_index_extension_control";
	public static String Bolt_Bolt_STreamIDExtension_Query = "_bolt_bolt_extension_query";
	public static String Bolt_Bolt_STreamIDExtension_Data = "_bolt_bolt_extension_data";
	public static String Bolt_Bolt_STreamIDExtension_Control = "_bolt_bolt_extension_control";
	public static String Bolt_Output_STreamIDExtension = "_bolt_output_extension";
	public static String Fields_Grouping_ID_Field = "id";
	public static String Persistent = "persistent";
	public static String Volatile = "volatile";
	public static String CLEAN = "CLEAN";
	public static String NOTCLEAN = "NOTCLEAN";
	public static String VOLATILECLEANSTATE = "VOLATILECLEANSTATE";
	public static String Current = "current";
	public static String Static = "static";
	public static String Continuous = "continuous";
	public static String Static_Source_Class_Name = "tornado.static.source.class.name";
	public static String Static_Source_Class_Config = "tornado.static.source.class.config";
	public static String Default = "default";
	//****************************************************************************
	//***********************Static DataSource 
	public static String HDFS = "HDFS";
	//****************************************************************************
	//***********************Spout constants
	public static final Integer DataTweetsSource = 2;
	public static final Integer QuerySource = 1;
	public static final Integer generatorSeed = 1000;
	public static final Integer dataGeneratorDelay = 100; // setting this to zero to achieve the highest data rate possible
	public static final Integer queryGeneratorDelay = 10000; // setting this to zero to achieve to send queries to bolts as  soon as possible

	public static final Integer numQueries = 10000;
	public static final Integer numMovingObjects = 100;

	//Data constants 
	public static final Double xMaxRange =10000.0;//16384.0 ;//
	public static final Double yMaxRange = 10000.0;//16384.0 ;//
	
	//find grid granuality constansnts
	public static final Integer defaultFineGridGranularityX =64;
	public static final Integer defaultFineGridGranularityY =64;
	
	public static final Double defaultFineGridGranularityXstep = xMaxRange/defaultFineGridGranularityX;
	public static final Double defaultFineGridGranularityYstep = yMaxRange/defaultFineGridGranularityX;
	

	//Data constants 
//	public static final Double minLat = -90.0;
//	public static final Double minLong = -180.0;
//	public static final Double maxLat = 90.0;
//	public static final Double maxLong =180.0;
	
	public static final Double usaMinLat = 20.0;
	public static final Double usaMinLong = -130.0;
	public static final Double usaMaxLat = 50.0;
	public static final Double usaMaxLong = -60.0;

	public static final Double minLat = usaMinLat;
	public static final Double minLong =usaMinLong;
	public static final Double maxLat = usaMaxLat;
	public static final Double maxLong =usaMaxLong;//	48.491158, -61.327106

	//Control message types 
	public static final String control = "control";
	public static final String query = "query";
	public static final String data = "data";
	public static final String output = "output";

	// Object's fields
	public static final String dataObject="dataObject"; 
	public static final String objectIdField = Fields_Grouping_ID_Field + "_Object";
	public static final String objectXCoordField = "xCoord";
	public static final String objectYCoordField = "yCoord";
	public static final String objectTextField = "textContent";
	public static final String incrementalState = "incrementalState";
	public static final String timeStamp = "timeStamp";

	public static final String queryIdField = Fields_Grouping_ID_Field + "_Query";

	// Text Query field
	public static final String queryTextField = "queryText";
	public static final String queryText2Field = "queryText2";
	public static final String queryComplexTextField = "complexText";
	// Text Timesatoamp field
	public static final String queryTimeStampField = "queryTimeStampField";

	public static final String dataSrc = "dataSrc";
	public static final String dataSrc2 = "dataSrc2";

	public static final String continuousPersistenceState = "continuousPersistenceState";// continuous query
	public static final String staticPersistenceState = "staticPersistenceState"; //static data source
	public static final String volatilePersistenceState = "volatilePersistenceState"; //volatile data source, not maintained in memory or snapshot query
	public static final String currentPersistenceState = "currentPersistenceState";//keeping only the current location of moving object
	public static final String persistentPersistenceState = "persistentPersistenceState";//persistent data with a window

	public static final String queryCommand = "queryCommand";
	public static final String dataObjectCommand = "dataObjectCommand";

	// Range Query fields
	public static final String queryXMinField = "xMin";
	public static final String queryYMinField = "yMin";
	public static final String queryXMaxField = "xMax";
	public static final String queryYMaxField = "yMax";

	// kNN Query fields
	public static final String focalXCoordField = "focalXCoord";
	public static final String focalYCoordField = "focalYCoord";
	public static final String kField = "k";
	public static final String updateStatus = "updateStatus";

	// Distance join Query fields
	public static final String queryDistance = "distance";
	// HotSpot Paramter
	public static final String hotSpotRatio = "hotSpotRatio";
	//Query types 
	public static final String queryTypeField = "type";
	//public static final String queryTextualKNN = "TextualKNN";
	//public static final String queryTextualRange = "TextualRange";
	//public static final String queryTextualSpatialJoin = "TextualSpatialJoin";

	//Text constants
	public static final String  textDelimiter = " ";
	public static final String  queryIdDelimiter = "_";
	public static final Integer relevenatKeyWordMinSize = 2; //for stopword min length identification
	public static final Integer relevenatKeyWordMaxSize = 15;//for stopword max length identification
	
	public static final String textualPredicate = "textualPredicate";
	public static final String textualPredicate2 = "textualPredicate2";
	public static final String joinTextualPredicate = "joinTextualPredicate";
	public static final String removeTime = "removeTime";
	

	//****************************************************************************
	//***********************Kafka constants
	public static final String kafkaZookeeper = "kafkaZookeeper";//"localhost:2181";
	public static final String kafkaConsumerGroup = "kafkaConsumerGroup";//"queries";
	public static final String kafkaConsumerTopic = "kafkaConsumerTopic";//"queries";
	public static final String kafkaProducerTopic = "kafkaProducerTopic";//"output";
	public static final String kafkaBootstrapServerConfig = "kafkaBootstrapServerConfig";//"localhost:9092";
	
	
	
	
	//****************************************************************************
	//***********************Semantic constansts
	public static Integer maxSimilarKeywords = 20;
	public static final String discoDir = "discoDir";

	//****************************************************************************
	//***********************RUN constants
	public static final String localCluster = "localCluster";
	public static final String stormServer = "stormServer";
	public static final String stormSubmitType = "stormSubmitType";
	public static final String stormNimbusServer = "stormNimbusServer";
	public static final String NIMBUS_HOST = "NIMBUS_HOST";
	public static final String NIMBUS_THRIFT_PORT = "NIMBUS_THRIFT_PORT";
	public static final String UI_HOST = "UI_HOST";
	public static final String UI_PORT = "UI_PORT";
	public static final String STORM_ZOOKEEPER_PORT = "STORM_ZOOKEEPER_PORT";
	public static final String STORM_JAR_PATH = "STORM_JAR_PATH";
	public static final String STORM_NUMBER_OF_WORKERS = "STORM_NUMBER_OF_WORKERS";
	public static final String STORM_ZOOKEEPER_SERVERS = "STORM_ZOOKEEPER_SERVERS";

	public static String getBoltBoltControlStreamId(String id) {
		return (id + LSpatioTextualConstants.Bolt_Bolt_STreamIDExtension_Control);
	}

	public static String getBoltIndexControlStreamId(String id) {
		return (id + LSpatioTextualConstants.Bolt_Index_STreamIDExtension_Control);
	}
	public static String getIndexBoltControlStreamId(String id) {
		return (id + LSpatioTextualConstants.Index_Bolt_STreamIDExtension_Control);
	}
	public static String getIndexBoltQueryStreamId(String id) {
		return (id + LSpatioTextualConstants.Index_Bolt_STreamIDExtension_Query);
	}
	public static String getIndexBoltDataStreamId(String id) {
		return (id + LSpatioTextualConstants.Index_Bolt_STreamIDExtension_Data);
	}
	public static String getIndexIndexControlStreamId(String id) {
		return (id + LSpatioTextualConstants.Index_Index_STreamIDExtension_Control);
	}
	public static String getIndexIndexDataStreamId(String id) {
		return (id + LSpatioTextualConstants.Index_Index_STreamIDExtension_Data);
	}
	public static String getIndexIndexQueryStreamId(String id) {
		return (id + LSpatioTextualConstants.Index_Index_STreamIDExtension_Query);
	}
	public static String getVolatilePropertyKey(String componentId) {
		return (LSpatioTextualConstants.VOLATILECLEANSTATE + "_" + componentId);
	}
	public static String getVolatilePropertyKey(String componentId, String streamId) {
		return (LSpatioTextualConstants.VOLATILECLEANSTATE + "_" + componentId + "_" + streamId);
	}
	public static Boolean isControlStreamSource(String sourceType) {
		return sourceType.contains(LSpatioTextualConstants.Index_Bolt_STreamIDExtension_Control) || sourceType.contains(LSpatioTextualConstants.Bolt_Bolt_STreamIDExtension_Control)|| sourceType.contains(LSpatioTextualConstants.Bolt_Index_STreamIDExtension_Control)|| sourceType.contains(LSpatioTextualConstants.Index_Index_STreamIDExtension_Control);
	}
	public static Boolean isDataStreamSource(String sourceType) {
		return sourceType.contains(LSpatioTextualConstants.Index_Bolt_STreamIDExtension_Data) || sourceType.contains(LSpatioTextualConstants.Bolt_Bolt_STreamIDExtension_Data);
	}
	public static String getIndexId(String id){
		return  id + LSpatioTextualConstants.IndexIDExtension;
	}
}
