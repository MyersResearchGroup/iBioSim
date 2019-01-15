/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Synthesis {
	
	private List<SBOLGraph> libraryGraphs; 
	private List<SBOLGraph> specificationGraphs; 
	
	private int moduleCounter, mapsToCounter, fcCounter;
	private Map<String, Map<SynthesisNode, SBOLGraph>> mappedSolution;
	
	private SBOLUtility sbolUtil;
	
	public Synthesis() {
		sbolUtil = SBOLUtility.getInstance();
		
		mappedSolution = new HashMap<>();
		
		libraryGraphs = new ArrayList<>();
		specificationGraphs = new ArrayList<>();
	}


	public void createSBOLGraph(SBOLDocument sbolDoc, boolean isLibraryFile) throws SBOLTechMapException {
		for(ModuleDefinition m : sbolDoc.getModuleDefinitions()) {
			SBOLGraph sbolGraph = new SBOLGraph();
			sbolGraph.createGraph(sbolDoc, m);
			sbolGraph.topologicalSort();
			if(isLibraryFile) {
				libraryGraphs.add(sbolGraph);
			}
			else {
				specificationGraphs.add(sbolGraph);
			}
		}

	}

	public void setLibraryGateScores(List<SBOLGraph> library) {
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
	
	public void matchAndCover() {
		Map<SynthesisNode, LinkedList<WeightedGraph>> matches = new HashMap<SynthesisNode, LinkedList<WeightedGraph>>();
		for(SBOLGraph specGraph :specificationGraphs) {
			match_topLevel(specGraph, matches);
			Map<SynthesisNode, SBOLGraph> solution = cover_topLevel(specGraph, matches);
			mappedSolution.put(specGraph.getOutputNode().getModuleDefinition().getDisplayId(), solution);
		}
	}

	public void match_topLevel(SBOLGraph _specifiGraph, Map<SynthesisNode, LinkedList<WeightedGraph>> matches) {
		setAllGraphNodeScore(_specifiGraph, Double.POSITIVE_INFINITY);
		List<SynthesisNode> s = _specifiGraph.getTopologicalSortNodes();

		for(SynthesisNode n: s) {
			if(n.isRoot()) {
				n.setScore(0);
				matches.put(n, new LinkedList<WeightedGraph>());
			}
			else {
				double totalScore;
				for(SBOLGraph gate : libraryGraphs) {
					SynthesisNode l = gate.getOutputNode();
					if(isMatch(n, l)) {
						totalScore = l.getScore() + getSubNodeScore(n, l);
						if(totalScore < n.getScore()) 
						{
							n.setScore(totalScore); //update speciGraph with new libGate score

							if(!matches.containsKey(n))
							{
								matches.put(n, new LinkedList<WeightedGraph>());
							}
							matches.get(n).addFirst(new WeightedGraph(gate, totalScore));
						}
						else {
							if(!matches.containsKey(n)) {
								matches.put(n, new LinkedList<WeightedGraph>());
								matches.get(n).add(new WeightedGraph(gate, totalScore));
							}
							else {
								/* find the correct location to put the gate such that it is in ascending order base off of score values
								 * Assuming every time add new gate to list, the list should be already ordered
								 */
								LinkedList<WeightedGraph> list = matches.get(n);

								for(int i = list.size()-1; i >= 0; i--) {
									if(list.get(i).getWeight() <= totalScore) {
										int index = i + 1; 
										list.add(index, new WeightedGraph(gate, totalScore));
										break;
									}
								}
							} 
						}
					}
				}
			}
		} 
	}

	public double getSubNodeScore(SynthesisNode spec, SynthesisNode lib) {
		if(lib.isRoot())
			return spec.getScore();
		double total = 0;
		for(int i=0; i<lib.getParents().size(); i++)
		{
			total += getSubNodeScore(spec.getParents().get(i), lib.getParents().get(i));
		}
		return total;
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
		if(lib.isRoot())
			nodes.add(spec);
		else {
			for(int i=0; i<lib.getParents().size(); i++){
				getNodes(spec.getParents().get(i), lib.getParents().get(i), nodes);
			}
		}
	}
	
	public Map<SynthesisNode, SBOLGraph> cover_topLevel(SBOLGraph syn, Map<SynthesisNode, LinkedList<WeightedGraph>> matches) {
		double bestScore = Double.POSITIVE_INFINITY;
		double currentScore = 0;
		SynthesisNode n = syn.getOutputNode();
		Map<SynthesisNode, SBOLGraph> bestSolution = cover(syn, n, matches, bestScore, currentScore);
		
		return bestSolution; 
	}
	
	private Map<SynthesisNode, SBOLGraph> cover(SBOLGraph syn, SynthesisNode n, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore) {
		LinkedList<WeightedGraph> matchedLibGates = matches.get(n);
		Map<SynthesisNode, SBOLGraph> bestSolution = null;

		for (WeightedGraph wg : matchedLibGates) {
			SBOLGraph libGate = wg.getSBOLGraph();
			double estimateScore = libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());

			if (estimateScore >= bestScore) {
				continue;
			}
			else {
				Map<SynthesisNode, SBOLGraph> solutionCopy = new HashMap<SynthesisNode, SBOLGraph>();
				solutionCopy.put(n, libGate);
				double score = libGate.getOutputNode().getScore();
				solutionCopy.put(n, libGate);
				List<SynthesisNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
				if (childrenNodes.size() > 0) {
					for (SynthesisNode child : childrenNodes) {
						solutionCopy = coverRecursive(child, matches, bestScore, currentScore, solutionCopy);
					}
				}

				if (solutionCopy != null) {
					score = getCurrentCoveredScore(solutionCopy.values());
					if (score < bestScore) {
						bestScore = score;
						bestSolution = solutionCopy;
					}
				}
			}
		}

		return bestSolution;
	}

	private Map<SynthesisNode, SBOLGraph> coverRecursive(SynthesisNode n, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore, Map<SynthesisNode, SBOLGraph> solution)
	{
		LinkedList<WeightedGraph> matchedLibGates = matches.get(n);
		Map<SynthesisNode, SBOLGraph> bestSolution = null;
		if (n.isRoot()) {
			return solution;
		}
		for (WeightedGraph wg : matchedLibGates) {
			SBOLGraph libGate = wg.getSBOLGraph();
			if (isCrossTalk(solution.values(), libGate)) {
				continue;
			}
			else {
				double estimateScore = currentScore + libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());

				if (estimateScore >= bestScore) {
					continue;
				}
				else {
					Map<SynthesisNode, SBOLGraph> solutionCopy = new HashMap<SynthesisNode, SBOLGraph>(solution);

					currentScore = getCurrentCoveredScore(solutionCopy.values());
					solutionCopy.put(n, libGate);
					List<SynthesisNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
					solutionCopy.put(n, libGate);
					if (childrenNodes.size() > 0) {
						for (SynthesisNode child : childrenNodes) {
							solutionCopy = coverRecursive(child, matches, bestScore, currentScore, solutionCopy);
							if (solutionCopy == null) {
								break;
							}
						}
					}

					if (solutionCopy != null) {
						double score = getCurrentCoveredScore(solutionCopy.values());
						if (score < bestScore) {
							bestScore = score;
							bestSolution = solutionCopy;
						}
					}
				}
			}

		}
		return bestSolution;
	}


	private double getCurrentCoveredScore(Collection<SBOLGraph> gatesUsed) {
		double totalScore = 0;  
		for(SBOLGraph g: gatesUsed) {
			totalScore += g.getOutputNode().getScore();
		}
		return totalScore; 
	}

	public void printCoveredGates() {
		for(Map.Entry<String, Map<SynthesisNode, SBOLGraph>> solution : mappedSolution.entrySet()) {
			String specName = solution.getKey();
			Map<SynthesisNode, SBOLGraph> coveredGates = solution.getValue();
			for(SBOLGraph g : coveredGates.values()) {
				System.out.println("Circuit Name: " + specName + "Gate: " + g.getOutputNode().toString());
			}
		}
	}
	
	public SBOLDocument getSBOLfromTechMapping() throws SBOLValidationException, SBOLTechMapException {
		SBOLDocument sbolDoc = sbolUtil.createSBOLDocument();
		for(SBOLGraph specGraph : specificationGraphs) {
			Map<SynthesisNode, SBOLGraph> solution = mappedSolution.get(specGraph.getOutputNode().getModuleDefinition().getDisplayId());
			ModuleDefinition topLevelModDef = sbolDoc.createModuleDefinition(specGraph.getOutputNode().getModuleDefinition().getDisplayId() + "_solution", "1.0");
			getSBOLfromTechMap(sbolDoc, topLevelModDef, solution, specGraph.getOutputNode(), null);
		}
		return sbolDoc;
	}
	
	public void exportAsSBOLFile(String filePath, SBOLDocument doc) throws IOException, SBOLConversionException {
		SBOLWriter.write(doc, filePath + ".xml");
	}
	
	private void getSBOLfromTechMap(SBOLDocument outputDoc, ModuleDefinition circuitSolution, Map<SynthesisNode, SBOLGraph> techMapSolution, SynthesisNode startingSpecNode, FunctionalComponent circuitGateOutput) throws SBOLValidationException, SBOLTechMapException {
		//Grab the gate that matches the spec. graph
		SBOLGraph coveredLibGate = techMapSolution.get(startingSpecNode);
		SynthesisNode coveredLibGate_outputNode = coveredLibGate.getOutputNode();
		ModuleDefinition copiedGate = coveredLibGate_outputNode.getModuleDefinition();
		
		/* Copy each gate into an SBOLDocument that will act as the solution of tech. map
		 * Note, the ModuleDefinition for each library gate must be copied or else the FunctionalComponent and Interactions will be dropped from the final solution */
		SBOLDocument gateDoc = coveredLibGate_outputNode.getSBOLDocument();
		gateDoc.createRecursiveCopy(outputDoc, coveredLibGate_outputNode.getModuleDefinition());
		Module copiedGate_instance = circuitSolution.createModule(getModuleId() + "_" + copiedGate.getDisplayId(), copiedGate.getIdentity());
		
		//connect the input and output components for the gate to the ModuleDefinition that represents the solution
		FunctionalComponent subGateOuput = copiedGate.getFunctionalComponent(coveredLibGate_outputNode.getFunctionalComponent().getDisplayId());
		if(subGateOuput == null) {
			throw new SBOLTechMapException("Unable to locate this FunctionalComponent " + coveredLibGate_outputNode.getFunctionalComponent().getDisplayId() + 
					"after this information was copied from " + coveredLibGate_outputNode.getModuleDefinition().getDisplayId()); 
		}
		
		if(circuitGateOutput == null) {
			circuitGateOutput = circuitSolution.createFunctionalComponent(getFunctionalComponentId() + "_" + subGateOuput.getDisplayId(), AccessType.PUBLIC, subGateOuput.getDefinition().getIdentity(), DirectionType.INOUT);
		}
		copiedGate_instance.createMapsTo(getMapsToId(), RefinementType.USELOCAL, circuitGateOutput.getDisplayId(), subGateOuput.getDisplayId());
		
		List<SynthesisNode> specLeafNodes = getEndNodes(startingSpecNode, coveredLibGate_outputNode);
		List<SynthesisNode> libLeafNodes = getEndNodes(coveredLibGate_outputNode, coveredLibGate_outputNode);

		for(int i = 0; i < specLeafNodes.size(); i++)
		{
			SynthesisNode specLeaf = specLeafNodes.get(i);
			SynthesisNode libLeaf = libLeafNodes.get(i); 
			FunctionalComponent subGateInput = copiedGate.getFunctionalComponent(libLeaf.getFunctionalComponent().getDisplayId());
			FunctionalComponent circuitGateInput =   circuitSolution.createFunctionalComponent(getFunctionalComponentId() + "_" + subGateInput.getDisplayId(), AccessType.PUBLIC, subGateInput.getDefinition().getIdentity(), DirectionType.INOUT);
			copiedGate_instance.createMapsTo(getMapsToId(), RefinementType.USELOCAL, circuitGateInput.getDisplayId(), subGateInput.getDisplayId());
			if(techMapSolution.containsKey(specLeaf))
			{
				getSBOLfromTechMap(outputDoc, circuitSolution, techMapSolution, specLeaf, circuitGateInput);
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
				if(g1Node.getFunctionalComponent().getDefinitionURI().equals(g2Node.getFunctionalComponent().getDefinitionURI()))
					return true;
			}
		}
		return false; 
	}

	public double cost(SBOLGraph g) {
		return g.getOutputNode().getScore();
	}

	private void setAllGraphNodeScore(SBOLGraph graph, Double score) {
		for(SynthesisNode node : graph.getTopologicalSortNodes()) {
			node.setScore(score);
		}
	}

	private boolean isMatch(SynthesisNode spec, SynthesisNode lib) {
		if(lib.isRoot()) return true;
		else {
			if(spec.isRoot()) return false;
			if(getDegree(spec) != getDegree(lib)) return false;
			if(getDegree(spec) == 1) {
				SynthesisNode specChild = spec.getParents().get(0);
				SynthesisNode libChild = lib.getParents().get(0);
				return isMatch(specChild, libChild);
			}
			else {
				//NOTE: this assumes it always has at most two children due to decomposition
				SynthesisNode specChildLeft = spec.getParents().get(0);
				SynthesisNode libChildLeft = lib.getParents().get(0);
				SynthesisNode specChildRight = spec.getParents().get(1);
				SynthesisNode libChildRight = lib.getParents().get(1);
				return isMatch(specChildLeft, libChildLeft) && isMatch(specChildRight, libChildRight) || isMatch(specChildLeft, libChildRight) && isMatch(specChildRight, libChildLeft);
			}
		}
	}
	
	private String getModuleId() {
		return "M" + this.moduleCounter++;
	}
	
	private String getMapsToId() {
		return "MT" + this.mapsToCounter++;
	}

	private String getFunctionalComponentId() {
		return "FC" + this.fcCounter++;
	}
	public int getDegree(SynthesisNode node) {
		return node.getParents().size();
	}
	
	public Map<String, Map<SynthesisNode, SBOLGraph>> getMappedSolution(){
		return this.mappedSolution;
	}

	public List<SBOLGraph> getSpecification() {
		return specificationGraphs;
	}

	public List<SBOLGraph> getLibrary() {
		return libraryGraphs;
	}
}
