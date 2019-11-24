package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;

public class EndNode {
	final DecomposedGraphNode specNode, libNode;

	EndNode(DecomposedGraphNode specNode, DecomposedGraphNode libNode){
		this.specNode = specNode;
		this.libNode = libNode;
	}
	
	public static List<DecomposedGraphNode> getMatchingEndNodes(DecomposedGraphNode specNode, DecomposedGraphNode libNode) {
		List<DecomposedGraphNode> endNodes = new ArrayList<>();
		Queue<EndNode> queue = new LinkedList<>();
		HashSet<DecomposedGraphNode> visited = new HashSet<>();
		
		queue.add(new EndNode(specNode, libNode));
		
		while(!queue.isEmpty()) {
			EndNode node = queue.poll();
			if(node.libNode.getChildrenNodeList().size() == 0) {
				endNodes.add(node.specNode);
			}
			else {
				for(int i=0; i<node.libNode.getChildrenNodeList().size(); i++) {
					if(!visited.contains(node.specNode.getChildrenNodeList().get(i))) {
						visited.add(node.specNode.getChildrenNodeList().get(i));
						queue.add(new EndNode(node.specNode.getChildrenNodeList().get(i), node.libNode.getChildrenNodeList().get(i)));
					}
				}
			}
		}
		return endNodes;
	} 
}