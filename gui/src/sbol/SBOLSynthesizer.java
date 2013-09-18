package sbol;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.ReplacedBy;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;


import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLSynthesisMatcher;

public class SBOLSynthesizer {
	private SBOLSynthesisMatcher matcher;
	boolean boundFlag = true;
	int boundCount = 0;
//	boolean greedyFlag = false;
	int greedCap = 0;
	int greedCount = 0;
//	int solutionCount = 0;
	
	public SBOLSynthesizer(Set<SBOLSynthesisGraph> graphLibrary) {
		this.matcher = new SBOLSynthesisMatcher(graphLibrary);
	}
	
	public List<Integer> mapSpecification(SBOLSynthesisGraph spec) {
//		int numWarmUps = 0;
//		int numTrials = 1;
//		double[] times = new double[numTrials];
		
//		List<SBOLSynthesisNode> postOrderedNodes = spec.postOrderNodes();
//		int solutionCost = 0;
//		for (int i = 0; i < numWarmUps; i++) {
//			for (SBOLSynthesisNode node : postOrderedNodes) {
//				matchNode(node, spec);
//				boundNode(node, spec);
//			}
//			solution.clear();
//			solutionCost = coverSpec(spec, solution);
//		}
//		for (int i = 0; i < numTrials; i++) {
		long startTime = System.nanoTime();
		for (SBOLSynthesisNode node : spec.postOrderNodes()) {
			matchNode(node, spec);
			boundNode(node, spec);
		}
		List<Integer> solution = new LinkedList<Integer>();
		int solutionCost = coverSpec(spec, solution);
		long endTime = System.nanoTime();
		double time = (endTime - startTime)*Math.pow(10, -9); 
//			times[i] = (endTime - startTime)*Math.pow(10, -9);
//		}
//		double avgTime = 0;
//		for (int i = 0; i < times.length; i++)
//			avgTime += times[i];
//		avgTime = avgTime/numTrials;
//		double sdTime = 0;
//		for (int i = 0; i < times.length; i++)
//			sdTime += Math.pow((times[i] - avgTime), 2);
//		sdTime = Math.sqrt(sdTime/(numTrials - 1));
//		System.out.println("Average time is " + avgTime + " s.");
//		System.out.println("Standard deviation is " + sdTime + " s.");
		System.out.println("Run took " + time + " s.");
		System.out.println("Solution is " + solution + ".");
		System.out.println("Solution cost is " + solutionCost + ".");
		System.out.println("Bounded solution " + boundCount + " times.");
		return solution;
	}
	
	private void matchNode(SBOLSynthesisNode node, SBOLSynthesisGraph spec) {
		List<SBOLSynthesisGraph> matches = new LinkedList<SBOLSynthesisGraph>();
		for (String path : spec.getPaths(node)) 
			matches.addAll(matcher.match(path));
		HashMap<SBOLSynthesisGraph, Integer> matchToCount = new HashMap<SBOLSynthesisGraph, Integer>();
		for (SBOLSynthesisGraph match : matches) 
			if (matchToCount.containsKey(match))
				matchToCount.put(match, matchToCount.get(match) + 1);
			else
				matchToCount.put(match, 1);
		List<SBOLSynthesisGraph> confirmedMatches = new LinkedList<SBOLSynthesisGraph>();
		for (SBOLSynthesisGraph match : matchToCount.keySet()) {
			if (matchToCount.get(match).intValue() >= match.getPaths().size())
				confirmedMatches.add(match);
		}
		node.setMatches(confirmedMatches);
	}
	
	private void boundNode(SBOLSynthesisNode node, SBOLSynthesisGraph spec) {
		List<Integer> matchBounds = new LinkedList<Integer>();
		for (SBOLSynthesisGraph match : node.getMatches())
			matchBounds.add(boundMatch(node, match, spec));
		node.setMatchBounds(matchBounds);
		node.sortMatches();
	}
	
	private int boundMatch(SBOLSynthesisNode node, SBOLSynthesisGraph match, SBOLSynthesisGraph spec) {
		int matchBound = calculateCoverCost(match);
		List<String> matchPaths = match.getPaths();
		for (SBOLSynthesisNode boundedNode : spec.walkPaths(node, matchPaths)) {
			//			if (previousNode.getType().equals("s")) {
			matchBound += boundedNode.getCoverBound();
			//				SBOLSynthesisNode leafMatch = graphMatch.walkPath(graphMatch.getRoot(), rootPath.substring(2));
			//				if (previousCoverGraph.getRoot().getSignalComponents().size() > 0 && leafMatch.getSignalComponents().size() > 0)
			//					solutionBound -= leafMatch.getNucleotideCount();
			//			}
		}
		return matchBound;
	}
	
