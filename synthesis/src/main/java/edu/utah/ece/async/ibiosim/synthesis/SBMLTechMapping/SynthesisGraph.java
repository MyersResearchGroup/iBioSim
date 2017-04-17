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
package edu.utah.ece.async.ibiosim.synthesis.SBMLTechMapping;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SpeciesReference;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLFileManager;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SynthesisGraph {

	private String projectPath;
	private String modelFileID;
	private String submodelID;
	//NOTE: hold the list of value nodes that connect/point to the key node.
	private HashMap<SynthesisNode, List<SynthesisNode>> edges;  //NOTE: This is the library for the graph. This library will be initialized in constructGraph()
	private int nucleotideCount;
	private Set<String> signals;
	private Set<URI> compURIs;
	private SynthesisNode output;
	private List<SynthesisNode> inputs;
	private List<String> paths;
	
	public SynthesisGraph(BioModel biomodel, SBOLFileManager fileManager) throws SBOLException {
		Model sbmlModel = biomodel.getSBMLDocument().getModel();
		projectPath = biomodel.getPath();
		modelFileID = biomodel.getSBMLFile();
		//NOTE: create reaction graph from the sbml model and return all nodes that makes up transcription and complex formation reaction
		Set<SynthesisNode> nodes = constructGraph(sbmlModel, fileManager); 
		decomposeGraph(nodes); //TODO: decomposeGraph creates the diff gates?
		output = identifyOutput(nodes);
		paths = buildPaths(getOutput()); 
//		print();
	}
	
	private void decomposeGraph(Set<SynthesisNode> nodes) {
		Set<SynthesisNode> typedSpeciesNodes = new HashSet<SynthesisNode>();
		Set<SynthesisNode> interNodes = new HashSet<SynthesisNode>();
		/*NOTE: Go through nodes list and check if there is any node that has type set to p (promoter?). 
		*		If there is a node that has type "p", then get that node from the edges table and count the 
		*		# of repressor and activator nodes and store them into their corresponding list (typedSpeciesNodes & interNodes)
		*/		
		for (SynthesisNode node : nodes)
			if (node.getType().equals("p")) 
			{
				if (edges.containsKey(node)) 
				{
					int numActivators = 0;
					int numRepressors = 0;
					for (SynthesisNode tfNode : edges.get(node))
					{
						if (tfNode.getType().startsWith("r")) 
						{
							numRepressors++;
							typedSpeciesNodes.add(tfNode);
						} 
						else if (tfNode.getType().startsWith("a")) 
						{
							numActivators++;
							typedSpeciesNodes.add(tfNode);
						}
					} 
					if (numRepressors > 0 && numActivators == 0) 
					{
						if (numRepressors == 1) 
						{
							//NOTE: replace all existing of this node in edges table and rename its type to inverter (i)
							decomposeInverterMotif(node);
						} 
						else if (numRepressors == 2)
							//NOTE: replace all existing of this node in edges table and rename its type to nor (n)
							decomposeNorMotif(node);
					} 
					else if (numRepressors == 0 && numActivators > 0) 
					{
						if (numActivators == 1)
							decomposeYesMotif(node, interNodes); 
						else if (numActivators == 2)
							decomposeOrMotif1(node, interNodes);
					} 
				} 
				else //NOTE: if edges table does not contain the specified node.
					node.setType("c");
			} 
			//TODO: why check if these types endsWith(), startsWith(), equal()?
			else if (node.getType().endsWith("m")) 
			{
				decomposeOrMotif2(node, interNodes);
				typedSpeciesNodes.add(node);
			} 
			else if (node.getType().endsWith("x")) 
			{
				decomposeAndMotif(node, interNodes);
				typedSpeciesNodes.add(node);
			} 
			else if (node.getType().startsWith("v"))
				typedSpeciesNodes.add(node);
		//TODO: why remove all nodes from typedSpeciesNodes in edges table when they were determined from the
		//		edges table if the node type was p and then put this current typedSpeciesNode in the for loop below 
		//		back into the edges table? (look at the upper for loop code in this method)
		for (SynthesisNode typedSpeciesNode : typedSpeciesNodes) 
		{
			if (edges.containsKey(typedSpeciesNode)) 
			{
				List<SynthesisNode> inputNodes = edges.remove(typedSpeciesNode);
				typedSpeciesNode.setType("s");
				edges.put(typedSpeciesNode, inputNodes);
			} 
			else
				typedSpeciesNode.setType("s");
		}
		nodes.addAll(interNodes);
	}
	
	private void decomposeInverterMotif(SynthesisNode promoterNode) {
		//NOTE: remove all copies of the promoter node from edges table. 
		//		Set the promoter node to type i (inverter?)
		//		Add this edited version of the promoter node, now with type inverter, back into edges?
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		edges.put(promoterNode, inputSpeciesNodes);
	}
	
	private void decomposeNorMotif(SynthesisNode promoterNode) {
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("n");
		edges.put(promoterNode, inputSpeciesNodes);
	}
	
	private void decomposeYesMotif(SynthesisNode promoterNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		SynthesisNode interSpeciesNode = new SynthesisNode("s");
		nodes.add(interSpeciesNode);
		SynthesisNode interPromoterNode = new SynthesisNode("i");
		nodes.add(interPromoterNode);
		edges.put(promoterNode, new LinkedList<SynthesisNode>());
		edges.get(promoterNode).add(interSpeciesNode);
		edges.put(interSpeciesNode, new LinkedList<SynthesisNode>());
		edges.get(interSpeciesNode).add(interPromoterNode);
		edges.put(interPromoterNode, inputSpeciesNodes);
	}
	
	private void decomposeOrMotif1(SynthesisNode promoterNode, Set<SynthesisNode> nodes) 
	{
		//NOTE: remove all node with this promoterNode in edges table and set this node type to i
		List<SynthesisNode> inputSpeciesNodes = edges.remove(promoterNode);
		promoterNode.setType("i");
		SynthesisNode interSpeciesNode = new SynthesisNode("s");
		nodes.add(interSpeciesNode);
		SynthesisNode interPromoterNode = new SynthesisNode("n");
		nodes.add(interPromoterNode);
		//NOTE: put this promoterNode back into edges table as a new node key
		//		promoterNode 	  -> interSpeciesNode
		//		interSpeciesNode  -> interPromoterNode
		//		interPromoterNode -> inputSpeciesNodes (resulting graph after removal of duplicate promoter nodes)
		edges.put(promoterNode, new LinkedList<SynthesisNode>());
		edges.get(promoterNode).add(interSpeciesNode);
		edges.put(interSpeciesNode, new LinkedList<SynthesisNode>());
		edges.get(interSpeciesNode).add(interPromoterNode);
		edges.put(interPromoterNode, inputSpeciesNodes);
	}
	
	private void decomposeOrMotif2(SynthesisNode productNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputPromoterNodes = edges.get(productNode);
		List<SynthesisNode> interPromoterNodes = new LinkedList<SynthesisNode>();
		interPromoterNodes.add(new SynthesisNode("i"));
		interPromoterNodes.add(new SynthesisNode("n"));
		nodes.addAll(interPromoterNodes);
		List<SynthesisNode> interSpeciesNodes = new LinkedList<SynthesisNode>();
		for (int i = 0; i < 3; i++)
			interSpeciesNodes.add(new SynthesisNode("s")); //TODO: NOTE: add this node in 3x...why? don't care for duplicate
		nodes.addAll(interSpeciesNodes);
		edges.put(productNode, interPromoterNodes.subList(0, 1)); //NOTE: get @ index = 0 only
		edges.put(interPromoterNodes.get(0), interSpeciesNodes.subList(0, 1)); 
		edges.put(interSpeciesNodes.get(0), interPromoterNodes.subList(1, 2));
		edges.put(interPromoterNodes.get(1), interSpeciesNodes.subList(1, 3));
		for (int i = 1; i < interSpeciesNodes.size(); i++)
			edges.put(interSpeciesNodes.get(i), inputPromoterNodes.subList(i - 1, i));
	}
	
	private void decomposeAndMotif(SynthesisNode complexNode, Set<SynthesisNode> nodes) {
		List<SynthesisNode> inputSpeciesNodes = edges.get(complexNode);
		List<SynthesisNode> interPromoterNodes = new LinkedList<SynthesisNode>();
		interPromoterNodes.add(new SynthesisNode("n"));
		for (int i = 1; i < 3; i++)
			interPromoterNodes.add(i, new SynthesisNode("i"));
		nodes.addAll(interPromoterNodes);
		List<SynthesisNode> interSpeciesNodes = new LinkedList<SynthesisNode>();
		for (int i = 0; i < 2; i++)
			interSpeciesNodes.add(new SynthesisNode("s"));
		nodes.addAll(interSpeciesNodes);
		edges.put(complexNode, interPromoterNodes.subList(0, 1));
		edges.put(interPromoterNodes.get(0), interSpeciesNodes);
		for (int i = 0; i < interSpeciesNodes.size(); i++) 
			edges.put(interSpeciesNodes.get(i), interPromoterNodes.subList(i + 1, i + 2));
		for (int i = 1; i < interPromoterNodes.size(); i++) 
			edges.put(interPromoterNodes.get(i), inputSpeciesNodes.subList(i - 1, i));
	}

	private Set<SynthesisNode> constructGraph(Model sbmlModel, SBOLFileManager fileManager) throws SBOLException {
		HashMap<String, SynthesisNode> idToNode = new HashMap<String, SynthesisNode>();
		edges = new HashMap<SynthesisNode, List<SynthesisNode>>();
		nucleotideCount = 0;
		compURIs = new HashSet<URI>();
		signals = new HashSet<String>();
		for (int i = 0; i < sbmlModel.getReactionCount(); i++) 
		{
			Reaction sbmlReaction = sbmlModel.getReaction(i);
			if (sbmlReaction.getProductCount() > 0) 
				if (BioModel.isProductionReaction(sbmlReaction)) 
				{
					constructTranscriptionMotif(sbmlReaction, idToNode, sbmlModel, fileManager);
				} 
				else if ((BioModel.isComplexReaction(sbmlReaction))) 
				{
					constructComplexationMotif(sbmlReaction, idToNode, sbmlModel, fileManager);
				}
		}
		//TODO: why return the value of idToNode and ignore the key?
		//NOTE: create a table of all the values in idToNode which contains transcription and complex formation reaction
		return new HashSet<SynthesisNode>(idToNode.values());
	}
	
	private void constructTranscriptionMotif(Reaction sbmlReaction, HashMap<String, SynthesisNode> idToNode, 
			Model sbmlModel, SBOLFileManager fileManager) throws SBOLException {
		SynthesisNode promoterNode = null;
		//NOTE: go through reaction and check if there is a promoter to create a node for and add to idToNode
		for (int j = 0; j < sbmlReaction.getModifierCount(); j++) {
			ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
			if (BioModel.isPromoter(sbmlModifier))
				promoterNode = constructNode("p", sbmlReaction, idToNode,
						fileManager);
		}
		//NOTE: if there was no promoter in the reaction, then create a promoter node for this reaction
		if (promoterNode == null) 
			promoterNode = constructNode("p", sbmlReaction, idToNode, fileManager);
		//NOTE: create nodes for repressors and activators and add to idToNode and edges table
		for (int j = 0; j < sbmlReaction.getModifierCount(); j++) {
			ModifierSpeciesReference sbmlModifier = sbmlReaction.getModifier(j);
			if (BioModel.isRepressor(sbmlModifier) || BioModel.isActivator(sbmlModifier)) {
				SynthesisNode tfNode;
				if (BioModel.isRepressor(sbmlModifier))
					tfNode = constructNode("r", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
							idToNode, fileManager);
				else
					tfNode = constructNode("a", sbmlModel.getSpecies(sbmlModifier.getSpecies()), 
							idToNode, fileManager);
				//NOTE: if the promoter node created does not exist in the edges table, add the promoter node to the table
				//		as the key and add its repressor/activator nodes as its value.
				if (!edges.containsKey(promoterNode))
					edges.put(promoterNode, new LinkedList<SynthesisNode>());
				edges.get(promoterNode).add(tfNode);	
			} 
		}
		//NOTE: create promoter nodes from the reaction and add to idToNode and edges table
		for (int j = 0; j < sbmlReaction.getProductCount(); j++) {
			SpeciesReference sbmlProduct = sbmlReaction.getProduct(j);
			SynthesisNode productNode = constructNode("s", sbmlModel.getSpecies(sbmlProduct.getSpecies()), 
					idToNode, fileManager);
			//NOTE: if product node does not exist in edges table, add the product node as the key and the promoter node as its value
			if (!edges.containsKey(productNode))
				edges.put(productNode, new LinkedList<SynthesisNode>());
			edges.get(productNode).add(promoterNode);	
		}
	}
	
	private void constructComplexationMotif(Reaction sbmlReaction, HashMap<String, SynthesisNode> idToNode, 
			Model sbmlModel, SBOLFileManager fileManager) throws SBOLException {
		//TODO: complexationMotif = complex formation ?
		// 		complex formation always has only 1 product?
		//NOTE: create nodes for products and reactants and add to edges table to represent complex formation
		SpeciesReference sbmlProduct = sbmlReaction.getProduct(0);
		SynthesisNode complexNode = constructNode("x", sbmlModel.getSpecies(sbmlProduct.getSpecies()), 
				idToNode, fileManager);
		//TODO: why don't we want to check if complexNode already exist in edges before adding?
		edges.put(complexNode, new LinkedList<SynthesisNode>());
		for (int j = 0; j < sbmlReaction.getReactantCount(); j++) 
		{
			SpeciesReference sbmlReactant = sbmlReaction.getReactant(j);
			SynthesisNode speciesNode = constructNode("v", sbmlModel.getSpecies(sbmlReactant.getSpecies()),
					idToNode, fileManager);
			edges.get(complexNode).add(speciesNode);	
		}
	}
	
	private SynthesisNode constructNode(String type, SBase sbmlElement, HashMap<String, SynthesisNode> idToNode, 
			SBOLFileManager fileManager) throws SBOLException { 
		SynthesisNode node;
		//NOTE: sbmlElement - is a reaction
		//NOTE: check if table idToNode contains the reaction. 
		//		if reaction DOES exist, check if neighboring nodes (edge) are the same as the specified node. 
		//			remove the neighboring node if it is the same, append the specified type to the current node's type, 
		//			then update the edges with the specified node and the updated copy of the neighboring nodes. 
		//		if reaction DOES NOT exist in the idToNode, then create a node for it and then add to idToNode
		if (idToNode.containsKey(SBMLutilities.getId(sbmlElement))) 
		{
			node = idToNode.get(SBMLutilities.getId(sbmlElement));
			if (edges.containsKey(node)) 
			{
				List<SynthesisNode> destinationNodes = edges.remove(node);
				if (type.equals("s")) 
					node.setType(node.getType() + "m");
				else
					node.setType(type + node.getType());
				edges.put(node, destinationNodes);
			} 
			else if (!type.equals("s"))
				node.setType(node.getType() + type);
		} 
		else 
		{
			node = new SynthesisNode(type, sbmlElement, fileManager);
			nucleotideCount += node.getNucleotideCount();
			// NOTE: check if signal (DNAComponent URI) isn't empty and add to this class a copy of that signal
			if (node.getSignal().length() > 0) 
				signals.add(node.getSignal());
			//NOTE: add a copy of the component URIs to this class
			compURIs.addAll(node.getCompURIs());
			idToNode.put(SBMLutilities.getId(sbmlElement), node);
		}
		return node;
	}
	
	private SynthesisNode identifyOutput(Set<SynthesisNode> nodes) {
		HashMap<SynthesisNode, SynthesisNode> reverseEdges = new HashMap<SynthesisNode, SynthesisNode>();
		//TODO: Clarify on understanding
		//NOTE: check if edges has any of the node from set nodes and get all of its children to put into reverseEdges list.
		//		if reverseEdges does not contain a copy of its parent node, then return that node as the output node. 
		for (SynthesisNode node : nodes) 
		{
			if (edges.containsKey(node))
				for (SynthesisNode childNode : edges.get(node))
					reverseEdges.put(childNode, node);
		}
		for (SynthesisNode node : nodes) {
			if (!reverseEdges.containsKey(node))
				return node;
		}
		return null;
	}
	
	public String getProjectPath() {
		return projectPath;
	}
	
	public void setModelFileID(String modelFileID) {
		this.modelFileID = modelFileID;
	}
	
	public String getModelFileID() {
		return modelFileID;
	}
	
	public void setSubmodelID(String submodelID) {
		this.submodelID = submodelID;
	}
	
	public String getSubmodelID() {
		if (submodelID == null)
			submodelID = "";
		return submodelID;
	}
	
	public SynthesisNode getOutput() {
		if (output == null)
			output = new SynthesisNode("t");
		return output;
	}
	
	public List<SynthesisNode> getInputs() {
		return inputs;
	}
	
	public SynthesisNode getInput(int inputIndex) {
		if (inputIndex < inputs.size())
			return inputs.get(inputIndex);
		return null;
	}
	
	public List<SynthesisNode> postOrderNodes() 
	{
		List<SynthesisNode> orderedNodes = new LinkedList<SynthesisNode>();
		List<SynthesisNode> currentNodes = new LinkedList<SynthesisNode>();
		currentNodes.add(getOutput()); 
		//NOTE: get output node from the biomodel and check if edges library contains the output node. 
		//		if the output node exist in the library, get all nodes pointing to the output node and recursively add onto orderedNodes until there is none left.
		while (currentNodes.size() > 0) 
		{
			orderedNodes.add(0, currentNodes.remove(0));
			if (edges.containsKey(orderedNodes.get(0))) {
				currentNodes.addAll(edges.get(orderedNodes.get(0)));
			}
		}
		return orderedNodes;
	}

	public List<SynthesisNode> getChildren(SynthesisNode node) {
		if (edges.containsKey(node))
			return edges.get(node);
		return new LinkedList<SynthesisNode>();
	}
	
	public int getNucleotideCount() {
		return nucleotideCount;
	}
	
	public Set<String> getSignals() {
		return signals;
	}
	
	public Set<URI> getCompURIs() {
		return compURIs;
	}

	@SuppressWarnings("unused")
	private void print() {
		System.out.println(modelFileID);
		for (String path : paths)
			System.out.println(path);
		System.out.println();
	}
	
	public List<String> getPaths(SynthesisNode node) {
		return buildPaths(node);
	}
	
	public List<String> getPaths() {
		if (paths == null)
			paths = buildPaths(getOutput());
		return paths;
	}
	
	private List<String> buildPaths(SynthesisNode node) {
		List<String> paths = new LinkedList<String>();
		if (node.equals(getOutput())) 
		{
			inputs = new LinkedList<SynthesisNode>();
			buildPathsHelper(node, "t1", paths, true);
		} 
		else 
			buildPathsHelper(node, "t1", paths, false);
		return paths;
	}
	
	private void buildPathsHelper(SynthesisNode node, String path, List<String> paths, boolean identifyInputs) 
	{
		//NOTE: check edges table and see if this node exist. if it does, then get any existing children and add to the specified list of paths to indicate that it has been walked down
		//		Summary - this method will walk down the specified node and add to path list as it walks until it reaches the end of the node that no longer has any children to walk down to
		path = path + node.getType();
		if (edges.containsKey(node)) {
			int childNum = 0;
			for (SynthesisNode childNode : edges.get(node)) 
			{
				childNum++;
				buildPathsHelper(childNode, path + childNum, paths, identifyInputs);
			}
		} 
		else 
		{
			//NOTE: if not in edges table, then add to the path to indicate that it this node has been taken
			//		check if this node is to be an input. if so, then add this node to the inputs list 
			paths.add(path);
			if (identifyInputs)
				inputs.add(node);
		}
	}
	
	public List<SynthesisNode> walkPaths(SynthesisNode startNode, List<String> paths) {
		List<SynthesisNode> destinationNodes = new LinkedList<SynthesisNode>();
		for (String path : paths) {
			path = path.substring(2);
			SynthesisNode currentNode = startNode;
			boolean validPath = true;
			while (path.length() > 1 && validPath) {
				int index = Integer.parseInt(path.substring(1,2)) - 1;
				if (edges.containsKey(currentNode) && index < edges.get(currentNode).size()) {
					currentNode = edges.get(currentNode).get(index);
					path = path.substring(2);
				} else
					validPath = false;
			}
			destinationNodes.add(currentNode);
		}
		return destinationNodes;
	}
	
	@Override
	public String toString() {
		return modelFileID;
	}

}
