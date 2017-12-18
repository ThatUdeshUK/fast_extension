package edu.purdue.cs.fast.messages;

import java.util.ArrayList;

import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.helper.TextualPredicate;

public class MinimalRangeQuery {
	public  int queryId;
	public boolean added;
	public boolean deleted;
	
	public ArrayList<String> queryText;
	public Rectangle spatialRange;
	public TextualPredicate textualPredicate;
	public int expireTime;
	
	public int getQueryId() {
		return queryId;
	}
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}

	public boolean isAdded() {
		return added;
	}
	public void setAdded(boolean added) {
		this.added = added;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public ArrayList<String> getQueryText() {
		return queryText;
	}
	public void setQueryText(ArrayList<String> queryText) {
		this.queryText = queryText;
	}
	public Rectangle getSpatialRange() {
		return spatialRange;
	}
	public void setSpatialRange(Rectangle spatialRange) {
		this.spatialRange = spatialRange;
	}
	public TextualPredicate getTextualPredicate() {
		return textualPredicate;
	}
	public void setTextualPredicate(TextualPredicate textualPredicate) {
		this.textualPredicate = textualPredicate;
	}
	public String toString(){
		String output = "Query[: " +  getQueryId() + " , "  + "Text: "
				+ (getQueryText() == null ? "" : getQueryText().toString()) + " Location:"+spatialRange.toString()+"]";
		return output;
	}
}