	private int coverSpec(SBOLSynthesisGraph spec, List<Integer> bestSolution) {
		List<Integer> solution = new LinkedList<Integer>();
		int solutionCost = 0;
		Set<String> solutionSignals = new HashSet<String>();
//		List<Integer> bestSolution = new LinkedList<Integer>();
		int bestSolutionCost = -1;
		List<SBOLSynthesisNode> previousNodes = new LinkedList<SBOLSynthesisNode>();
		List<SBOLSynthesisNode> currentNodes = new LinkedList<SBOLSynthesisNode>();
		currentNodes.add(spec.getOutput());
		do {
			if (currentNodes.get(0).getMatches().size() == 0) {
				currentNodes.remove(0);
				if (currentNodes.size() == 0 && previousNodes.size() > 0) {
					if (bestSolutionCost < 0 || solutionCost < bestSolutionCost) {
						bestSolutionCost = solutionCost;
						bestSolution.clear();
						bestSolution.addAll(solution);
						if (greedCap > 0) {
							greedCount++;
							if (greedCount == greedCap)
								return bestSolutionCost;
						}
					}
//					if (!boundFlag) {
//						solutionCount++;
//						if (solutionCount == greedCap)
//							return solutionCost;
//					}
					solutionCost = removeCoverFromSolution(previousNodes.get(0).getCover(), solution,
							solutionCost, solutionSignals);
					previousNodes.get(0).terminateBranch();
					previousNodes.remove(0);
				}
			} else if (currentNodes.get(0).branch()) {
				SBOLSynthesisGraph cover = currentNodes.get(0).getCover();
				if (!crossTalk(cover.getSignals(), solutionSignals) && 
						ioCompatible(cover.getOutput().getSignal(), currentNodes.get(0).getCoverConstraint())) { 
					if (solutionInBound(solutionCost, currentNodes, bestSolutionCost)) {
						currentNodes.get(0).setUncoveredNodes(
								new LinkedList<SBOLSynthesisNode>(currentNodes.subList(1, currentNodes.size())));
						List<SBOLSynthesisNode> nextNodes = spec.walkPaths(currentNodes.get(0), cover.getPaths());
						constrainNodes(nextNodes, cover.getInputs());
						solutionCost = addCoverToSolution(currentNodes.get(0).getCoverIndex(), cover, 
								solution, solutionCost, solutionSignals);
						previousNodes.add(0, currentNodes.remove(0));
						currentNodes.addAll(0, nextNodes);
					} else {
						boundCount++;
						currentNodes.get(0).terminateBranch();
						currentNodes.clear();
					}
				} 
			} else
				currentNodes.clear();
			if (currentNodes.size() == 0 && previousNodes.size() > 0) {
				solutionCost = removeCoverFromSolution(previousNodes.get(0).getCover(), solution, 
						solutionCost, solutionSignals);
				currentNodes.addAll(previousNodes.get(0).getUncoveredNodes());
				currentNodes.add(0, previousNodes.remove(0));
			}
		} while (currentNodes.size() > 0);
		return bestSolutionCost;
	}
	
	private int addCoverToSolution(int coverIndex, SBOLSynthesisGraph cover, List<Integer> solution, 
			int solutionCost, Set<String> solutionSignals) {
		solution.add(coverIndex);
		solutionSignals.addAll(cover.getSignals());
		return solutionCost + calculateCoverCost(cover);
	}
	
	private int removeCoverFromSolution(SBOLSynthesisGraph cover, List<Integer> solution, int solutionCost,
			Set<String> solutionSignals) {
		solution.remove(solution.size() - 1);
		solutionSignals.removeAll(cover.getSignals());
		return solutionCost - calculateCoverCost(cover);
	}
	
	private int calculateCoverCost(SBOLSynthesisGraph cover) {
//		return cover.getSignals().size();
		return cover.getNucleotideCount();
	}
	
	private boolean crossTalk(Set<String> coverSignals, Set<String> solutionSignals) {
		for (String coverSignal : coverSignals)
			if (solutionSignals.contains(coverSignal))
				return true;
		return false;
	}
	
