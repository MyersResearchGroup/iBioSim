package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.Comparator;

public class GeneticGateScoreComparator implements Comparator<GeneticGate>{

	@Override
	public int compare(GeneticGate o1, GeneticGate o2) {
		return Double.compare(o1.getDecomposedGraph().getRootNode().getScore(), o2.getDecomposedGraph().getRootNode().getScore());
	}

}
