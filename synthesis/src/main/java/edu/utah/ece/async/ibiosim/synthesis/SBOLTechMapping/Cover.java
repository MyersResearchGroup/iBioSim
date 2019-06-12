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
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class Cover {

	private Match matches;

	public Cover(Match matches) throws SBOLTechMapException {

		this.matches = matches;
	}


	public TechMapSolution branchAndBoundCover() throws GeneticGatesException {

		DecomposedGraphNode specOutputNode = matches.getSpecification().getOutputNode();
		TechMapSolution bestSolution = new TechMapSolution();
		TechMapSolution currSolution = new TechMapSolution();
		currSolution.setScore(0.0);
		return branchAndBoundCover_recurs(specOutputNode, null, currSolution, bestSolution);
	}

	private TechMapSolution branchAndBoundCover_recurs(DecomposedGraphNode specNode, GeneticGate previousGateSol, 
			TechMapSolution currentSolution, TechMapSolution bestSolution) throws GeneticGatesException {

		List<GeneticGate> gateList = matches.getGateList(specNode);

		if(gateList == null) {
			return currentSolution;
		}

		double boundedScore = specNode.getScore();
		for(GeneticGate gate : gateList) {
			DecomposedGraph decomposedGate = gate.getDecomposedGraph();
			double estimatedScore = boundedScore + decomposedGate.getOutputNode().getScore() + currentSolution.getScore();
			if(estimatedScore >= bestSolution.getScore() ||
					hasSignalMismatch(previousGateSol, gate) ){				
				//hasCrosstalk(specNode, mapOfAssignedNodes, cloneSolution, gate, previousGateSol)) {
				continue;
			}
			else {
				TechMapSolution newSolution = new TechMapSolution(currentSolution);
				newSolution.incrementScoreBy(decomposedGate.getOutputNode().getScore());
				newSolution.assignGateToNode(specNode, gate);

				List<DecomposedGraphNode> nextSpecNode = EndNode.getMatchingEndNodes(specNode, decomposedGate.getOutputNode());

				if(nextSpecNode.size() == 1) {
					if(!newSolution.isNodeMapped(nextSpecNode.get(0))) {
						newSolution.assignComponentToNode(nextSpecNode.get(0), decomposedGate.getLeafNodes().get(0).getComponentDefinition().get());
						newSolution = branchAndBoundCover_recurs(nextSpecNode.get(0), gate, newSolution, bestSolution);
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
						}
						else if(newSolution.getAssignedComponent(node2).getIdentity().equals(gateCd2.getIdentity())) {
							newSolution.assignComponentToNode(node1, gateCd1);
						}
						else {
							newSolution.setScore(Double.POSITIVE_INFINITY);
						}
					}
					else if(newSolution.isNodeMapped(node1) && !newSolution.isNodeMapped(node2)) {
						if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd1.getIdentity())) {
							newSolution.assignComponentToNode(node2, gateCd2);
						}
						else if(newSolution.getAssignedComponent(node1).getIdentity().equals(gateCd2.getIdentity())) {
							newSolution.assignComponentToNode(node2, gateCd1);
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
						solution1.assignComponentToNode(nextSpecNode.get(0), decomposedGate.getLeafNodes().get(0).getComponentDefinition().get());
						solution1.assignComponentToNode(nextSpecNode.get(1), decomposedGate.getLeafNodes().get(1).getComponentDefinition().get());
						solution1 = branchAndBoundCover_recurs(nextSpecNode.get(0), gate, solution1, bestSolution); 
						solution1 = branchAndBoundCover_recurs(nextSpecNode.get(1), gate, solution1, bestSolution);

						TechMapSolution solution2 = new TechMapSolution(newSolution);
						solution2.assignComponentToNode(nextSpecNode.get(0), decomposedGate.getLeafNodes().get(1).getComponentDefinition().get());
						solution2.assignComponentToNode(nextSpecNode.get(1), decomposedGate.getLeafNodes().get(0).getComponentDefinition().get());
						solution2 = branchAndBoundCover_recurs(nextSpecNode.get(0), gate, solution2, bestSolution);
						solution2 = branchAndBoundCover_recurs(nextSpecNode.get(1), gate, solution2, bestSolution);

						if(solution1.compareTo(solution2) < 0) {
							newSolution = solution1;
						}
						else {
							newSolution = solution2;
						}
					}
				}
				//No gates could match to spec. node, reset score
				if(!newSolution.isNodeMapped(specNode)) {
					newSolution.incrementScoreBy(Double.POSITIVE_INFINITY);
				}
				if(newSolution.compareTo(bestSolution) < 0 && !hasCrosstalk(newSolution)) {
					bestSolution = newSolution;
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
		
		return false;
	}







}
