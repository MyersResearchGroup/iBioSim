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

	public Synthesis()
	{
		_libraryGraph = new ArrayList<SBOLGraph>();
		_specificationGraph = null; 
	}


	public void createSBOLGraph(SBOLDocument sbolDoc, boolean isLibraryFile)
	{
		for(ModuleDefinition m : sbolDoc.getModuleDefinitions())
		{
			SBOLGraph sbolGraph = new SBOLGraph();
			sbolGraph.createGraph(sbolDoc, m);
			sbolGraph.topologicalSort();
			if(isLibraryFile)
				_libraryGraph.add(sbolGraph);
			else
				_specificationGraph = sbolGraph; 
		}

	}

	public void createSBOLGraph(File library, File specification, String defaultURIPrefix) throws SBOLValidationException, IOException, SBOLConversionException
	{
		_libraryGraph.add(createSBOLGraph(library, defaultURIPrefix));
		_specificationGraph = createSBOLGraph(specification, defaultURIPrefix);
	}

	public SBOLGraph createSBOLGraph(File fileName, String defaultURIPrefix) throws SBOLValidationException, IOException, SBOLConversionException{
		SBOLDocument sbolDoc = SBOLReader.read(fileName);
		sbolDoc.setDefaultURIprefix(defaultURIPrefix);
		SBOLGraph sbolGraph = new SBOLGraph();
		for(ModuleDefinition m : sbolDoc.getModuleDefinitions())
		{
			sbolGraph.createGraph(sbolDoc, m);
			sbolGraph.topologicalSort(); 
		}
		return sbolGraph;
	}

	public void setLibraryGateScores(List<SBOLGraph> library)
	{
		for(SBOLGraph g: library)
		{
			SynthesisNode node = g.getOutputNode();
			int score = node.getSequence().length() - 1;
			node.setScore(score);
		}
	}

	public void match_topLevel(SBOLGraph _specifiGraph, Map<SynthesisNode, LinkedList<WeightedGraph>> matches)
	{
		//Map<SynthesisNode, LinkedList<SBOLGraph>> matches = new HashMap<SynthesisNode, LinkedList<SBOLGraph>>();
		setAllGraphNodeScore(_specifiGraph, Double.POSITIVE_INFINITY);
		List<SynthesisNode> s = _specifiGraph.getTopologicalSortNodes();

		for(SynthesisNode n: s) //go through each species node in speciGraph
		{
			if(n.isRoot())
			{
				n.setScore(0);
				matches.put(n, new LinkedList<WeightedGraph>());
			}
			else
			{
				//SynthesisNode bestLib = null;
				double totalScore;
				for(SBOLGraph gate : _libraryGraph)
				{
					SynthesisNode l = gate.getOutputNode();
					if(isMatch(n, l))
					{
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
						else
						{
							if(!matches.containsKey(n))
							{
								matches.put(n, new LinkedList<WeightedGraph>());
								matches.get(n).add(new WeightedGraph(gate, totalScore));
							}
							else
							{
								//find the correct location to put the gate such that it is in ascending order
								//base off of score values
								//Assuming every time add new gate to list, the list should be already ordered
								LinkedList<WeightedGraph> list = matches.get(n);

								for(int i = list.size()-1; i >= 0; i--)
								{
									if(list.get(i).getWeight() <= totalScore)
									{
										int index = i + 1; 
										list.add(index, new WeightedGraph(gate, totalScore));
										break;
									}

								}
							} //end of ordering else check
						}
					}
				}
			}
		} //end of for loop
	}


	public double getSubNodeScore(SynthesisNode spec, SynthesisNode lib)
	{
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
	public List<SynthesisNode> getEndNodes(SynthesisNode spec, SynthesisNode lib)
	{
		List<SynthesisNode> list = new ArrayList<SynthesisNode>();
		getNodes(spec, lib, list);
		return list;

	}


	private void getNodes(SynthesisNode spec, SynthesisNode lib, List<SynthesisNode> nodes)
	{
		if(lib.isRoot())
			nodes.add(spec);
		else
		{
			for(int i=0; i<lib.getParents().size(); i++)
			{
				getNodes(spec.getParents().get(i), lib.getParents().get(i), nodes);
			}
		}
	}

	public Map<SynthesisNode, SBOLGraph> cover_topLevel(SBOLGraph syn, Map<SynthesisNode, LinkedList<WeightedGraph>> matches)
	{
		// 0. Set up solution of gates used for spec
		double bestScore = Double.POSITIVE_INFINITY;
		double currentScore = 0;
		SynthesisNode n = syn.getOutputNode();
		Map<SynthesisNode, SBOLGraph> bestSolution = cover(syn, n, matches, bestScore, currentScore);
		return bestSolution; 
	}

	private Map<SynthesisNode, SBOLGraph> cover(SBOLGraph syn, SynthesisNode n, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore)
	{
		LinkedList<WeightedGraph> matchedLibGates = matches.get(n);
		Map<SynthesisNode, SBOLGraph> bestSolution = null;

		for (WeightedGraph wg : matchedLibGates)
		{
			SBOLGraph libGate = wg.getSBOLGraph();
			double estimateScore = libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());

			if (estimateScore >= bestScore)
			{
				continue;
			}
			else
			{
				Map<SynthesisNode, SBOLGraph> solutionCopy = new HashMap<SynthesisNode, SBOLGraph>();
				solutionCopy.put(n, libGate);
				double score = libGate.getOutputNode().getScore();
				solutionCopy.put(n, libGate);
				List<SynthesisNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
				if (childrenNodes.size() > 0)
				{
					for (SynthesisNode child : childrenNodes)
					{
						solutionCopy = coverRecursive(child, matches, bestScore, currentScore, solutionCopy);
					}
				}

				if (solutionCopy != null)
				{
					score = getCurrentCoveredScore(solutionCopy.values());

					if (score < bestScore)
					{
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
		if (n.isRoot())
		{
			return solution;
		}
		for (WeightedGraph wg : matchedLibGates)
		{
			SBOLGraph libGate = wg.getSBOLGraph();
			if (isCrossTalk(solution.values(), libGate))
			{
				continue;
			}
			else
			{
				double estimateScore = currentScore + libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());

				if (estimateScore >= bestScore)
				{
					continue;
				}
				else
				{
					Map<SynthesisNode, SBOLGraph> solutionCopy = new HashMap<SynthesisNode, SBOLGraph>(solution);

					currentScore = getCurrentCoveredScore(solutionCopy.values());
					solutionCopy.put(n, libGate);
					List<SynthesisNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
					solutionCopy.put(n, libGate);
					if (childrenNodes.size() > 0)
					{
						for (SynthesisNode child : childrenNodes)
						{

							solutionCopy = coverRecursive(child, matches, bestScore, currentScore, solutionCopy);
							if (solutionCopy == null)
							{
								break;
							}
						}
					}

					if (solutionCopy != null)
					{
						double score = getCurrentCoveredScore(solutionCopy.values());
						if (score < bestScore)
						{
							bestScore = score;
							bestSolution = solutionCopy;
						}
					}

				}
			}

		}
		return bestSolution;
	}


	private double getCurrentCoveredScore(Collection<SBOLGraph> gatesUsed)
	{
		double totalScore = 0;  //TODO: Should this be init. to 0? 
		for(SBOLGraph g: gatesUsed)
		{
			totalScore += g.getOutputNode().getScore();
		}
		return totalScore; 
	}

	public static void printCoveredGates(Map<SynthesisNode, SBOLGraph> coveredGates)
	{
		for(SBOLGraph g : coveredGates.values())
		{
			System.out.println(g.getOutputNode().toString());
		}
	}

	public SBOLDocument getSBOLfromTechMapping(Map<SynthesisNode, SBOLGraph> solution, SBOLGraph specificationGraph, String defaultURIPrefix) throws SBOLValidationException
	{
		//Set up SBOLDocument to write into
		SBOLDocument sbolDoc = new SBOLDocument();
		sbolDoc.setDefaultURIprefix(defaultURIPrefix);

		ModuleDefinition topLevelModDef = sbolDoc.createModuleDefinition("TechMapSolution_ModDef");
		getSBOLfromTechMap(null, sbolDoc, topLevelModDef, solution, specificationGraph.getOutputNode());

		return sbolDoc;

	}

	private void getSBOLfromTechMap(FunctionalComponent comp, SBOLDocument sbolDoc, ModuleDefinition solModDef, Map<SynthesisNode, SBOLGraph> solution, SynthesisNode specNode) throws SBOLValidationException
	{
		//Grab the gate that matches the spec. graph
		SBOLGraph coveredLibGate = solution.get(specNode);

		SynthesisNode gateOutNode = coveredLibGate.getOutputNode();
		SBOLDocument gateSBOLDoc = gateOutNode.getSBOLDocument();
		ModuleDefinition gateMD = gateOutNode.getModuleDefinition();

		//Copy each gate into an SBOLDocument that will act as the solution of tech. map
		gateSBOLDoc.createRecursiveCopy(sbolDoc, gateMD);

		//Create an SBOL Module for every gate mapped to the solution
		Module gateModule = solModDef.createModule(gateMD.getDisplayId()+ "_module", gateMD.getIdentity());

		// Tech. map solution is converted to SBOL data format from output node and traverses downward to input nodes
		// of the design specification. If comp is null, then conversion is at output node.
		if(comp == null)
		{
			ComponentDefinition gateOutCD = gateOutNode.getComponentDefinition();
			String fc_id = gateOutCD.getDisplayId();
			comp = solModDef.createFunctionalComponent(fc_id + "_FunctionalComponent", AccessType.PUBLIC, gateOutCD.getIdentity(), DirectionType.INOUT);
		}

		URI libGateURI = gateOutNode.getFunctionalComponent().getIdentity();
		gateModule.createMapsTo(gateModule.getDisplayId()+"_outputMapsTo", RefinementType.USEREMOTE, comp.getIdentity(), libGateURI);


		List<SynthesisNode> specLeafNodes = getEndNodes(specNode, gateOutNode);
		List<SynthesisNode> libLeafNodes = getEndNodes(gateOutNode, gateOutNode);

		for(int i = 0; i < specLeafNodes.size(); i++)
		{
			SynthesisNode specLeaf = specLeafNodes.get(i);
			SynthesisNode libLeaf = libLeafNodes.get(i); 
			if(solution.containsKey(specLeaf))
			{
				ComponentDefinition leafGateCD = libLeaf.getComponentDefinition();
				FunctionalComponent leafGateFC = libLeaf.getFunctionalComponent();
				String fc_id = leafGateCD.getDisplayId();
				FunctionalComponent topFC = solModDef.createFunctionalComponent(fc_id + "_FunctionalComponent", AccessType.PUBLIC, leafGateCD.getIdentity(), DirectionType.INOUT);
				gateModule.createMapsTo(gateModule.getDisplayId() + "_MapsTo" + i, RefinementType.USELOCAL, topFC.getIdentity(), leafGateFC.getIdentity());

				getSBOLfromTechMap(topFC, sbolDoc, solModDef, solution, specLeaf);

			}
		}
	}

	private boolean isCrossTalk(Collection<SBOLGraph> gatesUsed, SBOLGraph gate)
	{
		for(SBOLGraph g: gatesUsed)
		{
			if(isGateMatch(g, gate))
			{
				return true;
			}
		}
		return false; 
	}

	private boolean isGateMatch(SBOLGraph g1, SBOLGraph g2)
	{
		for(SynthesisNode g1Node: g1.getTopologicalSortNodes())
		{
			for(SynthesisNode g2Node: g2.getTopologicalSortNodes())
			{
				if(g1Node.getFunctionalComponent().getDefinitionURI().equals(g2Node.getFunctionalComponent().getDefinitionURI()))
					return true;
			}
		}
		return false; 
	}

	public double cost(SBOLGraph g)
	{
		return g.getOutputNode().getScore();
	}

	private void setAllGraphNodeScore(SBOLGraph graph, Double score)
	{
		for(SynthesisNode node : graph.getTopologicalSortNodes())
		{
			node.setScore(score);
		}
	}

	private boolean isMatch(SynthesisNode spec, SynthesisNode lib)
	{
		if(lib.isRoot()) return true;
		else
		{
			if(spec.isRoot()) return false;
			if(getDegree(spec) != getDegree(lib)) return false;
			if(getDegree(spec) == 1)
			{
				SynthesisNode specChild = spec.getParents().get(0);
				SynthesisNode libChild = lib.getParents().get(0);
				return isMatch(specChild, libChild);
			}
			else
			{
				//NOTE: this assumes it always has at most two children due to decomposition
				SynthesisNode specChildLeft = spec.getParents().get(0);
				SynthesisNode libChildLeft = lib.getParents().get(0);
				SynthesisNode specChildRight = spec.getParents().get(1);
				SynthesisNode libChildRight = lib.getParents().get(1);
				return isMatch(specChildLeft, libChildLeft) && isMatch(specChildRight, libChildRight) || isMatch(specChildLeft, libChildRight) && isMatch(specChildRight, libChildLeft);
			}
		}
	}

	//	public List<SynthesisNode> topologicalSort(SBOLGraph graph)
	//	{
	//		List<SynthesisNode> sortedElements = new ArrayList<SynthesisNode>();
	//		Queue<SynthesisNode> unsortedElements = new LinkedList<SynthesisNode>();
	//		unsortedElements.addAll(graph.getRoots());
	//
	//		while(!unsortedElements.isEmpty())
	//		{
	//			SynthesisNode n = unsortedElements.poll();
	//			if(sortedElements.contains(n))
	//				continue;
	//			sortedElements.add(n);
	//			for(SynthesisNode m: n.getChildren()) 
	//			{
	//				if(m.getParents().size() == 1)
	//				{
	//					unsortedElements.add(m.getChildren().get(0));
	//					break;
	//				}
	//				else if(m.getParents().size() == 2)//assume 2 input into promoter
	//				{
	//					List<SynthesisNode> parentNodes = m.getParents(); 
	//					if(parentNodes.contains(n) && parentNodes.contains(unsortedElements.peek()))
	//					{
	//						SynthesisNode temp = unsortedElements.poll();
	////						sortedElements.add(n);
	//						sortedElements.add(temp);
	//						unsortedElements.add(m.getChildren().get(0));
	//
	//					}
	//					else
	//					{
	//						unsortedElements.add(n);
	//					}
	//				}
	//			} //end of for loop
	//		} //end of while loop
	//
	//		return sortedElements;
	//	}

	public int getDegree(SynthesisNode node)
	{
		return node.getParents().size();
	}

	public SBOLGraph getSpecification()
	{
		return _specificationGraph;
	}

	public List<SBOLGraph> getLibrary()
	{
		return _libraryGraph;
	}
}
