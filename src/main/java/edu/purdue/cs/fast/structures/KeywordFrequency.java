package edu.purdue.cs.fast.structures;

import java.io.Serializable;

public class KeywordFrequency implements Serializable {
	public int queryCount;
	public int visitCount;
	public int lastDecayTimeStamp;

	public KeywordFrequency(int queryCount, int visitCount, int timeStamp) {
		this.queryCount = queryCount;
		this.visitCount = visitCount;
		this.lastDecayTimeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return "" + queryCount;
	}
}

