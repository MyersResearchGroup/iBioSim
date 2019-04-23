package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SystemsBiologyOntology;

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
	
	public DecomposedGraph() {
		nodeList = new ArrayList<>();
		leafNodeList = new ArrayList<>();
	}
	
	public void createDecomposedGraph(ModuleDefinition md) {
		for(FunctionalComponent fc : md.getFunctionalComponents()) {
			
			DecomposedGraphNode newNode = new DecomposedGraphNode(fc.getIdentity(), fc.getDefinition());
			addNode(newNode);
			
			if(fc.getDirection().equals(DirectionType.IN)) {
				leafNodeList.add(newNode);
			}
			else if(fc.getDirection().equals(DirectionType.OUT)) {
				outputNode = newNode;
			}
		}
		for(Interaction interaction : md.getInteractions()) {
			if(interaction.containsType(SystemsBiologyOntology.INHIBITION)) {
				DecomposedGraphNode childNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.INHIBITOR);
				DecomposedGraphNode parentNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.INHIBITED);
				addNodeRelationship(parentNode, childNode, NodeInteractionType.REPRESSION);
			}
			else if(interaction.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				DecomposedGraphNode parentNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.PRODUCT);
				DecomposedGraphNode childNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.TEMPLATE);
				addNodeRelationship(parentNode, childNode, NodeInteractionType.PRODUCTION);
			}
			
		}
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
	
	public DecomposedGraphNode getOutputNode() {
		return this.outputNode;
	}

	public void addNodeRelationship(DecomposedGraphNode parent, DecomposedGraphNode child, NodeInteractionType interactionType) {
		parent.childrenNodeList.put(child, interactionType);
		child.parentNodeList.put(parent, interactionType);
	}
	
	
	public boolean isMatch(DecomposedGraph graph) {
		List<DecomposedGraphNode> topologicalSortNodes = topologicalSort();
		for(DecomposedGraphNode n : topologicalSortNodes) {
			List<DecomposedGraphNode> graphSortedNode = graph.topologicalSort();
			for(DecomposedGraphNode m : graphSortedNode) {
				if(Optional.of(n).isPresent() && Optional.of(m).isPresent()) {
					if(Optional.of(n).get().equals(Optional.of(m).get())) {
						return true;
					}
				}			
			}
		}
		return false;
	}
	
	public void addAllNodes(DecomposedGraphNode... nodes) {
		for(DecomposedGraphNode n : nodes) {
			addNode(n);
		}
	}
	
	
	public void addNode(DecomposedGraphNode node) {
		if(!nodeList.contains(node)) { 
			nodeList.add(node);
		}
	}
	
	/**
	 * Sort from bottom leaf nodes to root nodes
	 * @return Nodes sorted topological order
	 */
	public List<DecomposedGraphNode> topologicalSort()
	{
		List<DecomposedGraphNode> sortedElements = new ArrayList<DecomposedGraphNode>();
		Queue<DecomposedGraphNode> unsortedElements = new LinkedList<DecomposedGraphNode>();
		unsortedElements.addAll(leafNodeList);

		while(!unsortedElements.isEmpty())
		{
			DecomposedGraphNode currentUnsortedNode = unsortedElements.poll();
			if(sortedElements.contains(currentUnsortedNode))
				continue;
			sortedElements.add(currentUnsortedNode);
			for(DecomposedGraphNode currentUnsortedParentNode : currentUnsortedNode.parentNodeList.keySet()) {
				if(currentUnsortedParentNode.childrenNodeList.size() == 1) {
					unsortedElements.add(currentUnsortedParentNode);
					break;
				}
				else if(currentUnsortedParentNode.childrenNodeList.size() == 2){
					Set<DecomposedGraphNode> childrenNodes = currentUnsortedParentNode.childrenNodeList.keySet(); 
					if(childrenNodes.contains(currentUnsortedNode) && childrenNodes.contains(unsortedElements.peek())) {
						DecomposedGraphNode temp = unsortedElements.poll();
						sortedElements.add(temp);
						unsortedElements.add(currentUnsortedParentNode);

					}
					else if(sortedElements.containsAll(childrenNodes)) {
						unsortedElements.add(currentUnsortedParentNode);
					}
					else {
						unsortedElements.add(currentUnsortedNode);
					}
				}
			} 
		} 
		return sortedElements;
	}
	
	private DecomposedGraphNode getNode(URI uri) {
		List<DecomposedGraphNode> nodes = new ArrayList<>();
		for(DecomposedGraphNode n : nodeList) {
			if(n.fcUri.isPresent() && n.fcUri.get().equals(uri)) {
				nodes.add(n); 
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}
	
	private DecomposedGraphNode getNodeFromParticipationRole(Interaction interaction, URI participationType) {
		List<DecomposedGraphNode> nodes = new ArrayList<>();
		for(Participation participation : interaction.getParticipations()) {
			if(participation.containsRole(participationType)) {
				FunctionalComponent participant = participation.getParticipant();
				DecomposedGraphNode node = getNode(participant.getIdentity());
				nodes.add(node);
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}
}
