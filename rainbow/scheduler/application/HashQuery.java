/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rainbow.scheduler.application;

/**
 *
 * @author WesleyLuk
 */
public class HashQuery {
	
	private static int lastQueryID = 0;
	
	private String query;
	private int queryID;
	private String method;
	
	public HashQuery(String query, String method){
		this.query = query;
		this.queryID = lastQueryID;
		lastQueryID++;
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public String getQuery() {
		return query;
	}

	public int getQueryID() {
		return queryID;
	}
	
}
