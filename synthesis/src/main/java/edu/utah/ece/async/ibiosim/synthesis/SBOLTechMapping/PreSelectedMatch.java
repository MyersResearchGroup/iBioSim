package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.FunctionalComponent;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class PreSelectedMatch implements Match {


	private Map<DecomposedGraphNode, LinkedList<GeneticGate>> matches;
	private DecomposedGraph specGraph;

	public PreSelectedMatch(DecomposedGraph specGraph) {
		this.specGraph = specGraph;
	}

	/** 
	 * 
	 */
	public List<GeneticGate> getGateList(DecomposedGraphNode node) {
		return matches.get(node);
	}

	public DecomposedGraph getSpecification() {
		return specGraph;
	}

	private boolean gateMatchPreselectedSpecNode(GeneticGate gate, DecomposedGraphNode specOutputNode) {
		//TODO: 
		for(FunctionalComponent gateOutput : gate.getListOfOutputs()) {
			if(specOutputNode.getComponentDefinition().equals(gateOutput.getDefinition())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<GeneticGate> getLibrary() {
		// TODO Auto-generated method stub
		return null;
	}
}
