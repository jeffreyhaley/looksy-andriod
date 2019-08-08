package at.looksy.cache;

import java.util.Date;

public class DataCacheEntry {
	
	public DataCacheEntry(Object payload) 
	{
		this.payload = payload;
		this.creationTime = new Date();
		this.hitCount = 1;
	}
	
	private Object payload;
	private Date creationTime;
	private int hitCount;
	
	public Object getPayload() {
		return payload;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void touch() {
		this.hitCount++;
	}
	public int getHitCount() {
		return hitCount;
	}
	

}
