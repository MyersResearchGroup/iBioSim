package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGateScoreComparator;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class PreSelectedMatch implements Match {


	private Map<DecomposedGraphNode, LinkedList<GeneticGate>> matches;
	private DecomposedGraph specGraph;
	private List<GeneticGate> libraryGraphs;
	private boolean isInitialized;

	public PreSelectedMatch(DecomposedGraph specGraph, List<GeneticGate> libraryGraphs) {
		this.specGraph = specGraph;
		this.libraryGraphs = libraryGraphs;
		this.matches = new HashMap<>();
	}

	public void initialize() throws GeneticGatesException {
		// map spec. node to possible matching library gates
		setAllGraphNodeScore(specGraph, Double.POSITIVE_INFINITY);
		List<DecomposedGraphNode> specNodeList = specGraph.topologicalSort();

		for(DecomposedGraphNode currentSpecNode: specNodeList)
		{
			double totalScore;
			for(GeneticGate gate : libraryGraphs) {
				DecomposedGraph decomposedGate = gate.getDecomposedGraph();
				DecomposedGraphNode gateOuputNode = decomposedGate.getRootNode();
				if(isMatch(currentSpecNode, gateOuputNode)) {
					double tempSpecScore = currentSpecNode.getScore();
					if(currentSpecNode.getScore() == Double.POSITIVE_INFINITY) {
						tempSpecScore = 0.0;
					}
					totalScore = gateOuputNode.getScore() + tempSpecScore;
					if(totalScore < currentSpecNode.getScore()) {
						//a lower score was found, add gate to the matched spec. node
						currentSpecNode.setScore(totalScore); //update speciGraph with new libGate score

						if(!matches.containsKey(currentSpecNode)) {
							matches.put(currentSpecNode, new LinkedList<GeneticGate>());
						}
						matches.get(currentSpecNode).addFirst(gate);
					}
					else {
						if(!matches.containsKey(currentSpecNode)) {
							matches.put(currentSpecNode, new LinkedList<GeneticGate>());
							matches.get(currentSpecNode).add(gate);
						}
						else {
							LinkedList<GeneticGate> matchedGatelist = matches.get(currentSpecNode);
							matchedGatelist.add(gate);
						}
					}
				}
			}
			if(!matches.containsKey(currentSpecNode)) {
				currentSpecNode.setScore(0.0);
			}
		}
		isInitialized = true;
	}

	private boolean isMatch(DecomposedGraphNode specNode, DecomposedGraphNode libNode) throws GeneticGatesException {

		HashSet<DecomposedGraphNode> visited = new HashSet<>();
		Queue<EndNode> queue = new LinkedList<>();
		queue.add(new EndNode(specNode, libNode));


		while(!queue.isEmpty()) {
			EndNode node = queue.poll();

			if(node.libNode.getChildrenNodeList().size() == 0) {
				break;
			}
			else if(node.specNode.getChildrenNodeList().size() != node.libNode.getChildrenNodeList().size()) {
				return false;
			}
			else {
				if(!node.libNode.getComponentDefinition().isPresent()) {
					return false;
				}
				if(node.specNode.isNodePreselected()) {
					if(!node.specNode.getPreselectedComponentDefinition().get().equals(node.libNode.getComponentDefinition().get().getIdentity())) {
						return false;
					}
				}
				if(node.libNode.getChildrenNodeList().size() == 1 && is1InputInteractionMatch(node)){
					DecomposedGraphNode libChild = node.libNode.getChildrenNodeList().get(0);
					DecomposedGraphNode specChild = node.specNode.getChildrenNodeList().get(0);
					if(specChild.isNodePreselected() && libChild.getComponentDefinition().isPresent()) {
						if(specChild.getPreselectedComponentDefinition().get().equals(libChild.getComponentDefinition().get().getIdentity())) {
							if(!visited.contains(specChild)) 
							{
								visited.add(specChild);
								queue.add(new EndNode(specChild, libChild));
							}
						}
					}
					else {
						if(!visited.contains(specChild)) {
							visited.add(specChild);
							queue.add(new EndNode(specChild, libChild));
						}
					}
					
				}
				else if(node.libNode.getChildrenNodeList().size() == 2 && is2InputInteractionMatch(node)) {
					DecomposedGraphNode libChild1 = node.libNode.getChildrenNodeList().get(0);
					DecomposedGraphNode libChild2 = node.libNode.getChildrenNodeList().get(1);
					DecomposedGraphNode specChild1 = node.specNode.getChildrenNodeList().get(0);
					DecomposedGraphNode specChild2 = node.specNode.getChildrenNodeList().get(1);

					if(specChild1.isNodePreselected() && !specChild2.isNodePreselected()) {
						if(libChild1.getComponentDefinition().isPresent() && specChild1.getPreselectedComponentDefinition().get().equals(libChild1.getComponentDefinition().get().getIdentity())) {
							if(!visited.contains(specChild1)) {
								visited.add(specChild1);
								queue.add(new EndNode(specChild1, libChild1));
							}
							
							if(!visited.contains(specChild2)) {
								visited.add(specChild2);
								queue.add(new EndNode(specChild2, libChild2));
							}
						}
						else if(libChild2.getComponentDefinition().isPresent() && specChild1.getPreselectedComponentDefinition().get().equals(libChild2.getComponentDefinition().get().getIdentity())) {
							if(!visited.contains(specChild1)) {
								visited.add(specChild1);
								queue.add(new EndNode(specChild1, libChild2));
							}
							
							if(!visited.contains(specChild2)) {
								visited.add(specChild2);
								queue.add(new EndNode(specChild2, libChild1));
							}
						}
					}
					else if(!specChild1.isNodePreselected() && specChild2.isNodePreselected()) {
						if(libChild1.getComponentDefinition().isPresent() && specChild2.getPreselectedComponentDefinition().get().equals(libChild1.getComponentDefinition().get().getIdentity())) {
							if(!visited.contains(specChild1)) {
								visited.add(specChild1);
								queue.add(new EndNode(specChild1, libChild2));
							}
							
							if(!visited.contains(specChild2)) {
								visited.add(specChild2);
								queue.add(new EndNode(specChild2, libChild1));
							}
						}
						else if(libChild2.getComponentDefinition().isPresent() && specChild2.getPreselectedComponentDefinition().get().equals(libChild2.getComponentDefinition().get().getIdentity())) {
							if(!visited.contains(specChild1)) {
								visited.add(specChild1);
								queue.add(new EndNode(specChild1, libChild1));
							}
							
							if(!visited.contains(specChild2)) {
								visited.add(specChild2);
								queue.add(new EndNode(specChild2, libChild2));
							}
						}
					}
					else if(specChild1.isNodePreselected() && specChild2.isNodePreselected()) {
						if(libChild1.getComponentDefinition().isPresent() && libChild2.getComponentDefinition().isPresent() 
							&& specChild1.getPreselectedComponentDefinition().get().equals(libChild1.getComponentDefinition().get().getIdentity()) 
							&& specChild2.getPreselectedComponentDefinition().get().equals(libChild2.getComponentDefinition().get().getIdentity())){
							if(!visited.contains(specChild1)) {
								visited.add(specChild1);
								queue.add(new EndNode(specChild1, libChild1));
							}
							
							if(!visited.contains(specChild2)) {
								visited.add(specChild2);
								queue.add(new EndNode(specChild2, libChild2));
							}
						}
						else if(libChild1.getComponentDefinition().isPresent() && libChild2.getComponentDefinition().isPresent() 
								&& specChild1.getPreselectedComponentDefinition().get().equals(libChild2.getComponentDefinition().get().getIdentity())
								&& specChild2.getPreselectedComponentDefinition().get().equals(libChild1.getComponentDefinition().get().getIdentity())) {
							if(!visited.contains(specChild1)) {
								visited.add(specChild1);
								queue.add(new EndNode(specChild1, libChild2));
							}
							
							if(!visited.contains(specChild2)) {
								visited.add(specChild2);
								queue.add(new EndNode(specChild2, libChild1));
							}
						}
					}
					else {
						if(!visited.contains(specChild1)) {
							visited.add(specChild1);
							queue.add(new EndNode(specChild1, libChild1));
						}
						
						if(!visited.contains(specChild2)) {
							visited.add(specChild2);
							queue.add(new EndNode(specChild2, libChild2));
						}
					}

				
				}

				if(!visited.containsAll(node.specNode.getChildrenNodeList())) {
					return false;
				}
			}

		}
		return true;
	}
	
	private boolean is1InputInteractionMatch(EndNode node) throws GeneticGatesException {
		DecomposedGraphNode libParent = node.libNode;
		DecomposedGraphNode libChild1 = node.libNode.getChildrenNodeList().get(0);
		DecomposedGraphNode specParent = node.specNode;
		DecomposedGraphNode specChild1 = node.specNode.getChildrenNodeList().get(0);
		return specParent.getChildInteractionType(specChild1) == libParent.getChildInteractionType(libChild1);
	}

	private boolean is2InputInteractionMatch(EndNode node) throws GeneticGatesException {
		DecomposedGraphNode libParent = node.libNode;
		DecomposedGraphNode libChild1 = node.libNode.getChildrenNodeList().get(0);
		DecomposedGraphNode libChild2 = node.libNode.getChildrenNodeList().get(1);
		DecomposedGraphNode specParent = node.specNode;
		DecomposedGraphNode specChild1 = node.specNode.getChildrenNodeList().get(0);
		DecomposedGraphNode specChild2 = node.specNode.getChildrenNodeList().get(1);

		boolean pattern1 = specParent.getChildInteractionType(specChild1) == libParent.getChildInteractionType(libChild1)
				&& specParent.getChildInteractionType(specChild2) == libParent.getChildInteractionType(libChild2);
		boolean pattern2 = specParent.getChildInteractionType(specChild1) == libParent.getChildInteractionType(libChild2)
				&& specParent.getChildInteractionType(specChild2) == libParent.getChildInteractionType(libChild1);
		return pattern1 || pattern2;
	}

	private void setAllGraphNodeScore(DecomposedGraph graph, Double score) {
		for(DecomposedGraphNode node : graph.topologicalSort()) {
			node.setScore(score);
		}
	}

	@Override
	public List<GeneticGate> getGateList(DecomposedGraphNode node) throws GeneticGatesException {
		if(!isInitialized) {
			initialize();
		}
		return matches.get(node);
	}

	@Override
	public DecomposedGraph getSpecification() {
		return specGraph;
	}

	@Override
	public List<GeneticGate> getLibrary() {
		return this.libraryGraphs;
	}

	@Override
	public void sortAscendingOrder(List<GeneticGate> library) {
		library.sort(new GeneticGateScoreComparator());
	}
}