	private boolean ioCompatible(String outputSignal, String inputSignal) {
		if (outputSignal.length() == 0 || inputSignal.length() == 0)
			return true;
		else
			return inputSignal.equals(outputSignal);
	}
	
//	private boolean solutionInBound(int solutionCost, int coverBound, int bestSolutionCost) {
//		if (!boundFlag || bestSolutionCost < 0)
//			return true;
//		else
//			return solutionCost + coverBound < bestSolutionCost;
//	}

	private boolean solutionInBound(int solutionCost, List<SBOLSynthesisNode> currentNodes, int bestSolutionCost) {
		if (!boundFlag || bestSolutionCost < 0)
			return true;
		else {
			int bestCaseCost = solutionCost;
			for (SBOLSynthesisNode currentNode : currentNodes)
				bestCaseCost = bestCaseCost + currentNode.getCoverBound();
			return (bestCaseCost < bestSolutionCost);
		}
	}
	
//	private boolean crossTalk(List<SBOLSynthesisNode> currentNodes, Set<String> solutionSignals) {
//		do 
//			do
//				if (!currentNodes.get(0).branch())
//					return true;
//			while (crossTalkHelper(currentNodes.get(0).getCover().getSignals(), solutionSignals));
//		while (crossTalkHelper2(currentNodes, solutionSignals));
//		return false;
//	}
//	private boolean crossTalkHelper2(List<SBOLSynthesisNode> currentNodes, Set<String> solutionSignals) {
//		List<SBOLSynthesisNode> crossNodes = currentNodes.subList(1, currentNodes.size());
//		for (int i = 0; i < crossNodes.size(); i++) 
//			if (crossNodes.get(i).getMatches().size() > 0) 
//				while (crossTalkHelper(currentNodes.get(0).getCover().getSignals(), 
//						crossNodes.get(i).getCover().getSignals(), solutionSignals))
//					if (!crossNodes.get(i).branch()) {
//						for (int j = 0; j < i; j++)
//							crossNodes.get(j).terminateBranch();
//						return true;
//					}
//		return false;
//	}
//	
//	private boolean crossTalkHelper(Set<String> coverSignals, Set<String> solutionSignals) {
//		for (String coverSignal : coverSignals)
//			if (solutionSignals.contains(coverSignal))
//				return true;
//		return false;
//	}
//	
//	private boolean crossTalkHelper(Set<String> coverSignals, Set<String> crossCoverSignals, Set<String> solutionSignals) {
//		for (String crossCoverSignal : crossCoverSignals)
//			if (solutionSignals.contains(crossCoverSignal) || coverSignals.contains(crossCoverSignal))
//				return true;
//		return false;
//	}
	
	private void constrainNodes(List<SBOLSynthesisNode> nodes, List<SBOLSynthesisNode> nodeCovers) {
		for (int i = 0; i < nodes.size(); i++)
			nodes.get(i).setCoverConstraint(nodeCovers.get(i).getSignal());
	}
	
	public BioModel composeModel(List<Integer> solution, SBOLSynthesisGraph spec, String projectFilePath, 
			String fileID) {
		BioModel biomodel = new BioModel(projectFilePath);
		biomodel.createSBMLDocument(fileID.replace(".xml", ""), false, false);
		List<SBOLSynthesisNode> currentNodes = new LinkedList<SBOLSynthesisNode>();
		List<SBOLSynthesisGraph> previousCovers = new LinkedList<SBOLSynthesisGraph>();
		List<Integer> inputIndices = new LinkedList<Integer>();
		int submodelIndex = 0;
		currentNodes.add(spec.getOutput());
		do {
			if (previousCovers.size() == 0) {
				submodelIndex = composeOutput(biomodel, solution, spec, currentNodes, previousCovers, inputIndices, 
						submodelIndex);
			} else if (currentNodes.get(0).getMatches().size() == 0) {
				composeInput(biomodel, currentNodes, previousCovers, inputIndices);
			} else {
				submodelIndex = composeIntermediate(biomodel, solution, spec, currentNodes, previousCovers, 
						inputIndices, submodelIndex);
			}
		} while (currentNodes.size() > 0);
		return biomodel;
	}
	
