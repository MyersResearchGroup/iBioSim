package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.HashMap;
import java.util.Map;

import org.sbolstandard.core2.ComponentDefinition;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;

/**
 * A class that holds the solution of the Technology Mapping procedure.
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
	 * Retrieve the {@link GeneticGate} assigned to the given {@link DecomposedGraphNode}.
	 * @param node: A {@link DecomposedGraphNode} that a {@link GeneticGate} is assigned to.
	 * @return A genetic gate assigned to the given node. Null is returned if no gate is assigned.
	 */
	public GeneticGate getGateFromNode(DecomposedGraphNode node) {
		return assignedNodes.get(node);
	}

	/**
	 * Check if the given {@link DecomposedGraphNode} has been assigned with a GeneticGate.
	 * @param node A {@link DecomposedGraphNode}
	 * @return True if the given {@link DecomposedGraphNode} has been assigned with a GeneticGate. Otherwise, false is returned.
	 */
	public boolean isNodeMapped(DecomposedGraphNode node) {
		return assignedNodes.containsKey(node);
	}

	

	/**
	 * Get the ComponentDefinition that was assigned to the given {@link DecomposedGraphNode}.
	 * @param node A {@link DecomposedGraphNode}
	 * @return A {@link ComponentDefinition} that is assigned to the given {@link DecomposedGraphNode}. 
	 * Null is returned if the {@link DecomposedGraphNode} was not found or no {@link ComponentDefinition} was assigned to the given {@link DecomposedGraphNode}.
	 */
	public ComponentDefinition getAssignedComponent(DecomposedGraphNode node) {
		return mapOfAssignedNodes.get(node);
	}

	/**
	 * Assign the given {@link DecomposedGraphNode} with the given {@link ComponentDefinition}.
	 * @param node The {@link DecomposedGraphNode} that the {@link ComponentDefinition} is assigned to.
	 * @param component The {@link ComponentDefinition} that the {@link DecomposedGraphNode} is assigned with.
	 */
	public void assignComponentToNode(DecomposedGraphNode node, ComponentDefinition component) {
		mapOfAssignedNodes.put(node, component);
	}
	
	@Override
	public void assignGateToNode(DecomposedGraphNode node, GeneticGate gate) {
		assignedNodes.put(node, gate);
	}
	
	@Override
	public int compareTo(Solution other) {
		return Double.compare(score, other.getScore());
	}

	@Override
	public double getScore() {
		return score;
	}

	@Override
	public void incrementScoreBy(double increment) {
		this.score += increment;
	}

	@Override
	public Map<DecomposedGraphNode, GeneticGate> getGateMapping() {
		return this.assignedNodes;
	}
}
