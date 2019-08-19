package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
	private Map<DecomposedGraphNode, URI> mapOfAssignedNodes;
	private Queue<DecomposedGraphNode> nodeQueue;
	
	public TechMapSolution() {
		score = Double.POSITIVE_INFINITY;
		assignedNodes = new HashMap<>();
		mapOfAssignedNodes = new HashMap<>();
		nodeQueue = new LinkedList<>();
	}

	public TechMapSolution(TechMapSolution copy) {
		this.score = copy.score;
		this.assignedNodes = new HashMap<>(copy.assignedNodes);
		this.mapOfAssignedNodes = new HashMap<>(copy.mapOfAssignedNodes);
		this.nodeQueue = new LinkedList<>(copy.nodeQueue);
	}
	
	public void setScore(double value) {
		score = value;
	}
	
	public DecomposedGraphNode getNextUnmappedNode() {
		return this.nodeQueue.poll();
	}

	public boolean hasUnmappedNode() {
		return !this.nodeQueue.isEmpty();
	}
	
	public void addUnmappedNode(DecomposedGraphNode node) {
		this.nodeQueue.add(node);
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

	

	public URI getAssignedComponent(DecomposedGraphNode node) {
		return mapOfAssignedNodes.get(node);
	}

	public void assignComponentToNode(DecomposedGraphNode node, URI component) {
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
