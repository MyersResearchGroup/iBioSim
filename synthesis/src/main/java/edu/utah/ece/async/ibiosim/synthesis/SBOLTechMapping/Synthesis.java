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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.SBOLGraph;


/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Synthesis
{
	private static List<SBOLGraph> _libraryGraph; 
	private static SBOLGraph _specificationGraph; 
	private static SBOLUtility sbolUtil;
	private static Map<FunctionalComponentNode, SBOLGraph> bestSolution;

	public Synthesis()
	{
		sbolUtil = SBOLUtility.getSBOLUtility();
		_libraryGraph = new ArrayList<SBOLGraph>();
		_specificationGraph = null; 
		bestSolution = new HashMap<>();
	}


//	public void createSBOLGraph(SBOLDocument sbolDoc, boolean isLibraryFile) throws SBOLTechMapException
//	{
//		for(ModuleDefinition m : sbolDoc.getModuleDefinitions())
//		{
//			SBOLGraph sbolGraph = new SBOLGraph();
//			sbolGraph.initializeGraph(sbolDoc, m);
//			sbolGraph.topologicalSort();
//			if(isLibraryFile)
//				_libraryGraph.add(sbolGraph);
//			else
//				_specificationGraph = sbolGraph; 
//		}
//
//	}
//
//	public void createSBOLGraph(File library, File specification, String defaultURIPrefix) throws SBOLValidationException, IOException, SBOLConversionException, SBOLTechMapException
//	{
//		_libraryGraph.add(createSBOLGraph(library, defaultURIPrefix));
//		_specificationGraph = createSBOLGraph(specification, defaultURIPrefix);
//	}
//
//	public SBOLGraph createSBOLGraph(File fileName, String defaultURIPrefix) throws SBOLValidationException, IOException, SBOLConversionException, SBOLTechMapException{
//		SBOLDocument sbolDoc = SBOLReader.read(fileName);
//		sbolDoc.setDefaultURIprefix(defaultURIPrefix);
//		SBOLGraph sbolGraph = new SBOLGraph();
//		for(ModuleDefinition m : sbolDoc.getModuleDefinitions())
//		{
//			sbolGraph.initializeGraph(sbolDoc, m);
//			sbolGraph.topologicalSort(); 
//		}
//		return sbolGraph;
//	}
//
//	public void setLibraryGateScores(List<SBOLGraph> library)
//	{
//		for(SBOLGraph graph: library)
//		{
//			int totalScore = 0;
//			for(FunctionalComponentNode node : graph.getAllNodes()) {
//				int score = node.getFlattenedSequence().length() - 1;
//				node.setScore(score);
//				totalScore += score;
//			}
//			graph.setScore(totalScore);
//		}
//	}
//
//	public void match_topLevel(SBOLGraph _specifiGraph, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches)
//	{
//		//Map<SynthesisNode, LinkedList<SBOLGraph>> matches = new HashMap<SynthesisNode, LinkedList<SBOLGraph>>();
//		setAllGraphNodeScore(_specifiGraph, Double.POSITIVE_INFINITY);
//		List<FunctionalComponentNode> s = _specifiGraph.getTopologicalSortNodes();
//
//		for(FunctionalComponentNode n: s) //go through each species node in speciGraph
//		{
//			if(n.isRoot())
//			{
//				n.setScore(0);
//				matches.put(n, new LinkedList<WeightedGraph>());
//			}
//			else
//			{
//				//SynthesisNode bestLib = null;
//				double totalScore;
//				for(SBOLGraph gate : _libraryGraph)
//				{
//					FunctionalComponentNode l = gate.getOutputNode();
//					if(isMatch(n, l))
//					{
//						totalScore = l.getScore() + getSubNodeScore(n, l);
//						if(totalScore < n.getScore()) 
//						{
//							n.setScore(totalScore); //update speciGraph with new libGate score
//
//							if(!matches.containsKey(n))
//							{
//								matches.put(n, new LinkedList<WeightedGraph>());
//							}
//							matches.get(n).addFirst(new WeightedGraph(gate, totalScore));
//						}
//						else
//						{
//							if(!matches.containsKey(n))
//							{
//								matches.put(n, new LinkedList<WeightedGraph>());
//								matches.get(n).add(new WeightedGraph(gate, totalScore));
//							}
//							else
//							{
//								//find the correct location to put the gate such that it is in ascending order
//								//base off of score values
//								//Assuming every time add new gate to list, the list should be already ordered
//								LinkedList<WeightedGraph> list = matches.get(n);
//
//								for(int i = list.size()-1; i >= 0; i--)
//								{
//									if(list.get(i).getWeight() <= totalScore)
//									{
//										int index = i + 1; 
//										list.add(index, new WeightedGraph(gate, totalScore));
//										break;
//									}
//
//								}
//							} //end of ordering else check
//						}
//					}
//				}
//			}
//		} //end of for loop
//	}
//
//
//	public double getSubNodeScore(FunctionalComponentNode spec, FunctionalComponentNode lib)
//	{
//		if(lib.isRoot())
//			return spec.getScore();
//		double total = 0;
//		for(int i=0; i<lib.getParents().size(); i++)
//		{
//			total += getSubNodeScore(spec.getParents().get(i), lib.getParents().get(i));
//		}
//		return total;
//	}
//
//	/**
//	 * Return a list of leaf nodes from the spec that maps to the specified library gate
//	 * @param spec
//	 * @param lib
//	 * @return
//	 */
//	public List<FunctionalComponentNode> getEndNodes(FunctionalComponentNode spec, FunctionalComponentNode lib)
//	{
//		List<FunctionalComponentNode> list = new ArrayList<FunctionalComponentNode>();
//		getNodes(spec, lib, list);
//		return list;
//
//	}
//
//
//	private void getNodes(FunctionalComponentNode spec, FunctionalComponentNode lib, List<FunctionalComponentNode> nodes)
//	{
//		if(lib.isRoot())
//			nodes.add(spec);
//		else
//		{
//			for(int i=0; i<lib.getParents().size(); i++)
//			{
//				getNodes(spec.getParents().get(i), lib.getParents().get(i), nodes);
//			}
//		}
//	}
//	
//	public void matchAndCover() {
//		Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches = new HashMap<FunctionalComponentNode, LinkedList<WeightedGraph>>();
//		match_topLevel(getSpecification(), matches);
//		Map<FunctionalComponentNode, SBOLGraph> solution = cover_topLevel(getSpecification(), matches);
//		this.bestSolution = solution;
//	}
//
//	public Map<FunctionalComponentNode, SBOLGraph> cover_topLevel(SBOLGraph syn, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches)
//	{
//		// 0. Set up solution of gates used for spec
//		double bestScore = Double.POSITIVE_INFINITY;
//		double currentScore = 0;
//		FunctionalComponentNode n = syn.getOutputNode();
//		Map<FunctionalComponentNode, SBOLGraph> bestSolution = cover(syn, n, matches, bestScore, currentScore);
//		return bestSolution; 
//	}
//
//	private Map<FunctionalComponentNode, SBOLGraph> cover(SBOLGraph syn, FunctionalComponentNode n, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore)
//	{
//		LinkedList<WeightedGraph> matchedLibGates = matches.get(n);
//		Map<FunctionalComponentNode, SBOLGraph> bestSolution = null;
//
//		for (WeightedGraph wg : matchedLibGates)
//		{
//			SBOLGraph libGate = wg.getSBOLGraph();
//			double estimateScore = libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());
//
//			if (estimateScore >= bestScore)
//			{
//				continue;
//			}
//			else
//			{
//				Map<FunctionalComponentNode, SBOLGraph> solutionCopy = new HashMap<FunctionalComponentNode, SBOLGraph>();
//				solutionCopy.put(n, libGate);
//				double score = libGate.getOutputNode().getScore();
//				solutionCopy.put(n, libGate);
//				List<FunctionalComponentNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
//				if (childrenNodes.size() > 0)
//				{
//					for (FunctionalComponentNode child : childrenNodes)
//					{
//						solutionCopy = coverRecursive(child, matches, bestScore, currentScore, solutionCopy);
//					}
//				}
//
//				if (solutionCopy != null)
//				{
//					score = getCurrentCoveredScore(solutionCopy.values());
//
//					if (score < bestScore)
//					{
//						bestScore = score;
//						bestSolution = solutionCopy;
//					}
//				}
//
//			}
//		}
//
//		return bestSolution;
//	}
//
//	private Map<FunctionalComponentNode, SBOLGraph> coverRecursive(FunctionalComponentNode n, Map<FunctionalComponentNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore, Map<FunctionalComponentNode, SBOLGraph> solution)
//	{
//		LinkedList<WeightedGraph> matchedLibGates = matches.get(n);
//		Map<FunctionalComponentNode, SBOLGraph> bestSolution = null;
//		if (n.isRoot())
//		{
//			return solution;
//		}
//		for (WeightedGraph wg : matchedLibGates)
//		{
//			SBOLGraph libGate = wg.getSBOLGraph();
//			if (isCrossTalk(solution.values(), libGate))
//			{
//				continue;
//			}
//			else
//			{
//				double estimateScore = currentScore + libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());
//
//				if (estimateScore >= bestScore)
//				{
//					continue;
//				}
//				else
//				{
//					Map<FunctionalComponentNode, SBOLGraph> solutionCopy = new HashMap<FunctionalComponentNode, SBOLGraph>(solution);
//
//					currentScore = getCurrentCoveredScore(solutionCopy.values());
//					solutionCopy.put(n, libGate);
//					List<FunctionalComponentNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
//					solutionCopy.put(n, libGate);
//					if (childrenNodes.size() > 0)
//					{
//						for (FunctionalComponentNode child : childrenNodes)
//						{
//
//							solutionCopy = coverRecursive(child, matches, bestScore, currentScore, solutionCopy);
//							if (solutionCopy == null)
//							{
//								break;
//							}
//						}
//					}
//
//					if (solutionCopy != null)
//					{
//						double score = getCurrentCoveredScore(solutionCopy.values());
//						if (score < bestScore)
//						{
//							bestScore = score;
//							bestSolution = solutionCopy;
//						}
//					}
//
//				}
//			}
//
//		}
//		return bestSolution;
//	}
//
//
//	private double getCurrentCoveredScore(Collection<SBOLGraph> gatesUsed)
//	{
//		double totalScore = 0;  //TODO: Should this be init. to 0? 
//		for(SBOLGraph g: gatesUsed)
//		{
//			totalScore += g.getOutputNode().getScore();
//		}
//		return totalScore; 
//	}
//
//	public static void printCoveredGates(Map<FunctionalComponentNode, SBOLGraph> coveredGates)
//	{
//		for(SBOLGraph g : coveredGates.values())
//		{
//			System.out.println(g.getOutputNode().toString());
//		}
//	}
//	
//	public void exportAsSBOLFile(String filePath, SBOLDocument doc) throws FileNotFoundException, SBOLConversionException {
//		sbolUtil.writeSBOLDocument(filePath, doc);
//	}
//
//	public SBOLDocument getSBOLfromTechMapping() throws SBOLValidationException
//	{
//		//Set up SBOLDocument to write into
//		SBOLDocument sbolDoc = sbolUtil.createSBOLDocument();
//
//		ModuleDefinition topLevelModDef = sbolDoc.createModuleDefinition("TechMapSolution_ModDef");
//		getSBOLfromTechMap(null, sbolDoc, topLevelModDef, getBestSolution(), getSpecification().getOutputNode());
//
//		return sbolDoc;
//
//	}
//	
//	public Map<FunctionalComponentNode, SBOLGraph> getBestSolution(){
//		return this.bestSolution;
//	}
//
//	private void getSBOLfromTechMap(FunctionalComponent comp, SBOLDocument sbolDoc, ModuleDefinition solModDef, Map<FunctionalComponentNode, SBOLGraph> solution, FunctionalComponentNode specNode) throws SBOLValidationException
//	{
//		//Grab the gate that matches the spec. graph
//		SBOLGraph coveredLibGate = solution.get(specNode);
//
//		FunctionalComponentNode gateOutNode = coveredLibGate.getOutputNode();
//		SBOLDocument gateSBOLDoc = gateOutNode.getSBOLDocument();
//		ModuleDefinition gateMD = gateOutNode.getModuleDefinition();
//
//		//Copy each gate into an SBOLDocument that will act as the solution of tech. map
//		gateSBOLDoc.createRecursiveCopy(sbolDoc, gateMD);
//
//		//Create an SBOL Module for every gate mapped to the solution
//		Module gateModule = solModDef.createModule(gateMD.getDisplayId()+ "_module", gateMD.getIdentity());
//
//		// Tech. map solution is converted to SBOL data format from output node and traverses downward to input nodes
//		// of the design specification. If comp is null, then conversion is at output node.
//		if(comp == null)
//		{
//			ComponentDefinition gateOutCD = gateOutNode.getComponentDefinition();
//			String fc_id = gateOutCD.getDisplayId();
//			comp = solModDef.createFunctionalComponent(fc_id + "_FunctionalComponent", AccessType.PUBLIC, gateOutCD.getIdentity(), DirectionType.INOUT);
//		}
//
//		URI libGateURI = gateOutNode.getFunctionalComponent().getIdentity();
//		gateModule.createMapsTo(gateModule.getDisplayId()+"_outputMapsTo", RefinementType.USEREMOTE, comp.getIdentity(), libGateURI);
//
//
//		List<FunctionalComponentNode> specLeafNodes = getEndNodes(specNode, gateOutNode);
//		List<FunctionalComponentNode> libLeafNodes = getEndNodes(gateOutNode, gateOutNode);
//
//		for(int i = 0; i < specLeafNodes.size(); i++)
//		{
//			FunctionalComponentNode specLeaf = specLeafNodes.get(i);
//			FunctionalComponentNode libLeaf = libLeafNodes.get(i); 
//			if(solution.containsKey(specLeaf))
//			{
//				ComponentDefinition leafGateCD = libLeaf.getComponentDefinition();
//				FunctionalComponent leafGateFC = libLeaf.getFunctionalComponent();
//				String fc_id = leafGateCD.getDisplayId();
//				FunctionalComponent topFC = solModDef.createFunctionalComponent(fc_id + "_FunctionalComponent", AccessType.PUBLIC, leafGateCD.getIdentity(), DirectionType.INOUT);
//				gateModule.createMapsTo(gateModule.getDisplayId() + "_MapsTo" + i, RefinementType.USELOCAL, topFC.getIdentity(), leafGateFC.getIdentity());
//
//				getSBOLfromTechMap(topFC, sbolDoc, solModDef, solution, specLeaf);
//
//			}
//		}
//	}
//
//	private boolean isCrossTalk(Collection<SBOLGraph> gatesUsed, SBOLGraph gate)
//	{
//		for(SBOLGraph g: gatesUsed)
//		{
//			if(isGateMatch(g, gate))
//			{
//				return true;
//			}
//		}
//		return false; 
//	}
//
//	private boolean isGateMatch(SBOLGraph g1, SBOLGraph g2)
//	{
//		for(FunctionalComponentNode g1Node: g1.getTopologicalSortNodes())
//		{
//			for(FunctionalComponentNode g2Node: g2.getTopologicalSortNodes())
//			{
//				if(g1Node.getFunctionalComponent().getDefinitionURI().equals(g2Node.getFunctionalComponent().getDefinitionURI()))
//					return true;
//			}
//		}
//		return false; 
//	}
//
//	public double cost(SBOLGraph g)
//	{
//		return g.getOutputNode().getScore();
//	}
//
//	private void setAllGraphNodeScore(SBOLGraph graph, Double score)
//	{
//		for(FunctionalComponentNode node : graph.getTopologicalSortNodes())
//		{
//			node.setScore(score);
//		}
//	}
//
//	private boolean isMatch(FunctionalComponentNode spec, FunctionalComponentNode lib)
//	{
//		if(lib.isRoot()) return true;
//		else
//		{
//			if(spec.isRoot()) return false;
//			if(getDegree(spec) != getDegree(lib)) return false;
//			if(getDegree(spec) == 1)
//			{
//				FunctionalComponentNode specChild = spec.getParents().get(0);
//				FunctionalComponentNode libChild = lib.getParents().get(0);
//				return isMatch(specChild, libChild);
//			}
//			else
//			{
//				//NOTE: this assumes it always has at most two children due to decomposition
//				FunctionalComponentNode specChildLeft = spec.getParents().get(0);
//				FunctionalComponentNode libChildLeft = lib.getParents().get(0);
//				FunctionalComponentNode specChildRight = spec.getParents().get(1);
//				FunctionalComponentNode libChildRight = lib.getParents().get(1);
//				return isMatch(specChildLeft, libChildLeft) && isMatch(specChildRight, libChildRight) || isMatch(specChildLeft, libChildRight) && isMatch(specChildRight, libChildLeft);
//			}
//		}
//	}
//
//	//	public List<SynthesisNode> topologicalSort(SBOLGraph graph)
//	//	{
//	//		List<SynthesisNode> sortedElements = new ArrayList<SynthesisNode>();
//	//		Queue<SynthesisNode> unsortedElements = new LinkedList<SynthesisNode>();
//	//		unsortedElements.addAll(graph.getRoots());
//	//
//	//		while(!unsortedElements.isEmpty())
//	//		{
//	//			SynthesisNode n = unsortedElements.poll();
//	//			if(sortedElements.contains(n))
//	//				continue;
//	//			sortedElements.add(n);
//	//			for(SynthesisNode m: n.getChildren()) 
//	//			{
//	//				if(m.getParents().size() == 1)
//	//				{
//	//					unsortedElements.add(m.getChildren().get(0));
//	//					break;
//	//				}
//	//				else if(m.getParents().size() == 2)//assume 2 input into promoter
//	//				{
//	//					List<SynthesisNode> parentNodes = m.getParents(); 
//	//					if(parentNodes.contains(n) && parentNodes.contains(unsortedElements.peek()))
//	//					{
//	//						SynthesisNode temp = unsortedElements.poll();
//	////						sortedElements.add(n);
//	//						sortedElements.add(temp);
//	//						unsortedElements.add(m.getChildren().get(0));
//	//
//	//					}
//	//					else
//	//					{
//	//						unsortedElements.add(n);
//	//					}
//	//				}
//	//			} //end of for loop
//	//		} //end of while loop
//	//
//	//		return sortedElements;
//	//	}
//
//	public int getDegree(FunctionalComponentNode node)
//	{
//		return node.getParents().size();
//	}
//
//	public SBOLGraph getSpecification()
//	{
//		return _specificationGraph;
//	}
//
//	public List<SBOLGraph> getLibrary()
//	{
//		return _libraryGraph;
//	}
}
