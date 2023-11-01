package edu.purdue.cs.fast.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

public class FileSpout  {
	public static final String FILE_PATH = "FILE_PATH";
	public static final String FILE_SYS_TYPE = "FILE_SYS_TYPE";
	public static final String HDFS = "HDFS";
	public static final String LFS = "LFS";
	public static final String CORE_FILE_PATH = "CORE_FILE_PATH";
	public static final String EMIT_SLEEP_DURATION_NANOSEC = "EMIT_SLEEP_DURATION_NANOSEC";
	public static final long serialVersionUID = 1L;
	public Integer initialSleepDuration;
	 Integer selfTaskId;
	 Integer selfTaskIndex;
	 Boolean reliable;
	 int count;

	//public Map conf;
	public  BufferedReader br;
	//public  Configuration hdfsconf;
	public  FileInputStream fstream;
	public  String filePath;
	public  String corePath;
	//public Path pt;
	public String fileSystemType;
	public Integer sleepDurationMicroSec;
	public Map spoutConf;

	public FileSpout(Map spoutConf,Integer initialSleepDuration) {
		this.spoutConf = spoutConf;
		this.initialSleepDuration = initialSleepDuration;
	}

	public void ack(Object msgId) {
	}

	public void close() {
	}

	public void fail(Object msgId) {
	}

	

	public void connectToFS() {
		if (fileSystemType.equals(HDFS)) {
			connectToHDFS();
		} else {
			connectToLFS();

		}
	}

	public void sleep() {
		if (sleepDurationMicroSec != 0) {
//			try {
//				//TimeUnit.NANOSECONDS.sleep(sleepDurationNanoSec);
//				Thread.sleep(0,sleepDurationMicroSec);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

		}
	}

	public void connectToLFS() {
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filePath);
			br = new BufferedReader(new InputStreamReader(fstream));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void connectToHDFS() {
		try {
//			FileSystem fs = FileSystem.get(hdfsconf);
//			br = new BufferedReader(new InputStreamReader(fs.open(pt)));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public void nextTuple() {
		if(count>=100000){
			count=0;
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		count++;
	}

	

}
