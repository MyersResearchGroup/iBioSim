package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbolstandard.core2.ComponentInstance;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;

public class SBOLTechMap {

	private List<SBOLGraph> libraryGraphs; 



	public SBOLTechMap(SBOLDocument libraryDoc) throws SBOLTechMapException {
		libraryGraphs = setLibraryGates(libraryDoc);
		setLibraryGateScores(libraryGraphs);
	}

	private List<SBOLGraph> setLibraryGates(SBOLDocument sbolDoc) throws SBOLTechMapException {
		List<SBOLGraph> library = new ArrayList<SBOLGraph>();
		for(ModuleDefinition gateMD : sbolDoc.getModuleDefinitions()) {
			library.add(createSBOLGraph(sbolDoc, gateMD));
		}
		return library;
	}

	private void setLibraryGateScores(List<SBOLGraph> library) {
		for(SBOLGraph g: library) {
			setGraphScore(g);
		}
	}

	private void setGraphScore(SBOLGraph graph) {
		int totalScore = 0;
		for(SynthesisNode node : graph.getAllNodes()) {
			int score = node.getFlattenedSequence().length() - 1;
			node.setScore(score);
			totalScore += score;
		}
		graph.setScore(totalScore);
	}

	public SBOLGraph createSBOLGraph(SBOLDocument sbolDoc, ModuleDefinition md) throws SBOLTechMapException {
		SBOLGraph sbolGraph = new SBOLGraph();
		sbolGraph.createGraph(sbolDoc, md);
		sbolGraph.topologicalSort();
		return sbolGraph;
	}

	public void mapCoverSpecification(SBOLDocument sbolDoc) throws SBOLTechMapException {
		Set<ModuleDefinition> rootMDList = sbolDoc.getRootModuleDefinitions();

		//this is flattened spec
		if(rootMDList.size() == 1) {
			SBOLGraph spec = createSBOLGraph(sbolDoc, rootMDList.iterator().next());
		}
		else {
			for(ModuleDefinition md : rootMDList) {
				
				//match and cover gates for subcircuits
				Set<Module> subCircuitList = md.getModules();
				Map<SynthesisNode, FunctionalComponent> feedbackNodeMapping = new HashMap<>();
				boolean isFirstCircuitMapping = true;
				for(Module circuitInstance : subCircuitList) {
					
					ModuleDefinition circuit = circuitInstance.getDefinition();
					SBOLGraph spec = createSBOLGraph(sbolDoc, circuit);
		
					spec.getOutputNode().getFunctionalComponent();
					for(SynthesisNode leafNode : spec.getRoots()) {
						leafNode.getFunctionalComponent();
					}
					Map<SynthesisNode, SBOLGraph> solution = matchAndCover(spec);
					if(isFirstCircuitMapping) {
						for(MapsTo mp : circuitInstance.getMapsTos()) {
							FunctionalComponent fc = getMapsToFunctionalComponent(mp);
							
						}
						isFirstCircuitMapping = false;
					}
				}
			}

		}
	}
	
	private FunctionalComponent getMapsToFunctionalComponent(MapsTo mp) {
		RefinementType refinement = mp.getRefinement();
		ComponentInstance component = null;
		if(refinement.equals(RefinementType.USELOCAL)) {
			component = mp.getLocal();
		}
		else if(refinement.equals(RefinementType.USEREMOTE)) {
			component = mp.getRemote();
		}
		else if(refinement.equals(RefinementType.VERIFYIDENTICAL)) {
			component = mp.getLocal();
		}

		assert(component instanceof FunctionalComponent);
		return (FunctionalComponent) component;
	}

	private Map<SynthesisNode, SBOLGraph> matchAndCover(SBOLGraph specGraph) {

		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = match_topLevel(specGraph);
		Map<SynthesisNode, SBOLGraph> solution = cover_topLevel(specGraph, matches);
	
		return solution;
	}

