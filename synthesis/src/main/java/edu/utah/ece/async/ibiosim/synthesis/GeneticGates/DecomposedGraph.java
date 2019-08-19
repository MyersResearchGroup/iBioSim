package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.sbolstandard.core2.ComponentDefinition;

import com.google.common.collect.Lists;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode.NodeInteractionType;


/**
 * Create a DecomposedGraph from an SBOL document describing a genetic circuit made of NOT and NOR logic. 
 * 
 * @author Tramy Nguyen
 *
 */
public class DecomposedGraph {


	private DecomposedGraphNode outputNode;
	private List<DecomposedGraphNode> nodeList, leafNodeList;
	private String graphId;
	
	public DecomposedGraph() {
		nodeList = new ArrayList<>();
		leafNodeList = new ArrayList<>();
	}
	
	public void setGraphId(String id) {
		this.graphId = id;
	}
	
	public String getGraphId() {
		return this.graphId == null || this.graphId.isEmpty() ? "" : this.graphId;
	}
	
	public void setNodeAsOutput(DecomposedGraphNode node) {
		this.outputNode = node;
	}
	
	public void setNodeAsLeaf(DecomposedGraphNode node) {
		if(leafNodeList.contains(node)) {
			return;
		}
		leafNodeList.add(node);
	}
	
	public List<DecomposedGraphNode> getLeafNodes(){
		return this.leafNodeList;
	}
	
	public DecomposedGraphNode getRootNode() {
		return this.outputNode;
	}

	public void addNodeRelationship(DecomposedGraphNode parent, DecomposedGraphNode child, NodeInteractionType interactionType) {
		parent.childrenNodeMap.put(child, interactionType);
		child.parentNodeMap.put(parent, interactionType);
	}
	
	public void addAllNodes(DecomposedGraphNode... nodes) {
		for(DecomposedGraphNode n : nodes) {
			addNode(n);
		}
	}
	
	public boolean isLeaf(DecomposedGraphNode node) {
		return leafNodeList.contains(node);
	}
	
	public boolean isRoot(DecomposedGraphNode node) {
		return outputNode == node ;
	}
	
	public void addNode(DecomposedGraphNode node) {
		if(!nodeList.contains(node)) { 
			nodeList.add(node);
		}
	}
	
	
	public List<DecomposedGraphNode> topologicalSort()
	{
		List<DecomposedGraphNode> sortedElements = new ArrayList<DecomposedGraphNode>();
		Queue<DecomposedGraphNode> unsortedElements = new LinkedList<DecomposedGraphNode>();
		unsortedElements.add(outputNode);

		while(!unsortedElements.isEmpty())
		{
			DecomposedGraphNode currNode = unsortedElements.poll();
			if(sortedElements.contains(currNode)) {
				continue;
			}
			
			sortedElements.add(currNode);
			for(DecomposedGraphNode childNode : currNode.childrenNodeMap.keySet()) {
				unsortedElements.add(childNode);
//				if(childNode.childrenNodeMap.size() == 1) {
//					unsortedElements.add(childNode);
//					break;
//				}
//				else if(childNode.childrenNodeMap.size() == 2){
//					List<DecomposedGraphNode> childrenNodes = Lists.newArrayList(childNode.childrenNodeMap.keySet());
//					DecomposedGraphNode child1 = childrenNodes.get(0);
//					DecomposedGraphNode child2 = childrenNodes.get(1);
//
//					if(sortedElements.containsAll(childrenNodes)) {
//						unsortedElements.add(childNode);
//					}
//					else if(childrenNodes.contains(currNode)) {
//						DecomposedGraphNode temp = null;
//						if(sortedElements.contains(child1) && !sortedElements.contains(child2)) {
//							temp = child2;
//						}
//						else if(sortedElements.contains(child2) && !sortedElements.contains(child1)){
//							temp = child1;
//						}
//						
//						if(unsortedElements.contains(temp)) {
//							sortedElements.add(temp);
//							unsortedElements.add(childNode);
//						}
//						else {
//							unsortedElements.add(childNode);
//						}
//						
//
//					}
//					else {
//						unsortedElements.add(currNode);
//					}
//				}
			} 
		} 
		Collections.reverse(sortedElements);
		return sortedElements;
	}
	
	public List<DecomposedGraphNode> getAllNodes(){
		return this.nodeList;
	}
	
	public DecomposedGraphNode getNodeByComponentDefinition(ComponentDefinition cd) {
		List<DecomposedGraphNode> nodes = new ArrayList<>();
		for(DecomposedGraphNode n : nodeList) {
			if(n.getComponentDefinition().isPresent() && n.getComponentDefinition().get().equals(cd)) {
				nodes.add(n); 
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}
	
	/**
	 * Get the DecomposedGraphNode with the given functionalComponent URI.
	 * @param functionalComponentUri
	 * @return A DecomposedGraphNode with the given URI. Otherwise, null is returned if the DecomposedGraphNode does not exist in this DecomposedGraph.
	 */
	public DecomposedGraphNode getNodeByFunctionalComponent(URI functionalComponentUri) {
		List<DecomposedGraphNode> nodes = new ArrayList<>();
		for(DecomposedGraphNode n : nodeList) {
			if(n.getFunctionalComponent().isPresent() && n.getFunctionalComponent().get().getIdentity().equals(functionalComponentUri)) {
				nodes.add(n); 
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}	
}
