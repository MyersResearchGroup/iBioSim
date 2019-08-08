package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.List;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public interface Match {
  
	public DecomposedGraph getSpecification();

	public List<GeneticGate> getGateList(DecomposedGraphNode node)throws GeneticGatesException;
	
	public List<GeneticGate> getLibrary();
	
	public void sortAscendingOrder(List<GeneticGate> library);
}
