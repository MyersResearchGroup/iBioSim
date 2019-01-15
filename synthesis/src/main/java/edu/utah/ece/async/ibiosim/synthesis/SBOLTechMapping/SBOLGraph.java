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
	private Map<URI, SynthesisNode> _nodes; 
	private List<SynthesisNode> _root; 
	private int _score;
	private List<SynthesisNode> _topologicalSortNodes;

	public SBOLGraph()
	{
		_nodes = new HashMap<URI, SynthesisNode>();
		_root = new ArrayList<SynthesisNode>();
		_score = 0;
		_topologicalSortNodes = new ArrayList<SynthesisNode>();
	}

	/**
	 * Create an SBOLGraph for each SBOL ModuleDefinition where each FunctionalComponets represents the node 
	 * for the graph.
	 * @param md
	 * @throws SBOLTechMapException 
	 */
	public void createGraph(SBOLDocument sbolDoc, ModuleDefinition md) throws SBOLTechMapException
	{
		for(FunctionalComponent f : md.getFunctionalComponents())
		{
			if(!_nodes.containsKey(f.getIdentity()))
			{
				SynthesisNode node = new SynthesisNode(sbolDoc, md, f);
				_nodes.put(f.getIdentity(), node);
			}	
		}
		for(Interaction interaction : md.getInteractions())
		{
			assert(interaction.getTypes().size() == 1);
			URI interactionType = interaction.getTypes().iterator().next();
			addNodeRelationship(interactionType, interaction);
		}

		for(SynthesisNode node : _nodes.values())
		{
			if(node.isRoot())
			{
				_root.add(node);
			}
			node.setDegree(getDegree(node, node.getDegree()));
		}

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
		_score = value;
	}
	
	/**
	 * Sort SBOLGraph in toplogical order. All leaves are printed first then it's parent node.
	 */
	public void topologicalSort()
	{
		List<SynthesisNode> sortedElements = new ArrayList<SynthesisNode>();
		Queue<SynthesisNode> unsortedElements = new LinkedList<SynthesisNode>();
		unsortedElements.addAll(getRoots());

		while(!unsortedElements.isEmpty())
		{
			SynthesisNode n = unsortedElements.poll();
			if(sortedElements.contains(n))
				continue;
			sortedElements.add(n);
			for(SynthesisNode m: n.getChildren()) {
				if(m.getParents().size() == 1) {
					unsortedElements.add(m.getChildren().get(0));
					break;
				}
				else if(m.getParents().size() == 2){
					//assume 2 input into promoter
					List<SynthesisNode> parentNodes = m.getParents(); 
					if(parentNodes.contains(n) && parentNodes.contains(unsortedElements.peek())) {
						SynthesisNode temp = unsortedElements.poll();
						sortedElements.add(temp);
						unsortedElements.add(m.getChildren().get(0));

					}
					else if(sortedElements.containsAll(parentNodes)) {
						unsortedElements.add(m.getChildren().get(0));
					}
					else {
						unsortedElements.add(n);
					}
				}
			} //end of for loop
		} //end of while loop
		this._topologicalSortNodes = sortedElements;
	}
	
	/**
	 * Retrieves the root/output node of the SBOLGraph
	 * @return
	 */
	public SynthesisNode getOutputNode() {
		if(_topologicalSortNodes.size() <= 0)
			return null; 
		return _topologicalSortNodes.get(_topologicalSortNodes.size()-1);
	}

	/**
	 * Retrieve the leaf nodes of the SBOLGraph
	 * @return
	 */
	public List<SynthesisNode> getRoots() {
		return this._root;
	}

	private void addChild(URI parent, URI child) {
		SynthesisNode parentNode = _nodes.get(parent);
		SynthesisNode childNode = _nodes.get(child);
		parentNode.addChild(childNode);

	}

	private void addParent(URI parent, URI child) {
		SynthesisNode parentNode = _nodes.get(parent);
		SynthesisNode childNode = _nodes.get(child);
		childNode.addParent(parentNode);

	}

	private void addRelationship(URI parent, URI child, URI relation) {
		SynthesisNode parentNode = _nodes.get(parent);
		SynthesisNode childNode = _nodes.get(child);
		parentNode.addRelationship(childNode, relation);

	}

	/**
	 * Get all nodes within an SBOLGraph
	 * @return
	 */
	public List<SynthesisNode> getAllNodes() {
		List<SynthesisNode> nodes = new ArrayList<SynthesisNode>();
		for (Map.Entry<URI,SynthesisNode> entry : this._nodes.entrySet()) {
			nodes.add(entry.getValue());
		}
		return nodes; 
	}

	private int getDegree(SynthesisNode node, int degree) {
		if(node.isLeaf()) {
			return degree;
		}
		int max = 0; int temp;
		for(SynthesisNode child : node.getChildren()) {
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
	public List<SynthesisNode> getTopologicalSortNodes() {
		return _topologicalSortNodes;
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

			for(URI uri : _nodes.keySet()) {
				SynthesisNode node = _nodes.get(uri);
				for(SynthesisNode child : node.getChildren()) {
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
		return "SBOLGraph [Gate name = " + getOutputNode().getModuleDefinition() + "]";
	}
	
	

}
