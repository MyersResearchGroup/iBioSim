package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sbolstandard.core2.ComponentDefinition;


public class DecomposedGraphNode {
	Optional<URI> fcUri;
	Optional<ComponentDefinition> cd;
	private double score; 
	Map<DecomposedGraphNode, NodeInteractionType> parentNodeList, childrenNodeList;

	public DecomposedGraphNode(){
		fcUri = Optional.empty();
		parentNodeList = new HashMap<>();
		childrenNodeList = new HashMap<>();
	}

	public DecomposedGraphNode(URI uri, ComponentDefinition cd){
		this.fcUri = Optional.of(uri);
		this.cd = Optional.of(cd);

		parentNodeList = new HashMap<>();
		childrenNodeList = new HashMap<>();
	}

	public Set<DecomposedGraphNode> getParentNodeList() {
		return this.parentNodeList.keySet();
	}

	public Set<DecomposedGraphNode> getChildrenNodeList(){
		return this.childrenNodeList.keySet();
	}

	public void setScore(double value) {
		this.score = value;
	}

	public double getScore() {
		return this.score;
	}

	@Override
	public String toString() {
		if(fcUri.isPresent()) {
			return fcUri.get().toString(); 
		}
		return "";
	}

	public NodeInteractionType getParentInteractionType(DecomposedGraphNode n) throws GeneticGatesException {
		if(!parentNodeList.containsKey(n)) {
			throw new GeneticGatesException("The given DecomposedGraphNode is not identified as a parent of this DecomposedGraphNode.");
		}
		return parentNodeList.get(n);
	}

	public NodeInteractionType getChildInteractionType(DecomposedGraphNode n) throws GeneticGatesException {
		if(!childrenNodeList.containsKey(n)) {
			throw new GeneticGatesException("The given DecomposedGraphNode is not identified as a child of this DecomposedGraphNode.");
		}
		return childrenNodeList.get(n);
	}

	enum NodeInteractionType {
		REPRESSION,
		PRODUCTION
	}; 
}