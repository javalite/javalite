package org.javalite.activejdbc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author amarkhel
 *
 * This class is used for tracking select query statistics(hits and misses) attached to Connection, but only if connectionCache is enabled.
 * Currently, I create it for testing purposes, but maybe can be used in user apps? Need to think.
 */
public class ConnectionStatistics {

	private Map<String, Status> stats = new HashMap<String, Status>();
	
	/**
	 * 
	 * @return ConnectionStatistics - snapshot of statistics
	 */
	public ConnectionStatistics snapshot(){
		ConnectionStatistics info = new ConnectionStatistics();
		info.stats = Collections.unmodifiableMap(stats);
		return info;
	}
	
	/**
	 * Returns general count of select queries for specified queryString
	 * @param query - specified query
	 * @return count of selects
	 */
	public int getCount(String query){
		return stats.get(query) != null ? stats.get(query).getCount() : 0;
	}
	
	/**
	 * Returns count of hits of select queries for specified queryString
	 * @param query - specified query
	 * @return count of hits
	 */
	public int getHits(String query){
		return stats.get(query) != null ? stats.get(query).getHits() : 0;
	}
	
	/**
	 * Returns count of misses of select queries for specified queryString
	 * @param query - specified query
	 * @return count of misses
	 */
	public int getMiss(String query){
		return stats.get(query) != null ? stats.get(query).getMiss() : 0;
	}
	
	/**
	 * Used to get count of ALL select queries during lifetime of connection
	 * @return count of ALL queries
	 */
	public int getCountQueries(){
		int accumulator = 0;
		for(String query : stats.keySet()){
			accumulator += stats.get(query).getCount();
		}
		return accumulator;
	}
	
	/**
	 * Used to get count of misses of ALL select queries during lifetime of connection
	 * @return All misses count
	 */
	public int getCountMisses(){
		int accumulator = 0;
		for(String query : stats.keySet()){
			accumulator += stats.get(query).getMiss();
		}
		return accumulator;
	}
	
	/**
	 * Used to get count of hits of ALL select queries during lifetime of connection
	 * @return All hits count
	 */
	public int getCountHits(){
		int accumulator = 0;
		for(String query : stats.keySet()){
			accumulator += stats.get(query).getHits();
		}
		return accumulator;
	}

	/**
	 * Increment hit value for given queryString
	 * @param query
	 */
	public void hit(String query){
		Status queryStatus = stats.get(query);
		if(queryStatus == null){
			queryStatus = new Status();
		}
		queryStatus.incrementHits();
		stats.put(query, queryStatus);
	}
	
	/**
	 * Increment miss value for given queryString
	 * @param query
	 */
	public void miss(String query){
		Status queryStatus = stats.get(query);
		if(queryStatus == null){
			queryStatus = new Status();
		}
		queryStatus.incrementMiss();
		stats.put(query, queryStatus);
	}
	
	private static class Status{
		private int count = 0;
		private int hits = 0;
		public int getCount() {
			return count;
		}
		public int getMiss() {
			return count - hits;
		}
		public int getHits() {
			return hits;
		}
		public void incrementMiss(){
			count++;
		}
		public void incrementHits(){
			count++;
			hits++;
		}
	}
}
