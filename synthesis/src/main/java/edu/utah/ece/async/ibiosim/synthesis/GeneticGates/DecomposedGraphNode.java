package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;

import com.google.common.collect.Lists;


public class DecomposedGraphNode {
	private Optional<FunctionalComponent> fc;
	private Optional<ComponentDefinition> cd;
	private Optional<URI> preselectedCdUri;
	private double score; 
	Map<DecomposedGraphNode, NodeInteractionType> parentNodeList, childrenNodeList;

	public DecomposedGraphNode(){
		fc = Optional.empty();
		cd = Optional.empty();
		preselectedCdUri = Optional.empty();
		parentNodeList = new HashMap<>();
		childrenNodeList = new HashMap<>();
	}

	public DecomposedGraphNode(FunctionalComponent fc){
		this.fc = Optional.of(fc);
		this.cd = Optional.of(fc.getDefinition());
		preselectedCdUri = Optional.empty();
		parentNodeList = new HashMap<>();
		childrenNodeList = new HashMap<>();
	}
	
	public List<DecomposedGraphNode> getParentNodeList() {
		return Lists.newArrayList(this.parentNodeList.keySet());
	}

	public List<DecomposedGraphNode> getChildrenNodeList(){ this.childrenNodeList.keySet();
		return Lists.newArrayList(this.childrenNodeList.keySet());
	}
	
	public void setPreselectedComponentDefinition(URI cdUri) {
		this.preselectedCdUri = Optional.of(cdUri);
	}
	

	public void setScore(double value) {
		this.score = value;
	}
	
	public Optional<FunctionalComponent> getFunctionalComponent() {
		return this.fc;
	}
	
	public Optional<ComponentDefinition> getComponentDefinition() {
		return this.cd;
	}

	public Optional<URI> getPreselectedComponentDefinition() {
		return this.preselectedCdUri;
	}
	
	public double getScore() {
		return this.score;
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
	
	public boolean isNodePreselected() {
		return preselectedCdUri.isPresent();
	}

	@Override
	public String toString() {
		if(fc.isPresent()) {
			return fc.get().getIdentity().toString(); 
		}
		return "";
	}

	enum NodeInteractionType {
		REPRESSION,
		PRODUCTION
	}; 
}