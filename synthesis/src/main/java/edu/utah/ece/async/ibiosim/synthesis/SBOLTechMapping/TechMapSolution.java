package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.HashMap;
import java.util.Map;

import org.sbolstandard.core2.ComponentDefinition;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class TechMapSolution implements Solution, Comparable<Solution> {
	private double score;
	private Map<DecomposedGraphNode, GeneticGate> assignedNodes;
	private Map<DecomposedGraphNode, ComponentDefinition> mapOfAssignedNodes;

	public TechMapSolution() {
		score = Double.POSITIVE_INFINITY;
		assignedNodes = new HashMap<>();
		mapOfAssignedNodes = new HashMap<>();
	}

	public TechMapSolution(TechMapSolution copy) {
		this.score = copy.score;
		this.assignedNodes = new HashMap<>(copy.assignedNodes);
		this.mapOfAssignedNodes = new HashMap<>(copy.mapOfAssignedNodes);
	}
	
	public void setScore(double value) {
		score = value;
	}

	/** 
	 * 
	 */
	@Override
	public double getScore() {
		return score;
	}

	/**
	 *
	 */
	@Override
	public void incrementScoreBy(double increment) {
		this.score += increment;
	}

	/**
	 *
	 */
	@Override
	public Map<DecomposedGraphNode, GeneticGate> getGateMapping() {
		return this.assignedNodes;
	}
	
	public GeneticGate getGateFromNode(DecomposedGraphNode node) {
		return assignedNodes.get(node);
	}

	public boolean isNodeMapped(DecomposedGraphNode node) {
		return assignedNodes.containsKey(node);
	}

	public void assignGateToNode(DecomposedGraphNode node, GeneticGate gate) {
		assignedNodes.put(node, gate);
	}
	
	public ComponentDefinition getAssignedComponent(DecomposedGraphNode node) {
		return mapOfAssignedNodes.get(node);
	}

	public void assignComponentToNode(DecomposedGraphNode node, ComponentDefinition component) {
		mapOfAssignedNodes.put(node, component);
	}

	@Override
	public int compareTo(Solution other) {
		return Double.compare(score, other.getScore());
	}
}
