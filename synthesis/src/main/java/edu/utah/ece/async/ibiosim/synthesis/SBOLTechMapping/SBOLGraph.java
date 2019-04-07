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
package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SystemsBiologyOntology;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLGraph
{
	private Map<URI, FunctionalComponentNode> fcNodes; 
	private List<FunctionalComponentNode> inputNodes; 
	private int score;
	private List<FunctionalComponentNode> topologicalSortNodes;
	private Map<FunctionalComponent, FunctionalComponentNode> mappedNode;
	private ModuleDefinition md;

	public SBOLGraph()
	{
		fcNodes = new HashMap<URI, FunctionalComponentNode>();
		inputNodes = new ArrayList<FunctionalComponentNode>();
		mappedNode = new HashMap<FunctionalComponent, FunctionalComponentNode>();
		score = 0;
		topologicalSortNodes = new ArrayList<FunctionalComponentNode>();
	}

	/**
	 * Create an SBOLGraph for each SBOL ModuleDefinition where each FunctionalComponets represents the node 
	 * for the graph.
	 * @param md
	 * @throws SBOLTechMapException 
	 */
	public void initializeGraph(SBOLDocument sbolDoc, ModuleDefinition md) throws SBOLTechMapException
	{
		this.md = md;
		for(FunctionalComponent f : md.getFunctionalComponents())
		{
			if(!fcNodes.containsKey(f.getIdentity()))
			{
				FunctionalComponentNode node = new FunctionalComponentNode(sbolDoc, f);
				node.addContext(md);
				fcNodes.put(f.getIdentity(), node);
			}	
		}
		for(Interaction interaction : md.getInteractions())
		{
			assert(interaction.getTypes().size() == 1);
			URI interactionType = interaction.getTypes().iterator().next();
			addNodeRelationship(interactionType, interaction);
		}

		for(FunctionalComponentNode node : fcNodes.values())
		{
			if(node.isRoot(md))
			{
				inputNodes.add(node);
			}
			node.setDegree(md, getDegree(node, node.getDegree(md)));
		}

	}
	
	void addFunctionalComponentNode(FunctionalComponentNode fcNode, URI fcIdentity) {
		this.fcNodes.put(fcIdentity, fcNode);
	}
	
	private void addNodeRelationship(URI interactionType, Interaction interaction) throws SBOLTechMapException {
		List<URI> tf = new ArrayList<URI>();
		URI transcriptionalUnit = null; 
		for(Participation participation : interaction.getParticipations()) {
			URI role = participation.getRoles().iterator().next();
			if(isTranscriptionalUnitNode(role)) {
				transcriptionalUnit = participation.getParticipantURI();
			}
			else {
				tf.add(participation.getParticipantURI());
			}
		}
		
		setNodeRelationship(tf, transcriptionalUnit, interactionType);
	}
	
	private void setNodeRelationship(List<URI> transcriptionFactors, URI transcriptionalUnit, URI interactionType) throws SBOLTechMapException
	{
		for(URI tf : transcriptionFactors)
		{
			URI parentNode = null;
			URI childNode = null;
			if(interactionType.equals(SystemsBiologyOntology.INHIBITION) || interactionType.equals(SystemsBiologyOntology.STIMULATION)) {
				parentNode = tf;
				childNode = transcriptionalUnit;
			}
			else if(interactionType.equals(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				parentNode = transcriptionalUnit;
				childNode = tf;
			}
			else {
				throw new SBOLTechMapException("Unidentified Interaction occurred: " + interactionType);
			}
			addChild(parentNode, childNode);
			addParent(parentNode, childNode);
			addRelationship(parentNode, childNode, interactionType);
		}
	}
	
	private boolean isTranscriptionalUnitNode(URI nodeRole) {
		return nodeRole.equals(SystemsBiologyOntology.INHIBITED) || nodeRole.equals(SystemsBiologyOntology.TEMPLATE);
	}
	
	public void setScore(int value)
	{
		score = value;
	}
	
	/**
	 * Sort SBOLGraph in toplogical order. All leaves are printed first then it's parent node.
	 */
	public void topologicalSort()
	{
		List<FunctionalComponentNode> sortedElements = new ArrayList<FunctionalComponentNode>();
		Queue<FunctionalComponentNode> unsortedElements = new LinkedList<FunctionalComponentNode>();
		unsortedElements.addAll(getRoots());

		while(!unsortedElements.isEmpty())
		{
			FunctionalComponentNode n = unsortedElements.poll();
			if(sortedElements.contains(n))
				continue;
			sortedElements.add(n);
			for(FunctionalComponentNode m: n.getChildren(md)) {
				if(m.getParents(md).size() == 1) {
					unsortedElements.add(m.getChildren(md).get(0));
					break;
				}
				else if(m.getParents(md).size() == 2){
					//assume 2 input into promoter
					List<FunctionalComponentNode> parentNodes = m.getParents(md); 
					if(parentNodes.contains(n) && parentNodes.contains(unsortedElements.peek())) {
						FunctionalComponentNode temp = unsortedElements.poll();
						sortedElements.add(temp);
						unsortedElements.add(m.getChildren(md).get(0));

					}
					else if(sortedElements.containsAll(parentNodes)) {
						unsortedElements.add(m.getChildren(md).get(0));
					}
					else {
						unsortedElements.add(n);
					}
				}
			} //end of for loop
		} //end of while loop
		this.topologicalSortNodes = sortedElements;
	}
	
	public ModuleDefinition getModuleDefinition() {
		return this.md;
	}
	
	/**
	 * Retrieves the root/output node of the SBOLGraph
	 * @return
	 */
	public FunctionalComponentNode getOutputNode() {
		if(topologicalSortNodes.size() <= 0)
			return null; 
		return topologicalSortNodes.get(topologicalSortNodes.size()-1);
	}

	/**
	 * Retrieve the leaf nodes of the SBOLGraph
	 * @return
	 */
	public List<FunctionalComponentNode> getRoots() {
		return this.inputNodes;
	}

	private void addChild(URI parent, URI child) {
		FunctionalComponentNode parentNode = fcNodes.get(parent);
		FunctionalComponentNode childNode = fcNodes.get(child);
		parentNode.addChild(md, childNode);

	}

	private void addParent(URI parent, URI child) {
		FunctionalComponentNode parentNode = fcNodes.get(parent);
		FunctionalComponentNode childNode = fcNodes.get(child);
		childNode.addParent(md, parentNode);

	}

	private void addRelationship(URI parent, URI child, URI relation) {
		FunctionalComponentNode parentNode = fcNodes.get(parent);
		FunctionalComponentNode childNode = fcNodes.get(child);
		parentNode.addRelationship(md, childNode, relation);

	}

	/**
	 * Get all nodes within an SBOLGraph
	 * @return
	 */
	public List<FunctionalComponentNode> getAllNodes() {
		List<FunctionalComponentNode> nodes = new ArrayList<FunctionalComponentNode>();
		for (Map.Entry<URI,FunctionalComponentNode> entry : this.fcNodes.entrySet()) {
			nodes.add(entry.getValue());
		}
		return nodes; 
	}

	private int getDegree(FunctionalComponentNode node, int degree) {
		if(node.isLeaf(md)) {
			return degree;
		}
		int max = 0; int temp;
		for(FunctionalComponentNode child : node.getChildren(md)) {
			temp = getDegree(child, degree);
			if(temp > max) {
				max = temp;
			}
		}
		return max + 1;
	}
	
	/**
	 * Retrieve a list of nodes sorted from calling toplogical sort on an SBOLGraph
	 * @return - The sorted species nodes of the SBOLGraph after calling topological sort
	 */
	public List<FunctionalComponentNode> getTopologicalSortNodes() {
		return topologicalSortNodes;
	}
	
	/**
	 * Generate a DOT file for the SBOLGraph
	 * @param filename - Name of DOT file to be produced
	 */
	public void createDotFile(String filename) {
		BufferedWriter output = null;
		try {
			File file = new File(filename + ".dot");
			output = new BufferedWriter(new FileWriter(file));
			output.write("digraph G {");

			for(URI uri : fcNodes.keySet()) {
				FunctionalComponentNode node = fcNodes.get(uri);
				for(FunctionalComponentNode child : node.getChildren(md)) {
					String parentId = node.toString();
					String childId = child.toString();
					output.write(parentId + " -> " + childId + ";");
				}
			}
			output.write("}");
		} 
		catch ( IOException e ) {
			e.printStackTrace();
		} 
		finally {
			if (output != null ) {
				try {
					output.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public String toString()
	{
		return "SBOLGraph [Gate name = " + this.md + "]";
	}
	
	

}
