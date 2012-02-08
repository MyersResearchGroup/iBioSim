package lpn.parser;

import java.util.HashMap;

public class Vertex {
	public final Integer processID;
	private HashMap<Integer, Edge> adjacencies; 
	
	public Vertex(Integer process_id) {
		this.processID = process_id;
		adjacencies = new HashMap<Integer, Edge>();
	}

	public HashMap<Integer, Edge> getAdjacencies() {
		return this.adjacencies;
	}
	
//	public Integer getHighestEdgeWeight() {
//		for (Edge e : adjacencies.values()) {
//			
//		}
//		return null;
//	}
}
