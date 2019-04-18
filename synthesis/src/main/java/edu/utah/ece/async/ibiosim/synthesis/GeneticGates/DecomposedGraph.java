package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SystemsBiologyOntology;


/**
 * Create a DecomposedGraph from an SBOL document describing a genetic circuit made of NOT and NOR logic. 
 * 
 * @author Tramy Nguyen
 *
 */
public class DecomposedGraph {


	private Node outputNode;
	private List<Node> nodeList, leafNodeList;
	
	public DecomposedGraph() {
		nodeList = new ArrayList<>();
		leafNodeList = new ArrayList<>();
	}
	
	public void createDecomposedGraph(ModuleDefinition md) {
		for(FunctionalComponent fc : md.getFunctionalComponents()) {
			
			Node newNode = new Node(fc.getIdentity(), fc.getDefinition());
			addNode(newNode);
			
			if(fc.getDirection().equals(DirectionType.IN)) {
				leafNodeList.add(newNode);
			}
			else if(fc.getDirection().equals(DirectionType.OUT)) {
				outputNode = newNode;
			}
		}
		for(Interaction interaction : md.getInteractions()) {
			Node parentNode = null;
			Node childNode =  null;
			if(interaction.containsType(SystemsBiologyOntology.INHIBITION)) {
				childNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.INHIBITOR);
				parentNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.INHIBITED);
			}
			else if(interaction.containsType(SystemsBiologyOntology.GENETIC_PRODUCTION)) {
				parentNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.PRODUCT);
				childNode = getNodeFromParticipationRole(interaction, SystemsBiologyOntology.TEMPLATE);

			}
			assert(parentNode != null && childNode != null);
			addNodeRelationship(parentNode, childNode);
		}
	}
	
	public void setNodeAsOutput(Node node) {
		this.outputNode = node;
	}
	
	public void setNodeAsLeaf(Node node) {
		if(leafNodeList.contains(node)) {
			return;
		}
		leafNodeList.add(node);
	}
	
	public List<Node> getLeafNodes(){
		return this.leafNodeList;
	}
	
	public Node getOutputNode() {
		return this.outputNode;
	}

	public void addNodeRelationship(Node parent, Node child) {
		parent.childrenNodeList.add(child);
		child.parentNodeList.add(parent);
	}
	
	
	public boolean isMatch(DecomposedGraph graph) {
		List<Node> topologicalSortNodes = topologicalSort();
		for(Node n : topologicalSortNodes) {
			List<Node> graphSortedNode = graph.topologicalSort();
			for(Node m : graphSortedNode) {
				if(Optional.of(n).isPresent() && Optional.of(m).isPresent()) {
					if(Optional.of(n).get().equals(Optional.of(m).get())) {
						return true;
					}
				}			
			}
		}
		return false;
	}
	
	private Node getNodeFromParticipationRole(Interaction interaction, URI participationType) {
		List<Node> nodes = new ArrayList<>();
		for(Participation participation : interaction.getParticipations()) {
			if(participation.containsRole(participationType)) {
				FunctionalComponent participant = participation.getParticipant();
				Node node = getNode(participant.getIdentity());
				nodes.add(node);
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}
	
	public void addAllNodes(Node... nodes) {
		for(Node n : nodes) {
			addNode(n);
		}
	}
	
	
	public void addNode(Node node) {
		if(!nodeList.contains(node)) { 
			nodeList.add(node);
		}
	}
	
	/**
	 * Sort from bottom leaf nodes to root nodes
	 * @return
	 */
	public List<Node> topologicalSort()
	{
		List<Node> sortedElements = new ArrayList<Node>();
		Queue<Node> unsortedElements = new LinkedList<Node>();
		unsortedElements.addAll(leafNodeList);

		while(!unsortedElements.isEmpty())
		{
			Node currentUnsortedNode = unsortedElements.poll();
			if(sortedElements.contains(currentUnsortedNode))
				continue;
			sortedElements.add(currentUnsortedNode);
			for(Node currentUnsortedParentNode : currentUnsortedNode.parentNodeList) {
				if(currentUnsortedParentNode.childrenNodeList.size() == 1) {
					unsortedElements.add(currentUnsortedParentNode);
					break;
				}
				else if(currentUnsortedParentNode.childrenNodeList.size() == 2){
					List<Node> childrenNodes = currentUnsortedParentNode.childrenNodeList; 
					if(childrenNodes.contains(currentUnsortedNode) && childrenNodes.contains(unsortedElements.peek())) {
						Node temp = unsortedElements.poll();
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
	
	private Node getNode(URI uri) {
		List<Node> nodes = new ArrayList<>();
		for(Node n : nodeList) {
			if(n.uri.isPresent() && n.uri.get().equals(uri)) {
				nodes.add(n); 
			}
		}
		assert(nodes.size() == 1);
		return nodes.get(0);
	}
	
	
	
	public static class Node {
		Optional<URI> uri;
		Optional<ComponentDefinition> cd;
		double score; 
		List<Node> parentNodeList, childrenNodeList;
		
		public Node(){
			uri = Optional.empty();
			parentNodeList = new ArrayList<>();
			childrenNodeList = new ArrayList<>();
		}
		
		public Node(URI uri, ComponentDefinition cd){
			this.uri = Optional.of(uri);
			this.cd = Optional.of(cd);
			
			parentNodeList = new ArrayList<>();
			childrenNodeList = new ArrayList<>();
		}
		
		public List<Node> getParentNodeList() {
			return this.parentNodeList;
		}
		
		public List<Node> getChildrenNodeList(){
			return this.childrenNodeList;
		}
		
		public void setScore(double value) {
			this.score = value;
		}
		
		public double getScore() {
			return this.score;
		}
	}
}
