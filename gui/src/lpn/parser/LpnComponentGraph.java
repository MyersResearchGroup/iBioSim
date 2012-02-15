package lpn.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

public class LpnComponentGraph{
	private Integer maxNumVarsInOneComp; 
	private HashMap<Integer, Vertex> vertices; 
	public LpnComponentGraph(
			HashMap<Variable, ArrayList<Integer>> sharedCompVarsMap,
			HashMap<Integer,Component> compMap, Integer maxNumProcInOneComp) {
		this.maxNumVarsInOneComp = maxNumProcInOneComp;
		vertices = new HashMap<Integer, Vertex>();
		// Create a component graph. Vertex = component ID. Weighted edge = the number of shared variables between two components.	
		for (ArrayList<Integer> componentIDList : sharedCompVarsMap.values()) {
			for (int i=0; i<componentIDList.size(); i++) {
				Vertex curVertex; 
				if (vertices.keySet().contains(componentIDList.get(i))) {
					curVertex = vertices.get(componentIDList.get(i));
				}
				else {
					curVertex = new Vertex(componentIDList.get(i), compMap.get(componentIDList.get(i)).getNumVars());
					vertices.put(componentIDList.get(i), curVertex);
				}
				for (int j=0; j<componentIDList.size(); j++) {
					Vertex nextVertex; 
					if (vertices.keySet().contains(componentIDList.get(j))) {
						nextVertex = vertices.get(componentIDList.get(j));
					}
					else {
						nextVertex = new Vertex(componentIDList.get(j), compMap.get(componentIDList.get(i)).getNumVars());
						vertices.put(componentIDList.get(j), nextVertex);
					}
					if (nextVertex.componentID != curVertex.componentID) {
						HashMap<Integer,Edge> edges = curVertex.getAdjacencies();
						// edges != null
						Edge e; 
						if (!edges.keySet().contains(nextVertex.componentID)) {
							e = new Edge(nextVertex);
							e.addWeight();
							edges.put(nextVertex.componentID, e);
						}
						else {
							e = edges.get(nextVertex.componentID);
							e.addWeight();
						}
					}
				}
			}
		}
	}
	
	public void outputDotFile(String fileName) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write("digraph G {\n");
			for (Vertex v : vertices.values()) {
				String vertexId = v.componentID.toString();
				out.write(vertexId + "[shape=\"ellipse\"" + "]\n");
				for (Edge e : v.getAdjacencies().values()) {
					String targetVertexId = e.target.componentID.toString();
					out.write( vertexId + " -> " + targetVertexId + " [label=\"" + e.getWeight() + "\"]\n");
				}
			}
			out.write("}");
			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error outputting state graph as dot file.");
		}
	}

	public Vertex selectVerticesToCoalesce() {
		// Construct a priority queue.
		VertexComparator comparator = new VertexComparator(maxNumVarsInOneComp);
		if (vertices.size() < 1) {
			return null;
		}
		else {
			PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>(vertices.size(),comparator);
			for (Vertex vertex : vertices.values()) {
				//System.out.println("adding vertex " + vertex.componentID);
				vertexQueue.add(vertex);
			}
//			printVertexQueue(vertexQueue);
			Vertex destVertex = vertexQueue.poll(); // vertexToMerge and its most connected neighbor will be coalesced into one component.
			if (destVertex.getBestNetGain() < 0) 
				return null;
			else {
				return destVertex;
			}
		}			
	}

	private void printVertexQueue(PriorityQueue<Vertex> vertexQueue) {
		System.out.println("%%%%%%%%%% vertex queue %%%%%%%%%%%");
		int i = 0;
		for (Iterator<Vertex> vertexQueueIter = vertexQueue.iterator(); vertexQueueIter.hasNext();) {
			Vertex v = vertexQueueIter.next();
			System.out.println(i + " (" + v.componentID + " <- " + v.getMostConnectedNeighbor().componentID + ")" + " best net gain: " + v.getBestNetGain());
			i++;
		}
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}
	
	
	

}
