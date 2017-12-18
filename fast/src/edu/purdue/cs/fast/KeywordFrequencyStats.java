package edu.purdue.cs.fast;

public class KeywordFrequencyStats {
	public int queryCount;
	public int objectVisitCount;
	public int lastDecayTimeStamp;
	public KeywordFrequencyStats(int queryCount, int objectVisitCount, int timeStamp) {
		
		this.queryCount = queryCount;
		this.objectVisitCount = objectVisitCount;
		this.lastDecayTimeStamp = timeStamp;
	}
}

