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
package main.java.edu.utah.ece.async.lpn.parser.LpnDecomposition;

import java.util.HashMap;

import backend.verification.platu.main.Options;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Vertex {
	public final Integer componentID;
	private HashMap<Integer, Edge> adjacencies;
	private Edge edgeToMostConnectedNeighbor; 
	private int numVariables;
	private int bestNetGain;
	
	public Vertex(Integer componentID, int numVariables) {
		this.componentID = componentID;
		this.numVariables = numVariables;
		adjacencies = new HashMap<Integer, Edge>();
		bestNetGain = MinusINF;
	}

	public HashMap<Integer, Edge> getAdjacencies() {
		return this.adjacencies;
	}
	
	/**
	 * This method calculates the best net gain when coalesce this vertex with one of its neighbor vertices.
	 * The net gain is calculated as follows: net gain = (# internal variables created after coalescing) - (# remaining global variables
	 * of the target process after coalescing). The term "internal" and "global" are defined here with respect to the coalesced component. 
	 * @param maxNumVarsInOneComp 
	 * @return
	 */	
	public int calculateBestNetGain(int maxNumVarsInOneComp) {
		for (Integer i : adjacencies.keySet()){
			Edge e = adjacencies.get(i);
			Vertex targetVertex = e.target;
			int numInternal = e.getWeight(); // e is the shared edge between this vertex and the target vertex.
			if ((this.getNumVars() + targetVertex.getNumVars() - numInternal) > maxNumVarsInOneComp) {
				bestNetGain = MinusINF;
				if (Options.getDebugMode()) {
					System.out.println("^^^^^^^^^^^^^^^^^^^^^^^");
					System.out.println("Component " + this.componentID + " has " + this.getNumVars() + " variable(s).");
					System.out.println("Component " + targetVertex.componentID + " has " + targetVertex.getNumVars() + " variable(s).");			
					System.out.println("(" + this.componentID + " <- " 
							+ targetVertex.componentID + ")" + " bestNetGain = " + bestNetGain);
					System.out.println("vvvvvvvvvvvvvvvvvvvvvvv");
				}
			}
			else {
				int numRemainGlobal = targetVertex.getAllEdgesWeights() - numInternal;
				if (bestNetGain < numInternal - numRemainGlobal) {
					bestNetGain = numInternal - numRemainGlobal;
					edgeToMostConnectedNeighbor = e; 
				}
				if (Options.getDebugMode())
					System.out.println("(" + this.componentID + " <- " 
						+ targetVertex.componentID + ")" + " bestNetGain = " + bestNetGain);
			}
			
		}
		return bestNetGain;
	}
	
	private int getNumVars() {
		return numVariables;
	}
	
	public int getBestNetGain() {
		return bestNetGain;
	}
	
	private int getAllEdgesWeights() {
		int allEdgesWeights = 0;
		for (Edge e : adjacencies.values()) {
			allEdgesWeights = allEdgesWeights + e.getWeight();
		}		
		return allEdgesWeights;
	}

	public static int MinusINF = -2147483648;
	
	public Vertex getMostConnectedNeighbor() {
		return edgeToMostConnectedNeighbor.target;
	}
}
