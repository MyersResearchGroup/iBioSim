package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.Map;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public interface Solution {
   
	public double getScore();
	
	public void incrementScoreBy(double increment);

	public void assignGateToNode(DecomposedGraphNode node, GeneticGate gate);

	public Map<DecomposedGraphNode, GeneticGate> getGateMapping();
}
