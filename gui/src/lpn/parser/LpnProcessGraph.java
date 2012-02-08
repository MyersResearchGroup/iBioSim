package lpn.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LpnProcessGraph{
	private Integer maxNumProcInOneComp; 
	private HashMap<Integer, Vertex> vertices; 
	private ArrayList<Edge> edges;
	public LpnProcessGraph(
			HashMap<Variable, ArrayList<LpnProcess>> sharedVarMap,
			Integer maxNumProcInOneComp) {
		this.maxNumProcInOneComp = maxNumProcInOneComp;
		vertices = new HashMap<Integer, Vertex>();
		// Create a process graph. Vertex = process ID. Weighted edge = the number of shared variables between two processes. 
		for (Iterator<ArrayList<LpnProcess>> sharedVarMapIter = sharedVarMap.values().iterator(); sharedVarMapIter.hasNext();) {
			ArrayList<LpnProcess> processList = sharedVarMapIter.next();
			for (int i=0; i<processList.size(); i++) {
				Vertex curVertex; 
				if (vertices.keySet().contains(processList.get(i).getProcessId())) {
					curVertex = vertices.get(processList.get(i).getProcessId());
				}
				else {
					curVertex = new Vertex(processList.get(i).getProcessId());
					vertices.put(processList.get(i).getProcessId(), curVertex);
				}
				for (int j=0; j<processList.size(); j++) {
					Vertex nextVertex; 
					if (vertices.keySet().contains(processList.get(j).getProcessId())) {
						nextVertex = vertices.get(processList.get(j).getProcessId());
					}
					else {
						nextVertex = new Vertex(processList.get(j).getProcessId());
						vertices.put(processList.get(j).getProcessId(), nextVertex);
					}
					if (nextVertex.processID != curVertex.processID) {
						HashMap<Integer,Edge> edges = curVertex.getAdjacencies();
						// edges != null
						Edge e; 
						if (!edges.keySet().contains(nextVertex.processID)) {
							e = new Edge(nextVertex);
							e.addWeight();
							edges.put(nextVertex.processID, e);
						}
						else {
							e = edges.get(nextVertex.processID);
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
			out.write("graph G {\n");
			for (Vertex v : vertices.values()) {
				String vertexId = v.processID.toString();
				out.write(vertexId + "[shape=\"ellipse\"" + "]\n");
				for (Edge e : v.getAdjacencies().values()) {
					String targetVertexId = e.target.processID.toString();
					out.write( vertexId + " -- " + targetVertexId + " [label=\"" + e.getWeight() + "\"]\n");
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

	public ArrayList<Component> coalesceProcesses() {
		// Construct a priority queue.
		VertexComparator comparator = new VertexComparator();
		
		for (Integer vertexId : vertices.keySet()) {
			Vertex vertex = vertices.get(vertexId);
			
		}
		return null;
	}
	
	
	

}
