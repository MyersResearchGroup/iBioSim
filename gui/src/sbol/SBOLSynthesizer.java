package sbol;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;

import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;
import sbol.SBOLSynthesisMatcher;

public class SBOLSynthesizer {
	private SBOLSynthesisMatcher matcher;
	private boolean exhaustive;
	private int solutionCap;
	private int greedCount = 0;
	
	public SBOLSynthesizer(Set<SBOLSynthesisGraph> graphLibrary, Properties synthProps) {
		this.matcher = new SBOLSynthesisMatcher(graphLibrary);
//		if (synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY).equals(
//				GlobalConstants.SBOL_SYNTH_EXHAUST_BB))
//			solutionCap = 0;
//		else
		exhaustive = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY).equals(
				GlobalConstants.SBOL_SYNTH_EXHAUST_BB);
		solutionCap = Integer.parseInt(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
	}
	
	public List<List<SBOLSynthesisGraph>> mapSpecification(SBOLSynthesisGraph spec) {
		long startTime = System.nanoTime();
		for (SBOLSynthesisNode node : spec.postOrderNodes()) {
			matchNode(node, spec);
			boundNode(node, spec);
		}
		List<List<SBOLSynthesisGraph>> solutions = new LinkedList<List<SBOLSynthesisGraph>>();
		int solutionCost = coverSpec(spec, solutions);
		long endTime = System.nanoTime();
		double time = (endTime - startTime)*Math.pow(10, -9); 

//		System.out.println("Run took " + time + " s.");
//		System.out.println("Solution is " + solution + ".");
//		System.out.println("Solution cost is " + solutionCost + ".");

		return solutions;
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
	
	private int coverSpec(SBOLSynthesisGraph spec, List<List<SBOLSynthesisGraph>> bestSolutions) {
		List<SBOLSynthesisGraph> solution = new LinkedList<SBOLSynthesisGraph>();
		int solutionCost = 0;
		int lowerBound = spec.getOutput().getCoverBound();
		Set<String> solutionSignals = new HashSet<String>();
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
						bestSolutions.add(0, new LinkedList<SBOLSynthesisGraph>(solution));
						if (exhaustive) {
							if (bestSolutions.size() > solutionCap)
								bestSolutions.remove(bestSolutions.size() - 1);
						} else if (bestSolutions.size() == solutionCap)
							return bestSolutionCost;
					}
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
						for (SBOLSynthesisNode nextNode : nextNodes)
							lowerBound = lowerBound + nextNode.getCoverBound();
						constrainNodes(nextNodes, cover.getInputs());
						solutionCost = addCoverToSolution(cover, solution, solutionCost, solutionSignals);
						lowerBound = lowerBound - currentNodes.get(0).getCoverBound();
						previousNodes.add(0, currentNodes.remove(0));
						currentNodes.addAll(0, nextNodes);
					} else {
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
//				for (SBOLSynthesisNode uncoveredNode : previousNodes.get(0).getUncoveredNodes())
//					lowerBound = lowerBound + uncoveredNode.getCoverBound();
				lowerBound = lowerBound + previousNodes.get(0).getCoverBound();
				currentNodes.add(0, previousNodes.remove(0));
			}
		} while (currentNodes.size() > 0);
		return bestSolutionCost;
	}
	
	private int addCoverToSolution(SBOLSynthesisGraph cover, List<SBOLSynthesisGraph> solution, 
			int solutionCost, Set<String> solutionSignals) {
		solution.add(cover);
		solutionSignals.addAll(cover.getSignals());
		return solutionCost + calculateCoverCost(cover);
	}
	
	private int removeCoverFromSolution(SBOLSynthesisGraph cover, List<SBOLSynthesisGraph> solution, int solutionCost,
			Set<String> solutionSignals) {
		solution.remove(solution.size() - 1);
		solutionSignals.removeAll(cover.getSignals());
		return solutionCost - calculateCoverCost(cover);
	}
	
	private int calculateCoverCost(SBOLSynthesisGraph cover) {
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

	private boolean solutionInBound(int solutionCost, List<SBOLSynthesisNode> currentNodes, int bestSolutionCost) {
		if (bestSolutionCost < 0)
			return true;
		else {
			int bestCaseCost = solutionCost;
			for (SBOLSynthesisNode currentNode : currentNodes)
				bestCaseCost = bestCaseCost + currentNode.getCoverBound();
			return (bestCaseCost < bestSolutionCost);
		}
	}
	
	private void constrainNodes(List<SBOLSynthesisNode> nodes, List<SBOLSynthesisNode> nodeCovers) {
		for (int i = 0; i < nodes.size(); i++)
			nodes.get(i).setCoverConstraint(nodeCovers.get(i).getSignal());
	}
	
	public void composeSolutionModel(List<SBOLSynthesisGraph> solution, SBOLSynthesisGraph spec, BioModel solutionModel) {
		List<SBOLSynthesisGraph> solutionCopy = new LinkedList<SBOLSynthesisGraph>();
		solutionCopy.addAll(solution);
		List<SBOLSynthesisNode> currentNodes = new LinkedList<SBOLSynthesisNode>();
		List<SBOLSynthesisGraph> previousCovers = new LinkedList<SBOLSynthesisGraph>();
		List<Integer> inputIndices = new LinkedList<Integer>();
		int submodelIndex = 0;
		currentNodes.add(spec.getOutput());
		do {
			if (previousCovers.size() == 0) {
				SBOLSynthesisGraph currentCover = solution.remove(0);
				composeOutput(currentCover, solutionModel, submodelIndex);
				submodelIndex++;
				List<SBOLSynthesisNode> nextNodes = spec.walkPaths(currentNodes.remove(0), currentCover.getPaths());
				currentNodes.addAll(0, nextNodes);
				previousCovers.add(0, currentCover);
				inputIndices.add(0, 0);
			} else if (currentNodes.get(0).getMatches().size() == 0) {
				SBOLSynthesisGraph previousCover = previousCovers.get(0);
				composeInput(previousCover, solutionModel, inputIndices);
				currentNodes.remove(0);
				inputIndices.add(0, inputIndices.remove(0) + 1);
				if (inputIndices.get(0) == previousCover.getInputs().size()) {
					previousCovers.remove(0);
					inputIndices.remove(0);
				}
			} else {
				SBOLSynthesisGraph currentCover = solution.remove(0);
				SBOLSynthesisGraph previousCover = previousCovers.get(0);
				composeIntermediate(currentCover, previousCover, solutionModel, inputIndices, submodelIndex);
				submodelIndex++;
				List<SBOLSynthesisNode> nextNodes = spec.walkPaths(currentNodes.remove(0), currentCover.getPaths());
				currentNodes.addAll(0, nextNodes);
				inputIndices.add(0, inputIndices.remove(0) + 1);
				if (inputIndices.get(0) == previousCover.getInputs().size()) {
					previousCovers.remove(0);
					inputIndices.remove(0);
				}
				previousCovers.add(0, currentCover);
				inputIndices.add(0, 0);
			}
		} while (currentNodes.size() > 0);
	}
	
	private void composeOutput(SBOLSynthesisGraph currentCover, BioModel solutionModel, int submodelIndex) {
		currentCover.setSubmodelID("C" + submodelIndex);
		createSubmodel(currentCover.getSubmodelID(), currentCover.getModelFileID(), solutionModel);
		Species species = createIOSpecies(currentCover.getOutput().getID(), solutionModel);
		portMapIOSpecies(species, GlobalConstants.OUTPUT, currentCover.getOutput().getID(), 
				currentCover.getSubmodelID(), solutionModel);
	}
	
	private void composeInput(SBOLSynthesisGraph previousCover, BioModel solutionModel, List<Integer> inputIndices) {
		Species species = createIOSpecies(previousCover.getInput(inputIndices.get(0)).getID(), solutionModel);
		portMapIOSpecies(species, GlobalConstants.INPUT, previousCover.getInput(inputIndices.get(0)).getID(), 
				previousCover.getSubmodelID(), solutionModel);
	}
	
	private void composeIntermediate(SBOLSynthesisGraph currentCover, SBOLSynthesisGraph previousCover, BioModel solutionModel, 
			List<Integer> inputIndices, int submodelIndex) {
		currentCover.setSubmodelID("C" + submodelIndex);
		createSubmodel(currentCover.getSubmodelID(), currentCover.getModelFileID(), solutionModel);
		submodelIndex++;
		Species species = createInterSpecies(
				previousCover.getInput(inputIndices.get(0)).getID(), currentCover.getOutput().getID(), 
				previousCover.getInput(inputIndices.get(0)).getSignal(), currentCover.getOutput().getSignal(), 
				solutionModel);
		portMapInterSpecies(species, 
				previousCover.getInput(inputIndices.get(0)).getID(), currentCover.getOutput().getID(), 
				previousCover.getInput(inputIndices.get(0)).getSignal(), currentCover.getOutput().getSignal(), 
				previousCover.getSubmodelID(), currentCover.getSubmodelID());
	}
	
	private void createSubmodel(String submodelID, String sbmlFileID, BioModel biomodel) {
		BioModel subBiomodel = new BioModel(biomodel.getPath());
		subBiomodel.load(biomodel.getPath() + biomodel.getSeparator() + sbmlFileID);
		SBMLWriter writer = new SBMLWriter();
		String sbmlStr = null;
		try {
			sbmlStr = writer.writeSBMLToString(subBiomodel.getSBMLDocument());
		}
		catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String md5 = Utility.MD5(sbmlStr);
		
		biomodel.addComponent(submodelID, sbmlFileID, subBiomodel.IsWithinCompartment(), 
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
		CompSBasePlugin compSpecies = SBMLutilities.getCompSBasePlugin(species);
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
		CompSBasePlugin compSpecies = SBMLutilities.getCompSBasePlugin(species);
		ReplacedBy replacement = compSpecies.createReplacedBy();
		replacement.setSubmodelRef(submodelID);
		replacement.setPortRef(io + "__" + subSpeciesID);
		biomodel.createDirPort(SBMLutilities.getId(species), io);
	}
}
