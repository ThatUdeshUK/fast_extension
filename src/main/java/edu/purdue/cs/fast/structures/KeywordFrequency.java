package edu.purdue.cs.fast.structures;

public class KeywordFrequency {
	public int queryCount;
	public int visitCount;
	public int lastDecayTimeStamp;

	public KeywordFrequency(int queryCount, int visitCount, int timeStamp) {
		this.queryCount = queryCount;
		this.visitCount = visitCount;
		this.lastDecayTimeStamp = timeStamp;
	}
}

