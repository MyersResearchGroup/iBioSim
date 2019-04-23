package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.SBOLGraph;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class SBOLTechMap {
	private List<DecomposedGraph> specGraph;
	private List<DecomposedGraph> libraryGraphs; 

	public SBOLTechMap(List<DecomposedGraph> libraryGates, List<DecomposedGraph> specification) throws SBOLTechMapException {
		this.libraryGraphs = libraryGates;
		this.specGraph = specification;
	}
	
	public SBOLDocument performTechnologyMapping() throws GeneticGatesException {
		for(DecomposedGraph subSpec : specGraph) {
			Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> matches = match(subSpec);
//			Map<FunctionalComponentNode, SBOLGraph> solution = cover_topLevel(subSpec, matches);

		}
		return null;
	}
	
	public Map<FunctionalComponentNode, SBOLGraph> cover_topLevel(SBOLGraph specGraph, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches) {
		double bestScore = Double.POSITIVE_INFINITY;
		double currentScore = 0;
		FunctionalComponentNode specOutNode = specGraph.getOutputNode();
		return cover(specGraph, specOutNode, matches, bestScore, currentScore);
	}
	
	/**
	 * Return a list of leaf nodes from the spec that maps to the specified library gate
	 * @param spec
	 * @param lib
	 * @return
	 */
	public List<FunctionalComponentNode> getEndNodes(FunctionalComponentNode spec, FunctionalComponentNode lib,
			ModuleDefinition specMD, ModuleDefinition libGateMD) {
		List<FunctionalComponentNode> list = new ArrayList<FunctionalComponentNode>();
		getNodes(spec, lib, list, specMD, libGateMD);
		return list;

	}
	
	public Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> match(DecomposedGraph specGraph) throws GeneticGatesException {
		// map spec. node to possible matching library gates
		Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> matches = new HashMap<>();
		setAllGraphNodeScore(specGraph, Double.POSITIVE_INFINITY);
		List<DecomposedGraphNode> specNodeList = specGraph.topologicalSort();

		for(DecomposedGraphNode currentSpecNode: specNodeList)
		{
			double totalScore;
			for(DecomposedGraph gate : libraryGraphs) {
				DecomposedGraphNode gateOuputNode = gate.getOutputNode();
				if(isMatch(currentSpecNode, gateOuputNode)) {
					double tempSpecScore = currentSpecNode.getScore();
					if(currentSpecNode.getScore() == Double.POSITIVE_INFINITY) {
						tempSpecScore = 0.0;
					}
					totalScore = gateOuputNode.getScore() + tempSpecScore;
					if(totalScore < currentSpecNode.getScore()) {
						//a lower score was found, add gate to the matched spec. node
						currentSpecNode.setScore(totalScore); //update speciGraph with new libGate score

						if(!matches.containsKey(currentSpecNode)) {
							matches.put(currentSpecNode, new LinkedList<DecomposedGraph>());
						}
						matches.get(currentSpecNode).addFirst(gate);
					}
					else {
						if(!matches.containsKey(currentSpecNode)) {
							matches.put(currentSpecNode, new LinkedList<DecomposedGraph>());
							matches.get(currentSpecNode).add(gate);
						}
						else {
							//add new gate to the correct position in the list of weightedGraph so they are ordered from lowest to highest in the list.
							LinkedList<DecomposedGraph> matchedGatelist = matches.get(currentSpecNode);
							for(int i = matchedGatelist.size()-1; i >= 0; i--) {
								if(matchedGatelist.get(i).getOutputNode().getScore() <= totalScore) {
									int index = i + 1; 
									matchedGatelist.add(index, gate);
									break;
								}
							}
						}
					}
				}
				else {
					currentSpecNode.setScore(0.0);
				}
			}
		}
		return matches;
	}

	private Map<FunctionalComponentNode, SBOLGraph> cover(SBOLGraph specGraph, FunctionalComponentNode specOutNode, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore) {
		LinkedList<WeightedGraph> matchedLibGates = matches.get(specOutNode);
		Map<FunctionalComponentNode, SBOLGraph> bestSolution = null;
		ModuleDefinition specMD = specGraph.getModuleDefinition();
		for (WeightedGraph wg : matchedLibGates) {
//			SBOLGraph libGate = wg.getSBOLGraph();
//			ModuleDefinition libGateMD = libGate.getModuleDefinition();
//			double estimateScore = libGate.getOutputNode().getScore(specMD) + getSubNodeScore(specMD, specOutNode, libGate.getModuleDefinition(), libGate.getOutputNode());
//			if (estimateScore >= bestScore) {
//				continue;
//			}
//			else {
//				Map<FunctionalComponentNode, SBOLGraph> tempSolution = new HashMap<FunctionalComponentNode, SBOLGraph>();
//				tempSolution.put(specOutNode, libGate);
//				double score = libGate.getOutputNode().getScore(specMD);
//				tempSolution.put(specOutNode, libGate);
//				List<FunctionalComponentNode> childrenNodes = getEndNodes(specOutNode, libGate.getOutputNode(), specMD, libGateMD);
//				if (childrenNodes.size() > 0) {
//					for (FunctionalComponentNode child : childrenNodes) {
//						tempSolution = coverRecursive(specGraph, child, matches, bestScore, currentScore, tempSolution);
//					}
//				}
//
//				if (tempSolution != null) {
//					score = getCurrentCoveredScore(tempSolution.values());
//					if (score < bestScore) {
//						bestScore = score;
//						bestSolution = tempSolution;
//					}
//				}
//			}
		}

		return bestSolution;
	}

	private Map<FunctionalComponentNode, SBOLGraph> coverRecursive(SBOLGraph specGraph, FunctionalComponentNode specOutNode, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore, Map<FunctionalComponentNode, SBOLGraph> solution) {
		LinkedList<WeightedGraph> matchedLibGates = matches.get(specOutNode);
		Map<FunctionalComponentNode, SBOLGraph> bestSolution = null;
		ModuleDefinition specMD = specGraph.getModuleDefinition();
		if (specOutNode.isRoot(specMD)) {
			return solution;
		}
		for (WeightedGraph wg : matchedLibGates) {
//			SBOLGraph libGate = wg.getSBOLGraph();
//			ModuleDefinition libGateMD = libGate.getModuleDefinition();
//			if (isCrossTalk(solution.values(), libGate)) {
//				continue;
//			}
//			else {
//				double estimateScore = currentScore + libGate.getOutputNode().getScore(specMD) + getSubNodeScore(specMD, specOutNode, libGateMD, libGate.getOutputNode());
//				if (estimateScore >= bestScore) {
//					continue;
//				}
//				else {
//					Map<FunctionalComponentNode, SBOLGraph> tempSolution = new HashMap<FunctionalComponentNode, SBOLGraph>(solution);
//					currentScore = getCurrentCoveredScore(tempSolution.values());
//					tempSolution.put(specOutNode, libGate);
//					List<FunctionalComponentNode> childrenNodes = getEndNodes(specOutNode, libGate.getOutputNode(), specMD, libGateMD);
//					tempSolution.put(specOutNode, libGate);
//					if (childrenNodes.size() > 0) {
//						for (FunctionalComponentNode child : childrenNodes) {
//							tempSolution = coverRecursive(specGraph, child, matches, bestScore, currentScore, tempSolution);
//							if (tempSolution == null) {
//								break;
//							}
//						}
//					}
//
//					if (tempSolution != null) {
//						double score = getCurrentCoveredScore(tempSolution.values());
//						if (score < bestScore) {
//							bestScore = score;
//							bestSolution = tempSolution;
//						}
//					}
//
//				}
//			}

		}
		return bestSolution;
	}
	
	

	private double getCurrentCoveredScore(Collection<SBOLGraph> gatesUsed) {
		double totalScore = 0; 
		for(SBOLGraph g: gatesUsed) {
			totalScore += g.getOutputNode().getScore(g.getModuleDefinition());
		}
		return totalScore; 
	}

	private int getDegree(FunctionalComponentNode node, ModuleDefinition graphMD) {
		return node.getParents(graphMD).size();
	}
	
	
	private void getNodes(FunctionalComponentNode spec, FunctionalComponentNode lib, List<FunctionalComponentNode> nodes, 
			ModuleDefinition specMD, ModuleDefinition libGateMD) {
		if(lib.isRoot(libGateMD)) {
			nodes.add(spec);
		}
		else {
			for(int i=0; i<lib.getParents(libGateMD).size(); i++) {
				getNodes(spec.getParents(specMD).get(i), lib.getParents(libGateMD).get(i), nodes, specMD, libGateMD);
			}
		}
	}
	
	
	private double getSubNodeScore(ModuleDefinition specMD, FunctionalComponentNode spec, 
			ModuleDefinition libMD, FunctionalComponentNode lib) {
		if(lib.isRoot(libMD)) {
			// The spec score is returned because the current best score is stored on spec not lib gate.
			return spec.getScore(specMD); 
		}
		double total = 0;
		for(int i = 0; i < lib.getParents(libMD).size(); i++) {
			total += getSubNodeScore(specMD, spec.getParents(specMD).get(i), libMD, lib.getParents(libMD).get(i));
		}
		return total;
	}

	

	private boolean isCrossTalk(Collection<SBOLGraph> gatesUsed, SBOLGraph gate) {
		for(SBOLGraph g: gatesUsed) {
			if(isGateMatch(g, gate)) {
				return true;
			}
		}
		return false; 
	}

	private boolean isGateMatch(SBOLGraph g1, SBOLGraph g2) {
		for(FunctionalComponentNode g1Node: g1.getTopologicalSortNodes()) {
			for(FunctionalComponentNode g2Node: g2.getTopologicalSortNodes()) {
				if(g1Node.getFunctionalComponent().getDefinitionURI().equals(g2Node.getFunctionalComponent().getDefinitionURI())) {
					return true;
				}
			}
		}
		return false; 
	}
	
	private boolean isMatch(DecomposedGraphNode specNode, DecomposedGraphNode libNode) throws GeneticGatesException {
		if(libNode.getChildrenNodeList().size() == 0) {
			DecomposedGraphNode libParentNode = libNode.getParentNodeList().iterator().next();
			DecomposedGraphNode specParentNode = specNode.getParentNodeList().iterator().next();
			return (libNode.getParentInteractionType(libParentNode) == specNode.getParentInteractionType(specParentNode));
		}
		Set<DecomposedGraphNode> specChildren = specNode.getChildrenNodeList();
		Set<DecomposedGraphNode> libChildren = libNode.getChildrenNodeList();
		if(specChildren.size() != libChildren.size()) {
			return false;
		}
		if(specChildren.size() == 1 && libChildren.size() == 1) {
			DecomposedGraphNode specChild = specChildren.iterator().next();
			DecomposedGraphNode libChild = libChildren.iterator().next();
			if(libNode.getChildInteractionType(libChild) == specNode.getChildInteractionType(specChild)) {
				return isMatch(specChild, libChild);
			}
			else {
				return false;
			}
		}
		assert(specChildren.size() == 2 && libChildren.size() == 2);
		
		DecomposedGraphNode specLeftChild = specChildren.iterator().next();
		DecomposedGraphNode specRightChild = specChildren.iterator().next();
		
		DecomposedGraphNode libLeftChild = libChildren.iterator().next();
		DecomposedGraphNode libRightChild = libChildren.iterator().next();
		
		if(libNode.getChildInteractionType(libLeftChild) != specNode.getChildInteractionType(specLeftChild)
				&& libNode.getChildInteractionType(libLeftChild) != specNode.getChildInteractionType(specRightChild)
				&& libNode.getChildInteractionType(libRightChild) != specNode.getChildInteractionType(specLeftChild)
				&& libNode.getChildInteractionType(libRightChild) != specNode.getChildInteractionType(specRightChild)) {
			return false;
		}
		
		
		return isMatch(specLeftChild, libLeftChild) || isMatch(specLeftChild, libRightChild)
				|| isMatch(specRightChild, libLeftChild) || isMatch(specRightChild, libRightChild);
	}

	private boolean isMatch(SBOLGraph g, FunctionalComponentNode spec, FunctionalComponentNode lib,
			ModuleDefinition specMD, ModuleDefinition libGateMD) {
		if(lib.isRoot(g.getModuleDefinition())) {
			return true;
		}
		
			if(spec.isRoot(g.getModuleDefinition())) { 
				return false;
			}
			if(getDegree(spec, specMD) != getDegree(lib, libGateMD)) { 
				return false; 
			}
			if(getDegree(spec, specMD) == 1) {
				FunctionalComponentNode specChild = spec.getParents(g.getModuleDefinition()).get(0);
				FunctionalComponentNode libChild = lib.getParents(g.getModuleDefinition()).get(0);
				return isMatch(g, specChild, libChild, specMD, libGateMD);
			}
			
				//NOTE: this assumes it always has at most two children due to decomposition
				FunctionalComponentNode specLeftChild = spec.getParents(g.getModuleDefinition()).get(0);
				FunctionalComponentNode libLeftChild = lib.getParents(g.getModuleDefinition()).get(0);
				FunctionalComponentNode specRightChild = spec.getParents(g.getModuleDefinition()).get(1);
				FunctionalComponentNode libRightChild = lib.getParents(g.getModuleDefinition()).get(1);
				return isMatch(g, specLeftChild, libLeftChild, specMD, libGateMD) && isMatch(g, specRightChild, libRightChild, specMD, libGateMD) 
						|| isMatch(g, specLeftChild, libRightChild, specMD, libGateMD) && isMatch(g, specRightChild, libLeftChild, specMD, libGateMD);
			
	}

	
	private void setAllGraphNodeScore(DecomposedGraph graph, Double score) {
		for(DecomposedGraphNode node : graph.topologicalSort()) {
			node.setScore(score);
		}
	}


}
