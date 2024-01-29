package edu.purdue.cs.fast.baselines.fast;

public class LKeywordFrequencyStats {
	public int queryCount;
	public int objectVisitCount;
	public int lastDecayTimeStamp;
	public LKeywordFrequencyStats(int queryCount, int objectVisitCount, int timeStamp) {
		
		this.queryCount = queryCount;
		this.objectVisitCount = objectVisitCount;
		this.lastDecayTimeStamp = timeStamp;
	}
}

