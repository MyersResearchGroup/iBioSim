package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.sbolstandard.core2.ComponentDefinition;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.WiredORGate;

/**
 * A class holding different covering algorithms for the Technology Mapping procedure. 
 * @author Tramy Nguyen
 *
 */
public class Cover {

	private Match matches;

	public Cover(Match matches) throws SBOLTechMapException {
		this.matches = matches;
	}

	public List<TechMapSolution> exhaustiveCover() throws GeneticGatesException {
		return cover_(Integer.MAX_VALUE, false);
	}

	private List<TechMapSolution> cover_(int numOfSol, boolean sortMatches) throws GeneticGatesException {
		Queue<TechMapSolution> queueOfSol = new LinkedList<>();
		List<TechMapSolution> listOfSolutions = new ArrayList<>();
		DecomposedGraph specGraph  = matches.getSpecification();
		TechMapSolution currentSolution = new TechMapSolution();
		currentSolution.setScore(0);
		currentSolution.addUnmappedNode(matches.getSpecification().getRootNode());
		queueOfSol.add(currentSolution);
		while(!queueOfSol.isEmpty()) {
			currentSolution = queueOfSol.poll();
			while(currentSolution.hasUnmappedNode()) {
				DecomposedGraphNode specNode = currentSolution.getNextUnmappedNode();

				List<GeneticGate> gateList = matches.getGateList(specNode);
				if(gateList == null) {
					continue;
				}
				
				if(sortMatches) {
					matches.sortAscendingOrder(gateList);
				}
				for(GeneticGate gate : gateList) {
					DecomposedGraph decomposedGate = gate.getDecomposedGraph();

					if(currentSolution.getAssignedComponent(specNode) != null && !currentSolution.getAssignedComponent(specNode).getIdentity().equals(gate.getListOfOutputsAsComponentDefinition().get(0).getIdentity())) {
						continue;
					}
					TechMapSolution newSolution = new TechMapSolution(currentSolution);
					if(specGraph.isRoot(specNode)) {
						newSolution.assignComponentToNode(specNode, gate.getListOfOutputsAsComponentDefinition().get(0));
					}
					List<DecomposedGraphNode> nextSpecNode = EndNode.getMatchingEndNodes(specNode, decomposedGate.getRootNode());

					if(nextSpecNode.size() == 1) {
						if(!newSolution.isNodeMapped(nextSpecNode.get(0))) {
							newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
							newSolution.assignGateToNode(specNode, gate);
							newSolution.assignComponentToNode(nextSpecNode.get(0), decomposedGate.getLeafNodes().get(0).getComponentDefinition().get());
							newSolution.addUnmappedNode(nextSpecNode.get(0));
							queueOfSol.add(newSolution);
						}
						else {
							ComponentDefinition specCd = newSolution.getAssignedComponent(nextSpecNode.get(0));
							ComponentDefinition gateCd = gate.getListOfInputsAsComponentDefinition().get(0);
							if(specCd.getIdentity().equals(gateCd.getIdentity())) {
								newSolution.assignGateToNode(specNode, gate);
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
							}
						}
					}
					else if(nextSpecNode.size() == 2) {
						DecomposedGraphNode node1 = nextSpecNode.get(0);
						DecomposedGraphNode node2 = nextSpecNode.get(1);
						ComponentDefinition gateCd1 = gate.getListOfInputsAsComponentDefinition().get(0);
						ComponentDefinition gateCd2 = gate.getListOfInputsAsComponentDefinition().get(1);

						if(!newSolution.isNodeMapped(node1) && newSolution.isNodeMapped(node2)) {
							if(newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd1.getIdentity())) {
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
								newSolution.assignGateToNode(specNode, gate);
								newSolution.assignComponentToNode(node1, gateCd2);
								newSolution.addUnmappedNode(node1);
								queueOfSol.add(newSolution);
							}
							else if(newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd2.getIdentity())) {
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
								newSolution.assignGateToNode(specNode, gate);
								newSolution.assignComponentToNode(node1, gateCd1);
								newSolution.addUnmappedNode(node1);
								queueOfSol.add(newSolution);
							}
						}
						else if(newSolution.isNodeMapped(node1) && !newSolution.isNodeMapped(node2)) {
							if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd1.getIdentity())) {
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
								newSolution.assignGateToNode(specNode, gate);
								newSolution.assignComponentToNode(node2, gateCd2);
								newSolution.addUnmappedNode(node2);
								queueOfSol.add(newSolution);
							}
							else if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd2.getIdentity())) {
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
								newSolution.assignGateToNode(specNode, gate);
								newSolution.assignComponentToNode(node2, gateCd1);
								newSolution.addUnmappedNode(node2);
								queueOfSol.add(newSolution);
							}
						}
						else if(newSolution.isNodeMapped(node1) && newSolution.isNodeMapped(node2)) {
							if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd1.getIdentity()) && 
									newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd2.getIdentity())){
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
								newSolution.assignGateToNode(specNode, gate);
							}
							else if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd2.getIdentity()) && 
									newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd1.getIdentity())) {
								newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
								newSolution.assignGateToNode(specNode, gate);
							}
						}
						else {
							TechMapSolution solution1 = new TechMapSolution(newSolution);
							solution1.incrementScoreBy(decomposedGate.getRootNode().getScore());
							solution1.assignGateToNode(specNode, gate);
							solution1.assignComponentToNode(node1, gateCd1);
							solution1.assignComponentToNode(node2, gateCd2);
							solution1.addUnmappedNode(node1);
							solution1.addUnmappedNode(node2);
							queueOfSol.add(solution1);

							TechMapSolution solution2 = new TechMapSolution(newSolution);
							solution2.incrementScoreBy(decomposedGate.getRootNode().getScore());
							solution2.assignGateToNode(specNode, gate);
							solution2.assignComponentToNode(node1, gateCd2);
							solution2.assignComponentToNode(node2, gateCd1);
							solution2.addUnmappedNode(node1);
							solution2.addUnmappedNode(node2);
							queueOfSol.add(solution2);

						}

					}
				}

			} 
			if(isSolutionComplete(specGraph, currentSolution) && !hasCrosstalk(currentSolution) && !listOfSolutions.contains(currentSolution)) {
				if(listOfSolutions.size() < numOfSol ) {
					listOfSolutions.add(currentSolution);
				}
				else {
					return listOfSolutions;
				}
			}
		}

		return listOfSolutions;
	}

	public List<TechMapSolution> greedyCover(int numOfSolutions) throws GeneticGatesException {
		
		return cover_(numOfSolutions, true);
	}
	
	private boolean isSolutionComplete(DecomposedGraph specGraph, TechMapSolution solution) {
		for(DecomposedGraphNode node : specGraph.getLeafNodes()) {
			if(solution.getAssignedComponent(node) == null) {
				return false;
			}
		}
		return true;
	}


	public TechMapSolution branchAndBoundCover() throws GeneticGatesException {
		DecomposedGraphNode specOutputNode = matches.getSpecification().getRootNode();
		TechMapSolution bestSolution = new TechMapSolution();
		TechMapSolution currSolution = new TechMapSolution();
		currSolution.setScore(0.0);
		TechMapSolution finalSolution = branchAndBoundCover_recurs(matches.getSpecification(), specOutputNode, null, currSolution, bestSolution);
		return finalSolution;
	}

	private TechMapSolution branchAndBoundCover_recurs(DecomposedGraph specGraph, DecomposedGraphNode specNode, GeneticGate prevGate, 
			TechMapSolution currentSolution, TechMapSolution bestSolution) throws GeneticGatesException {

		List<GeneticGate> gateList = matches.getGateList(specNode);
		if(gateList == null) {
			if(isSolutionComplete(specGraph, currentSolution) && !hasCrosstalk(currentSolution) && currentSolution.compareTo(bestSolution) < 0) {
				return currentSolution;
			}
			else {
				return bestSolution;
			}
		}

		double boundedScore = specNode.getScore();
		for(GeneticGate gate : gateList) {
			DecomposedGraph decomposedGate = gate.getDecomposedGraph();
			double estimatedScore = boundedScore + decomposedGate.getRootNode().getScore() + currentSolution.getScore();
			if(estimatedScore >= bestSolution.getScore() ||
					hasSignalMismatch(prevGate, gate) ){				
				continue;
			}
			else {
				TechMapSolution newSolution = new TechMapSolution(currentSolution);
				newSolution.incrementScoreBy(decomposedGate.getRootNode().getScore());
				newSolution.assignGateToNode(specNode, gate);
				if(specGraph.isRoot(specNode)) {
					newSolution.assignComponentToNode(specNode, gate.getListOfOutputsAsComponentDefinition().get(0));
				}
				List<DecomposedGraphNode> nextSpecNode = EndNode.getMatchingEndNodes(specNode, decomposedGate.getRootNode());

				if(nextSpecNode.size() == 1) {
					if(!newSolution.isNodeMapped(nextSpecNode.get(0))) {
						newSolution.assignComponentToNode(nextSpecNode.get(0), decomposedGate.getLeafNodes().get(0).getComponentDefinition().get());
						bestSolution = branchAndBoundCover_recurs(specGraph, nextSpecNode.get(0), gate, newSolution, bestSolution);
					}
					else {
						ComponentDefinition specCd = newSolution.getAssignedComponent(nextSpecNode.get(0));
						ComponentDefinition gateCd = gate.getListOfInputsAsComponentDefinition().get(0);
						if(!specCd.getIdentity().equals(gateCd.getIdentity())) {
							newSolution.setScore(Double.POSITIVE_INFINITY);
						}
					}
				}
				else if(nextSpecNode.size() == 2) {
					DecomposedGraphNode node1 = nextSpecNode.get(0);
					DecomposedGraphNode node2 = nextSpecNode.get(1);
					ComponentDefinition gateCd1 = gate.getListOfInputsAsComponentDefinition().get(0);
					ComponentDefinition gateCd2 = gate.getListOfInputsAsComponentDefinition().get(1);

					if(!newSolution.isNodeMapped(node1) && newSolution.isNodeMapped(node2)) {
						if(newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd1.getIdentity())) {
							newSolution.assignComponentToNode(node1, gateCd2);
							bestSolution = branchAndBoundCover_recurs(specGraph, node1, gate, newSolution, bestSolution);
						}
						else if(newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd2.getIdentity())) {
							newSolution.assignComponentToNode(node1, gateCd1);
							bestSolution = branchAndBoundCover_recurs(specGraph, node1, gate, newSolution, bestSolution);
						}
						else {
							newSolution.setScore(Double.POSITIVE_INFINITY);
						}
					}
					else if(newSolution.isNodeMapped(node1) && !newSolution.isNodeMapped(node2)) {
						if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd1.getIdentity())) {
							newSolution.assignComponentToNode(node2, gateCd2);
							bestSolution = branchAndBoundCover_recurs(specGraph, node2, gate, newSolution, bestSolution);
						}
						else if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd2.getIdentity())) {
							newSolution.assignComponentToNode(node2, gateCd1);
							bestSolution = branchAndBoundCover_recurs(specGraph, node2, gate, newSolution, bestSolution);
						}
						else {
							newSolution.setScore(Double.POSITIVE_INFINITY);
						}
					}
					else if(newSolution.isNodeMapped(node1) && newSolution.isNodeMapped(node2)) {
						if((!newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd1.getIdentity()) || 
								!newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd2.getIdentity())) && 
								(!newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd2.getIdentity()) || 
										!newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd1.getIdentity()))){
							newSolution.setScore(Double.POSITIVE_INFINITY);
						}
					}
					else {
						TechMapSolution solution1 = new TechMapSolution(newSolution);
						solution1.assignComponentToNode(node1, gateCd1);
						solution1.assignComponentToNode(node2, gateCd2);
						bestSolution = branchAndBoundCover_recurs(specGraph, node1, gate, solution1, bestSolution); 
						bestSolution = branchAndBoundCover_recurs(specGraph, node2, gate, solution1, bestSolution);

						TechMapSolution solution2 = new TechMapSolution(newSolution);
						solution2.assignComponentToNode(node1, gateCd2);
						solution2.assignComponentToNode(node2, gateCd1);
						bestSolution = branchAndBoundCover_recurs(specGraph, node1, gate, solution2, bestSolution);
						bestSolution = branchAndBoundCover_recurs(specGraph, node2, gate, solution2, bestSolution);

					}
				}

			}

		}

		return bestSolution;
	}


	private boolean hasSignalMismatch(GeneticGate previousGate, GeneticGate currentGate) {
		if(previousGate == null) {
			return false;
		}
		//TODO: include signal level matching
		for(ComponentDefinition prevGateInput : previousGate.getListOfInputsAsComponentDefinition()) {
			for(ComponentDefinition currentGateOutput : currentGate.getListOfOutputsAsComponentDefinition()) {
				URI currentGateURI = currentGateOutput.getIdentity();
				URI prevGateURI = prevGateInput.getIdentity();
				if(currentGateURI.equals(prevGateURI)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasCrosstalk(TechMapSolution solution) {
		Map<URI, DecomposedGraphNode> signalMapping = new HashMap<>();
		for(DecomposedGraphNode specNode : solution.getGateMapping().keySet()) {
			if(solution.getGateFromNode(specNode) instanceof WiredORGate)
			{
				continue;
			}

			ComponentDefinition cd = solution.getAssignedComponent(specNode);
			if(signalMapping.containsKey(cd.getIdentity())) {
				DecomposedGraphNode mappedNode = signalMapping.get(cd.getIdentity());
				if(mappedNode != specNode) {
					return true;
				}
			}
			else {
				signalMapping.put(cd.getIdentity(), specNode);
			}
		}
		return false;
	}

}
