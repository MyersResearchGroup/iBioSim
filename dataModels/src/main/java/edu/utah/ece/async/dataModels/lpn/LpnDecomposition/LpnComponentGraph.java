/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.dataModels.lpn.LpnDecomposition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import edu.utah.ece.async.dataModels.lpn.Variable;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class LpnComponentGraph{
	private Integer maxNumVarsInOneComp; 
	private HashMap<Integer, Vertex> vertices; 
	public LpnComponentGraph(
			HashMap<Variable, ArrayList<Integer>> sharedCompVarsMap,
			HashMap<Integer,Component> compMap, Integer maxNumProcInOneComp) {
		this.maxNumVarsInOneComp = maxNumProcInOneComp;
		vertices = new HashMap<Integer, Vertex>();
		// Create a component graph. vertex = component ID. edge weights = the number of shared variables between two components.
		if (sharedCompVarsMap.isEmpty() && compMap.size() == 1) {
			for (Iterator<Integer> compMapIter = compMap.keySet().iterator(); compMapIter.hasNext();) {
				Integer curCompId = compMapIter.next();
				Vertex curVertex = new Vertex(compMap.get(curCompId).getComponentId(), maxNumProcInOneComp);
				vertices.put(compMap.get(curCompId).getComponentId(), curVertex);
			}
		}
		else {
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
						if (i != j) {
							Vertex nextVertex; 
							if (vertices.keySet().contains(componentIDList.get(j))) {
								nextVertex = vertices.get(componentIDList.get(j));
							}
							else {
								nextVertex = new Vertex(componentIDList.get(j), compMap.get(componentIDList.get(j)).getNumVars());
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
		VertexComparator comparator = new VertexComparator(maxNumVarsInOneComp);
		if (vertices.size() < 1) {
			return null;
		}
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>(vertices.size(),comparator);
		for (Vertex vertex : vertices.values()) {
			//System.out.println("adding vertex " + vertex.componentID);
			vertexQueue.add(vertex);
		}
//			printVertexQueue(vertexQueue);
		Vertex destVertex = vertexQueue.poll(); // vertexToMerge and its most connected neighbor will be coalesced into one component.
		if (destVertex.getBestNetGain() < 0) 
			return null;
		return destVertex;			
	}

	@SuppressWarnings("unused")
	private static void printVertexQueue(PriorityQueue<Vertex> vertexQueue) {
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