	public Map<SynthesisNode, LinkedList<WeightedGraph>> match_topLevel(SBOLGraph specGraph) {
		//map spec. node to possible matching library gates
		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = new HashMap<>();

		setAllGraphNodeScore(specGraph, Double.POSITIVE_INFINITY);

		List<SynthesisNode> specNodeList = specGraph.getTopologicalSortNodes();
		for(SynthesisNode currentSpecNode: specNodeList) //go through each species node in speciGraph
		{
			if(currentSpecNode.isRoot()) {
				currentSpecNode.setScore(0);
				matches.put(currentSpecNode, new LinkedList<WeightedGraph>());
			}
			else {
				double totalScore;
				for(SBOLGraph gate : libraryGraphs) {
					SynthesisNode gateOuputNode = gate.getOutputNode();
					if(isMatch(currentSpecNode, gateOuputNode)) {
						totalScore = gateOuputNode.getScore() + getSubNodeScore(currentSpecNode, gateOuputNode);
						if(totalScore < currentSpecNode.getScore()) {
							//a lower score was found, add gate to the matched spec. node
							currentSpecNode.setScore(totalScore); //update speciGraph with new libGate score

							if(!matches.containsKey(currentSpecNode)) {
								matches.put(currentSpecNode, new LinkedList<WeightedGraph>());
							}
							matches.get(currentSpecNode).addFirst(new WeightedGraph(gate, totalScore));
						}
						else {

							if(!matches.containsKey(currentSpecNode)) {
								matches.put(currentSpecNode, new LinkedList<WeightedGraph>());
								matches.get(currentSpecNode).add(new WeightedGraph(gate, totalScore));
							}
							else {
								//find the correct location to put the gate such that it is in ascending order
								//base off of score values
								//Assuming every time add new gate to list, the list should be already ordered
								LinkedList<WeightedGraph> matchedGatelist = matches.get(currentSpecNode);

								for(int i = matchedGatelist.size()-1; i >= 0; i--) {
									if(matchedGatelist.get(i).getWeight() <= totalScore) {
										int index = i + 1; 
										matchedGatelist.add(index, new WeightedGraph(gate, totalScore));
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		return matches;
	}

	public Map<SynthesisNode, SBOLGraph> cover_topLevel(SBOLGraph specGraph, Map<SynthesisNode, LinkedList<WeightedGraph>> matches) {
		double bestScore = Double.POSITIVE_INFINITY;
		double currentScore = 0;
		SynthesisNode specOutNode = specGraph.getOutputNode();
		return cover(specGraph, specOutNode, matches, bestScore, currentScore);
	}

	private Map<SynthesisNode, SBOLGraph> cover(SBOLGraph specGraph, SynthesisNode specOutNode, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore) {
		LinkedList<WeightedGraph> matchedLibGates = matches.get(specOutNode);
		Map<SynthesisNode, SBOLGraph> bestSolution = null;

		for (WeightedGraph wg : matchedLibGates) {
			SBOLGraph libGate = wg.getSBOLGraph();
			double estimateScore = libGate.getOutputNode().getScore() + getSubNodeScore(specOutNode, libGate.getOutputNode());
			if (estimateScore >= bestScore) {
				continue;
			}
			else {
				Map<SynthesisNode, SBOLGraph> tempSolution = new HashMap<SynthesisNode, SBOLGraph>();
				tempSolution.put(specOutNode, libGate);
				double score = libGate.getOutputNode().getScore();
				tempSolution.put(specOutNode, libGate);
				List<SynthesisNode> childrenNodes = getEndNodes(specOutNode, libGate.getOutputNode());
				if (childrenNodes.size() > 0) {
					for (SynthesisNode child : childrenNodes) {
						tempSolution = coverRecursive(child, matches, bestScore, currentScore, tempSolution);
					}
				}

				if (tempSolution != null) {
					score = getCurrentCoveredScore(tempSolution.values());
					if (score < bestScore) {
						bestScore = score;
						bestSolution = tempSolution;
					}
				}
			}
		}

		return bestSolution;
	}
	
	private Map<SynthesisNode, SBOLGraph> coverRecursive(SynthesisNode specOutNode, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore, Map<SynthesisNode, SBOLGraph> solution) {
		LinkedList<WeightedGraph> matchedLibGates = matches.get(specOutNode);
		Map<SynthesisNode, SBOLGraph> bestSolution = null;
		if (specOutNode.isRoot()) {
			return solution;
		}
		for (WeightedGraph wg : matchedLibGates) {
			SBOLGraph libGate = wg.getSBOLGraph();
			if (isCrossTalk(solution.values(), libGate)) {
				continue;
			}
			else {
				double estimateScore = currentScore + libGate.getOutputNode().getScore() + getSubNodeScore(specOutNode, libGate.getOutputNode());
				if (estimateScore >= bestScore) {
					continue;
				}
				else {
					Map<SynthesisNode, SBOLGraph> tempSolution = new HashMap<SynthesisNode, SBOLGraph>(solution);
					currentScore = getCurrentCoveredScore(tempSolution.values());
					tempSolution.put(specOutNode, libGate);
					List<SynthesisNode> childrenNodes = getEndNodes(specOutNode, libGate.getOutputNode());
					tempSolution.put(specOutNode, libGate);
					if (childrenNodes.size() > 0) {
						for (SynthesisNode child : childrenNodes) {
							tempSolution = coverRecursive(child, matches, bestScore, currentScore, tempSolution);
							if (tempSolution == null) {
								break;
							}
						}
					}

					if (tempSolution != null) {
						double score = getCurrentCoveredScore(tempSolution.values());
						if (score < bestScore) {
							bestScore = score;
							bestSolution = tempSolution;
						}
					}

				}
			}

		}
		return bestSolution;
	}
	
	/**
	 * Return a list of leaf nodes from the spec that maps to the specified library gate
	 * @param spec
	 * @param lib
	 * @return
	 */
	public List<SynthesisNode> getEndNodes(SynthesisNode spec, SynthesisNode lib) {
		List<SynthesisNode> list = new ArrayList<SynthesisNode>();
		getNodes(spec, lib, list);
		return list;

	}
	
	private void getNodes(SynthesisNode spec, SynthesisNode lib, List<SynthesisNode> nodes) {
		if(lib.isRoot()) {
			nodes.add(spec);
		}
		else {
			for(int i=0; i<lib.getParents().size(); i++) {
				getNodes(spec.getParents().get(i), lib.getParents().get(i), nodes);
			}
		}
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
		for(SynthesisNode g1Node: g1.getTopologicalSortNodes()) {
			for(SynthesisNode g2Node: g2.getTopologicalSortNodes()) {
				if(g1Node.getFunctionalComponent().getDefinitionURI().equals(g2Node.getFunctionalComponent().getDefinitionURI())) {
					return true;
				}
			}
		}
		return false; 
	}

	private double getSubNodeScore(SynthesisNode spec, SynthesisNode lib) {
		if(lib.isRoot()) {
			//the spec score is returned because the current best score is stored on spec not lib gate.
			return spec.getScore(); 
		}
		double total = 0;
		for(int i = 0; i < lib.getParents().size(); i++) {
			total += getSubNodeScore(spec.getParents().get(i), lib.getParents().get(i));
		}
		return total;
	}

	private double getCurrentCoveredScore(Collection<SBOLGraph> gatesUsed) {
		double totalScore = 0; 
		for(SBOLGraph g: gatesUsed) {
			totalScore += g.getOutputNode().getScore();
		}
		return totalScore; 
	}
	
	private boolean isMatch(SynthesisNode spec, SynthesisNode lib) {
		if(lib.isRoot()) {
			return true;
		}
		else {
			if(spec.isRoot()) { 
				return false;
			}
			if(getDegree(spec) != getDegree(lib)) { 
				return false; 
			}
			if(getDegree(spec) == 1) {
				SynthesisNode specChild = spec.getParents().get(0);
				SynthesisNode libChild = lib.getParents().get(0);
				return isMatch(specChild, libChild);
			}
			else {
				//NOTE: this assumes it always has at most two children due to decomposition
				SynthesisNode specLeftChild = spec.getParents().get(0);
				SynthesisNode libLeftChild = lib.getParents().get(0);
				SynthesisNode specRightChild = spec.getParents().get(1);
				SynthesisNode libRightChild = lib.getParents().get(1);
				return isMatch(specLeftChild, libLeftChild) && isMatch(specRightChild, libRightChild) || isMatch(specLeftChild, libRightChild) && isMatch(specRightChild, libLeftChild);
			}
		}
	}

	private void setAllGraphNodeScore(SBOLGraph graph, Double score) {
		for(SynthesisNode node : graph.getTopologicalSortNodes()) {
			node.setScore(score);
		}
	}

	private int getDegree(SynthesisNode node) {
		return node.getParents().size();
	}

	public List<SBOLGraph> getLibrary(){
		return this.libraryGraphs;
	}
}
