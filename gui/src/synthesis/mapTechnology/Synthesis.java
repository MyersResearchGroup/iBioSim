package synthesis.mapTechnology;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;

public class Synthesis
{
	private static List<SBOLGraph> _libraryGraph; 
	private static SBOLGraph _specificationGraph; 

	public Synthesis()
	{
		_libraryGraph = new ArrayList<SBOLGraph>();
		_specificationGraph = null; 
	}

	//	public void addLibraryGraph(SBOLGraph lib)
	//	{
	//		_libraryGraph.add(lib);
	//	}
	//	
	//	public void setSpecificationGraph(SBOLGraph graph)
	//	{
	//		_specificationGraph = graph;
	//	}

	public void createSBOLGraph(File fileName, boolean isLibraryFile)
	{
		SBOLReader sbolFile = new SBOLReader();
		try
		{
			SBOLDocument sbolDoc = sbolFile.read(fileName);
			for(ModuleDefinition m : sbolDoc.getModuleDefinitions())
			{
				SBOLGraph sbolGraph = new SBOLGraph();
				sbolGraph.createGraph(m);
				sbolGraph.topologicalSort();
				if(isLibraryFile)
					_libraryGraph.add(sbolGraph);
				else
					_specificationGraph = sbolGraph; 
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	//	public boolean match(SBOLGraph spec, SBOLGraph lib)
	//	{	
	//		if(spec.getRoots().size() != lib.getRoots().size())
	//		{
	//			return false;
	//		}
	//		return isMatch(spec.getRoots().get(0), lib.getRoots().get(0));
	//	}

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
				SynthesisNode bestLib = null;
				double totalScore;
				for(SBOLGraph gate : _libraryGraph)
				{
					SynthesisNode l = gate.getOutputNode();
					if(isMatch(n, l))
					{
						totalScore = l.getScore() + getSubNodeScore(n, l);
						//System.out.println(n.getFunctionalComponent().getDisplayId() +"/" + l.toString() + "/" + totalScore);
						if(totalScore < n.getScore()) 
						{
							n.setScore(totalScore); //update speciGraph with new libGate score
							//match(o) = <01, I1, I2, I3, I4>
							if(!matches.containsKey(n))
							{
								matches.put(n, new LinkedList<WeightedGraph>());
							}
							matches.get(n).addFirst(new WeightedGraph(gate, totalScore));
							//bestLib = l;
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
								WeightedGraph temp = list.getFirst();
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
		//		System.out.println("Score: " + _specifiGraph.getOutputNode().getScore());
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

	public void cover_topLevel(SynthesisNode n, Map<SynthesisNode, LinkedList<WeightedGraph>> matches)
	{
		//0. Set up solution of gates used for spec
		double bestScore = Double.POSITIVE_INFINITY;
		double currentScore = 0; 
		double estimateScore = 0; 
		Map<SynthesisNode, SBOLGraph> solution = new HashMap<SynthesisNode, SBOLGraph>();
		Map<SynthesisNode, SBOLGraph> bestSolution = new HashMap<SynthesisNode, SBOLGraph>();
		cover(n, matches, bestScore, currentScore, estimateScore, solution, bestSolution);
		printCoveredGates(solution);
	}

	private void cover(SynthesisNode n, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, double bestScore, double currentScore, double estimateScore, Map<SynthesisNode, SBOLGraph> solution, Map<SynthesisNode, SBOLGraph> bestSolution)
	{
		//1. get all gates that can fit this node
		LinkedList<WeightedGraph> matchedLibGates = matches.get(n);

		for(WeightedGraph wg: matchedLibGates)
		{
			SBOLGraph libGate = wg.getSBOLGraph();
			//2. Check to see if graph will cause crosstalk in sol. list before adding to sol. list
			if(isCrossTalk(solution.values(), libGate))
			{
				//3. If there are crosstalk, move onto new lib gate
				continue;
			}
			else
			{
				//3.1 Crosstalk did not occur. Calculate the estimate score
				estimateScore = currentScore + libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());
				
				//				estimateScore = getCurrentCoveredScore(solution.values()) + libGate.getOutputNode().getScore() + getSubNodeScore(n, libGate.getOutputNode());

				//3.2 Check if the currentScore is smaller than the best solution score so far
				//		- if currentScore is bigger, move onto next gate in lib
				//		- else add lib gate to the solution for the current spec node that you are at
				if(estimateScore >= bestScore)
					continue;
				else//(estimateScore < bestScore)
				{
					currentScore = getCurrentCoveredScore(solution.values());
					solution.put(n, libGate);
					//5. Check if there are any nodes left to cover in the spec
					List<SynthesisNode> childrenNodes = getEndNodes(n, libGate.getOutputNode());
					if(childrenNodes.size() > 0 )
					{
						//6. get the children nodes that were not covered and continue covering the remaining nodes of the spec
						for(SynthesisNode child : childrenNodes)
						{
							cover(child, matches, bestScore, currentScore, estimateScore, solution, bestSolution);
						}
//						bestScore = currentScore;
					}
					//spec is done traversing. 
					//move onto next lib gate and see if that gate can beat this bestScore
					//	- store the currentScore as the bestScore
					//	- store the solution of the library gates used for the spec
					bestScore = getCurrentCoveredScore(solution.values());
					bestSolution = solution; 
					if(Math.abs(bestScore - 115) < 1e-5)
					{
						System.out.println("stop here");
					}
				}
				//TODO: if no solution is found, return empty solution and INFINITY score
			}

		}
		System.out.println("Best Score: " + bestScore);
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

	public Map<SynthesisNode, SBOLGraph> cover(SynthesisNode n, Map<SynthesisNode, LinkedList<WeightedGraph>> matches, Map<SynthesisNode, SBOLGraph> coveredGates, double best)
	{
		double scoreEst, currentCostCovered;
		Map<SynthesisNode, SBOLGraph> bestCover = null; 
		//get all library gates that matches specification top node
		for(WeightedGraph m : matches.get(n))
		{
			//check if library gate crosstalk with any of the gate(s) already used for spec.
			if(isCrossTalk(coveredGates.values(), m.getSBOLGraph()))
				continue;

			Map<SynthesisNode, SBOLGraph> temp = new HashMap<SynthesisNode, SBOLGraph>(coveredGates);
			currentCostCovered = 0; 
			for(SBOLGraph g : temp.values())
			{
				currentCostCovered += g.getOutputNode().getScore();
			}
			scoreEst = currentCostCovered + m.getSBOLGraph().getOutputNode().getScore() + getSubNodeScore(n, m.getSBOLGraph().getOutputNode());
			if(scoreEst < best)
			{
				temp.put(n, m.getSBOLGraph());
				List<SynthesisNode> uncoveredNodeList = getEndNodes(n, m.getSBOLGraph().getOutputNode());
				if(uncoveredNodeList.size() > 0)
				{
					for(SynthesisNode node: uncoveredNodeList)
					{
						bestCover = cover(node, matches, temp, best);
					}
				}
				else
				{
					best = scoreEst; 
					bestCover = temp;
				}
			}

		}

		return bestCover;
	}


	private boolean isCrossTalk(Collection<SBOLGraph> gatesUsed, SBOLGraph gate)
	{
		//crosstalk occurs when any species contain same componentDefinition uri
		// TODO: does crosstalk occur when you have same species & PROMOTER or only species?
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
				//				System.out.println(g1Node.getComponentDefinition().getIdentity() + "/" + g2Node.getComponentDefinition().getIdentity());
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