	private int composeOutput(BioModel biomodel, List<Integer> solution, SBOLSynthesisGraph spec, 
			List<SBOLSynthesisNode> currentNodes, List<SBOLSynthesisGraph> previousCovers, 
			List<Integer> inputIndices, int submodelIndex) {
		SBOLSynthesisGraph currentCover = currentNodes.get(0).getCover(solution.remove(0));
		currentCover.setSubmodelID("C" + submodelIndex);
		createSubmodel(currentCover.getSubmodelID(), currentCover.getModelID(), biomodel);
		submodelIndex++;
		Species species = createIOSpecies(currentCover.getOutput().getID(), biomodel);
		portMapIOSpecies(species, GlobalConstants.OUTPUT, currentCover.getOutput().getID(), 
				currentCover.getSubmodelID(), biomodel);
		List<SBOLSynthesisNode> nextNodes = spec.walkPaths(currentNodes.remove(0), currentCover.getPaths());
		currentNodes.addAll(0, nextNodes);
		previousCovers.add(0, currentCover);
		inputIndices.add(0, 0);
		return submodelIndex;
	}
	
	private void composeInput(BioModel biomodel, List<SBOLSynthesisNode> currentNodes, 
			List<SBOLSynthesisGraph> previousCovers, List<Integer> inputIndices) {
		SBOLSynthesisGraph previousCover = previousCovers.get(0);
		Species species = createIOSpecies(previousCover.getInput(inputIndices.get(0)).getID(), biomodel);
		portMapIOSpecies(species, GlobalConstants.INPUT, previousCover.getInput(inputIndices.get(0)).getID(), 
				previousCover.getSubmodelID(), biomodel);
		currentNodes.remove(0);
		inputIndices.add(0, inputIndices.remove(0) + 1);
		if (inputIndices.get(0) == previousCover.getInputs().size()) {
			previousCovers.remove(0);
			inputIndices.remove(0);
		}
	}
	
	private int composeIntermediate(BioModel biomodel, List<Integer> solution, SBOLSynthesisGraph spec, 
			List<SBOLSynthesisNode> currentNodes, List<SBOLSynthesisGraph> previousCovers, 
			List<Integer> inputIndices, int submodelIndex) {
		SBOLSynthesisGraph currentCover = currentNodes.get(0).getCover(solution.remove(0));
		SBOLSynthesisGraph previousCover = previousCovers.get(0);
		
		currentCover.setSubmodelID("C" + submodelIndex);
		createSubmodel(currentCover.getSubmodelID(), currentCover.getModelID(), biomodel);
		submodelIndex++;
		Species species = createInterSpecies(
				previousCover.getInput(inputIndices.get(0)).getID(), currentCover.getOutput().getID(), 
				previousCover.getInput(inputIndices.get(0)).getSignal(), currentCover.getOutput().getSignal(), 
				biomodel);
		portMapInterSpecies(species, 
				previousCover.getInput(inputIndices.get(0)).getID(), currentCover.getOutput().getID(), 
				previousCover.getInput(inputIndices.get(0)).getSignal(), currentCover.getOutput().getSignal(), 
				previousCover.getSubmodelID(), currentCover.getSubmodelID());
		List<SBOLSynthesisNode> nextNodes = spec.walkPaths(currentNodes.remove(0), currentCover.getPaths());
		currentNodes.addAll(0, nextNodes);
		inputIndices.add(0, inputIndices.remove(0) + 1);
		if (inputIndices.get(0) == previousCover.getInputs().size()) {
			previousCovers.remove(0);
			inputIndices.remove(0);
		}
		previousCovers.add(0, currentCover);
		inputIndices.add(0, 0);
		return submodelIndex;
	}
	
	private void createSubmodel(String submodelID, String modelID, BioModel biomodel) {
		BioModel subBiomodel = new BioModel(biomodel.getPath());
		subBiomodel.load(biomodel.getPath() + biomodel.getSeparator() + modelID + ".xml");
		SBMLWriter writer = new SBMLWriter();
		String sbmlStr = writer.writeSBMLToString(subBiomodel.getSBMLDocument());
		String md5 = Utility.MD5(sbmlStr);
		
		biomodel.addComponent(submodelID, modelID + ".xml", subBiomodel.IsWithinCompartment(), 
				subBiomodel.getCompartmentPorts(), -1, -1, 0, 0, md5);
	}
	
