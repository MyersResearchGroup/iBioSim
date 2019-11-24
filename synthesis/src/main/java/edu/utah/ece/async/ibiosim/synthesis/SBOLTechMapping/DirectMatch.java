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
public class DirectMatch implements Match {


	private Map<DecomposedGraphNode, LinkedList<GeneticGate>> matches;
	private DecomposedGraph specGraph;
	private List<GeneticGate> libraryGraphs;
	private boolean isInitialized;


	public DirectMatch(DecomposedGraph specGraph, List<GeneticGate> libraryGraphs) {
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
				else {
					currentSpecNode.setScore(0.0);
				}
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
			else {
				for(int i = 0; i < node.libNode.getChildrenNodeList().size(); i++) {
					if(node.specNode.getChildrenNodeList().size() != node.libNode.getChildrenNodeList().size()) {
						return false;
					}

					for(int j = 0; j < node.specNode.getChildrenNodeList().size(); j++) {
						if(!visited.contains(node.specNode.getChildrenNodeList().get(j)) &&
								node.specNode.getChildInteractionType(node.specNode.getChildrenNodeList().get(j)) == node.libNode.getChildInteractionType(node.libNode.getChildrenNodeList().get(i))) {

							visited.add(node.specNode.getChildrenNodeList().get(j));
							queue.add(new EndNode(node.specNode.getChildrenNodeList().get(j), node.libNode.getChildrenNodeList().get(i)));
						}
					}
					if(!visited.containsAll(node.specNode.getChildrenNodeList())) {
						return false;
					}

				}
			}
		}
		return true;
	}

	private void setAllGraphNodeScore(DecomposedGraph graph, Double score) {
		for(DecomposedGraphNode node : graph.topologicalSort()) {
			node.setScore(score);
		}
	}

	@Override
	public List<GeneticGate> getGateList(DecomposedGraphNode node) throws GeneticGatesException {
		/**
		 * Returns null if no gate is mapped to the given node.
		 */
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
