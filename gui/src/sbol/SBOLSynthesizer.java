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
	
	public SBOLSynthesizer(Set<SBOLSynthesisGraph> graphLibrary) {
		this.matcher = new SBOLSynthesisMatcher(graphLibrary);
	}
	
	public List<Integer> mapSpecification(SBOLSynthesisGraph spec) {
		for (SBOLSynthesisNode node : spec.postOrderNodes()) {
			matchNode(node, spec);
			boundNode(node, spec);
		}
		return coverSpec(spec);
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
		int matchBound = match.getNucleotideCount();
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
	
	private List<Integer> coverSpec(SBOLSynthesisGraph spec) {
		List<Integer> solution = new LinkedList<Integer>();
		int solutionCost = 0;
		Set<String> solutionSignals = new HashSet<String>();
		List<Integer> bestSolution = new LinkedList<Integer>();
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
						bestSolution = new LinkedList<Integer>(solution);
					}
					solutionCost = removeLastCoverFromSolution(previousNodes.remove(0).getCover(), solution,
							solutionCost, solutionSignals);
				}
			} else if (currentNodes.get(0).branch()) {
				SBOLSynthesisGraph cover = currentNodes.get(0).getCover();
				if (!crossTalk(cover.getSignals(), solutionSignals) && 
						ioCompatible(cover.getOutput().getSignal(), currentNodes.get(0).getCoverConstraint())) { 
					if (solutionInBound(solutionCost, currentNodes.get(0).getCoverBound(), bestSolutionCost)) {
						currentNodes.get(0).setUncoveredNodes(
								new LinkedList<SBOLSynthesisNode>(currentNodes.subList(1, currentNodes.size())));
						List<SBOLSynthesisNode> nextNodes = spec.walkPaths(currentNodes.get(0), cover.getPaths());
						constrainNodes(nextNodes, cover.getInputs());
						solutionCost = addCoverToSolution(currentNodes.get(0).getCoverIndex(), cover, 
								solution, solutionCost, solutionSignals);
						previousNodes.add(0, currentNodes.remove(0));
						currentNodes.addAll(0, nextNodes);
					} else
						currentNodes.clear();
				}
			} else
				currentNodes.clear();
			if (currentNodes.size() == 0 && previousNodes.size() > 0) {
				solutionCost = removeLastCoverFromSolution(previousNodes.get(0).getCover(), solution, 
						solutionCost, solutionSignals);
				currentNodes.addAll(previousNodes.get(0).getUncoveredNodes());
				currentNodes.add(0, previousNodes.remove(0));
			}
		} while (currentNodes.size() > 0);
		return bestSolution;
	}
	
	private int addCoverToSolution(int coverIndex, SBOLSynthesisGraph cover, List<Integer> solution, 
			int solutionCost, Set<String> solutionSignals) {
		solution.add(coverIndex);
		solutionSignals.addAll(cover.getSignals());
		return solutionCost + cover.getNucleotideCount();
	}
	
	private int removeLastCoverFromSolution(SBOLSynthesisGraph lastCover, List<Integer> solution, int solutionCost,
			Set<String> solutionSignals) {
		solution.remove(solution.size() - 1);
		solutionSignals.removeAll(lastCover.getSignals());
		return solutionCost - lastCover.getNucleotideCount();
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
	
	private boolean solutionInBound(int solutionCost, int coverBound, int bestSolutionCost) {
		if (bestSolutionCost < 0)
			return true;
		else
			return solutionCost + coverBound < bestSolutionCost;
	}
	
	private void constrainNodes(List<SBOLSynthesisNode> nodes, List<SBOLSynthesisNode> nodeCovers) {
		for (int i = 0; i < nodes.size(); i++)
			nodes.get(i).setCoverConstraint(nodeCovers.get(i).getSignal());
	}
	
	public BioModel composeModel(List<Integer> solution, SBOLSynthesisGraph spec, String projectDirectory, 
			String fileID) {
		BioModel biomodel = new BioModel(projectDirectory);
		biomodel.createSBMLDocument(fileID.replace(".xml", ""), false, false);
		List<SBOLSynthesisNode> currentNodes = new LinkedList<SBOLSynthesisNode>();
		List<SBOLSynthesisGraph> previousCovers = new LinkedList<SBOLSynthesisGraph>();
		List<Integer> inputIndices = new LinkedList<Integer>();
		int submodelIndex = 1;
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
	
	private Species createInterSpecies(String inputSpeciesID, String outputSpeciesID, 
			String inputDNA, String outputDNA, BioModel biomodel) {
		String speciesID;
		if (outputDNA.length() == 0 && inputDNA.length() > 0) 
			speciesID = inputSpeciesID;
		else 
			speciesID = outputSpeciesID;
		int speciesIndex = 0;
		while (biomodel.isSIdInUse(speciesID))
			speciesID = speciesID + "_" + speciesIndex;
		biomodel.createSpecies(speciesID, 0, 0);
		return biomodel.getSBMLDocument().getModel().getSpecies(speciesID);
	}
	
	private Species createIOSpecies(String SpeciesID, BioModel biomodel) {
		int speciesIndex = 0;
		while (biomodel.isSIdInUse(SpeciesID))
			SpeciesID = SpeciesID + "_" + speciesIndex;
		biomodel.createSpecies(SpeciesID, 0, 0);
		return biomodel.getSBMLDocument().getModel().getSpecies(SpeciesID);
	}
	
	private void portMapInterSpecies(SBase species, String inputSpeciesID, String outputSpeciesID, 
			String inputDNA, String outputDNA, String inputSubmodelID, String outputSubmodelID) {
		CompSBasePlugin compSpecies = (CompSBasePlugin) species.getPlugin("comp");
		ReplacedBy replacement = compSpecies.createReplacedBy();
		ReplacedElement replacee = compSpecies.createReplacedElement();
		if (outputDNA.length() == 0 && inputDNA.length() > 0) {
			replacement.setSubmodelRef(inputSubmodelID);
			replacement.setPortRef("input__" + inputSpeciesID);
			replacee.setSubmodelRef(outputSubmodelID);
			replacee.setPortRef("output__" + outputSpeciesID);
		} else {
			replacement.setSubmodelRef(outputSubmodelID);
			replacement.setPortRef("output__" + outputSpeciesID);
			replacee.setSubmodelRef(inputSubmodelID);
			replacee.setPortRef("input__" + inputSpeciesID);
		}
	}
	
	private void portMapIOSpecies(SBase species, String io, String speciesID, String submodelID,
			BioModel biomodel) {
		CompSBasePlugin compSpecies = (CompSBasePlugin) species.getPlugin("comp");
		ReplacedBy replacement = compSpecies.createReplacedBy();
		replacement.setSubmodelRef(submodelID);
		replacement.setPortRef(io + "__" + speciesID);
		biomodel.createDirPort(speciesID, io);
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