	private Species createInterSpecies(String inputSubSpeciesID, String outputSubSpeciesID, 
			String inputSubDNA, String outputSubDNA, BioModel biomodel) {
		String speciesID;
		int speciesIndex = 0;
		if (outputSubDNA.length() == 0 && inputSubDNA.length() > 0) {
			speciesID = inputSubSpeciesID;
			while (biomodel.isSIdInUse(speciesID)) {
				speciesID = inputSubSpeciesID + "_" + speciesIndex;
				speciesIndex++;
			}
		} else {
			speciesID = outputSubSpeciesID;
			while (biomodel.isSIdInUse(speciesID)) {
				speciesID = outputSubSpeciesID + "_" + speciesIndex;
				speciesIndex++;
			}
		}
		biomodel.createSpecies(speciesID, 0, 0);
		return biomodel.getSBMLDocument().getModel().getSpecies(speciesID);
	}
	
	private Species createIOSpecies(String subSpeciesID, BioModel biomodel) {
		String speciesID = subSpeciesID;
		int speciesIndex = 0;
		while (biomodel.isSIdInUse(speciesID)) {
			speciesID = subSpeciesID + "_" + speciesIndex;
			speciesIndex++;
		}
		biomodel.createSpecies(speciesID, 0, 0);
		return biomodel.getSBMLDocument().getModel().getSpecies(speciesID);
	}
	
	private void portMapInterSpecies(SBase species, String inputSubSpeciesID, String outputSubSpeciesID, 
			String inputSubDNA, String outputSubDNA, String inputSubmodelID, String outputSubmodelID) {
		CompSBasePlugin compSpecies = (CompSBasePlugin) species.getPlugin("comp");
		ReplacedBy replacement = compSpecies.createReplacedBy();
		ReplacedElement replacee = compSpecies.createReplacedElement();
		if (outputSubDNA.length() == 0 && inputSubDNA.length() > 0) {
			replacement.setSubmodelRef(inputSubmodelID);
			replacement.setPortRef(GlobalConstants.INPUT + "__" + inputSubSpeciesID);
			replacee.setSubmodelRef(outputSubmodelID);
			replacee.setPortRef(GlobalConstants.OUTPUT + "__" + outputSubSpeciesID);
		} else {
			replacement.setSubmodelRef(outputSubmodelID);
			replacement.setPortRef(GlobalConstants.OUTPUT + "__" + outputSubSpeciesID);
			replacee.setSubmodelRef(inputSubmodelID);
			replacee.setPortRef(GlobalConstants.INPUT + "__" + inputSubSpeciesID);
		}
	}
	
	private void portMapIOSpecies(SBase species, String io, String subSpeciesID, String submodelID,
			BioModel biomodel) {
		CompSBasePlugin compSpecies = (CompSBasePlugin) species.getPlugin("comp");
		ReplacedBy replacement = compSpecies.createReplacedBy();
		replacement.setSubmodelRef(submodelID);
		replacement.setPortRef(io + "__" + subSpeciesID);
		biomodel.createDirPort(species.getId(), io);
	}
	
//	private void coverSpec(SBOLSynthesisNode node, List<Integer> solution, int solutionCost, 
//	Set<String> solutionSignals, List<SBOLSynthesisNode> solutionInputs, 
//	List<Integer> bestSolution, int bestSolutionCost, SBOLSynthesisGraph spec) { 
//while (node.branch()) {
//	SBOLSynthesisGraph cover = node.getCover();
//	solutionCost += cover.getNucleotideCount();
//	if (!crossTalk(cover.getSignals(), solutionSignals) && 
//			ioCompatible(cover.getOutput().getSignal(), node.getCoverConstraint()) && 
//			solutionInBound(node.getBound(), solutionCost, bestSolutionCost)) {
//		solution.add(node.getCoverIndex());
//		List<SBOLSynthesisNode> coveredInputs = spec.walkPaths(node, cover.getPaths());
//		constrainNodes(coveredInputs, cover.getInputs());
//		coveredInputs.addAll(solutionInputs);
//		if (coveredInputs.size() > 0) {
//			solutionSignals.addAll(cover.getSignals());
//			coverSpec(coveredInputs.remove(0), solution, solutionCost, solutionSignals, 
//					coveredInputs, bestSolution, bestSolutionCost, spec);
//			solutionSignals.removeAll(cover.getSignals());
//		} else if (bestSolutionCost < 0 || solutionCost < bestSolutionCost) {
//			bestSolutionCost = solutionCost;
//			bestSolution = solution;
//			node.terminateBranch();
//		}
//		solution.remove(solution.size() - 1);
//	}
//	solutionCost -= cover.getNucleotideCount();
//}
//}

}
