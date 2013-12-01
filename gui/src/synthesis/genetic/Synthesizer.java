package synthesis.genetic;


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

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;
import biomodel.util.Utility;
import synthesis.genetic.SynthesisMatcher;

public class Synthesizer {
	private SynthesisMatcher matcher;
	private boolean exhaustive;
	private int solutionCap;
	//private int greedCount = 0;
	
	public Synthesizer(Set<SynthesisGraph> graphLibrary, Properties synthProps) {
		this.matcher = new SynthesisMatcher(graphLibrary);
//		if (synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY).equals(
//				GlobalConstants.SBOL_SYNTH_EXHAUST_BB))
//			solutionCap = 0;
//		else
		exhaustive = synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY).equals(
				GlobalConstants.SBOL_SYNTH_EXHAUST_BB);
		solutionCap = Integer.parseInt(synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY));
	}
	
	public List<List<SynthesisGraph>> mapSpecification(SynthesisGraph spec) {
		//long startTime = System.nanoTime();
		for (SynthesisNode node : spec.postOrderNodes()) {
			matchNode(node, spec);
			boundNode(node, spec);
		}
		List<List<SynthesisGraph>> solutions = new LinkedList<List<SynthesisGraph>>();
		coverSpec(spec, solutions);
		//ong endTime = System.nanoTime();
		//double time = (endTime - startTime)*Math.pow(10, -9); 

//		System.out.println("Run took " + time + " s.");
//		System.out.println("Solution is " + solution + ".");
//		System.out.println("Solution cost is " + solutionCost + ".");

		return solutions;
	}
	
	private void matchNode(SynthesisNode node, SynthesisGraph spec) {
		List<SynthesisGraph> matches = new LinkedList<SynthesisGraph>();
		for (String path : spec.getPaths(node)) 
			matches.addAll(matcher.match(path));
		HashMap<SynthesisGraph, Integer> matchToCount = new HashMap<SynthesisGraph, Integer>();
		for (SynthesisGraph match : matches) 
			if (matchToCount.containsKey(match))
				matchToCount.put(match, matchToCount.get(match) + 1);
			else
				matchToCount.put(match, 1);
		List<SynthesisGraph> confirmedMatches = new LinkedList<SynthesisGraph>();
		for (SynthesisGraph match : matchToCount.keySet()) {
			if (matchToCount.get(match).intValue() >= match.getPaths().size())
				confirmedMatches.add(match);
		}
		node.setMatches(confirmedMatches);
	}
	
	private void boundNode(SynthesisNode node, SynthesisGraph spec) {
		List<Integer> matchBounds = new LinkedList<Integer>();
		for (SynthesisGraph match : node.getMatches())
			matchBounds.add(boundMatch(node, match, spec));
		node.setMatchBounds(matchBounds);
		node.sortMatches();
	}
	
	private int boundMatch(SynthesisNode node, SynthesisGraph match, SynthesisGraph spec) {
		int matchBound = calculateCoverCost(match);
		List<String> matchPaths = match.getPaths();
		for (SynthesisNode boundedNode : spec.walkPaths(node, matchPaths)) {
			//			if (previousNode.getType().equals("s")) {
			matchBound += boundedNode.getCoverBound();
			//				SBOLSynthesisNode leafMatch = graphMatch.walkPath(graphMatch.getRoot(), rootPath.substring(2));
			//				if (previousCoverGraph.getRoot().getSignalComponents().size() > 0 && leafMatch.getSignalComponents().size() > 0)
			//					solutionBound -= leafMatch.getNucleotideCount();
			//			}
		}
		return matchBound;
	}
	
	@SuppressWarnings("unused")
	private int coverSpec(SynthesisGraph spec, List<List<SynthesisGraph>> bestSolutions) {
		List<SynthesisGraph> solution = new LinkedList<SynthesisGraph>();
		int solutionCost = 0;
		int lowerBound = spec.getOutput().getCoverBound();
		Set<String> solutionSignals = new HashSet<String>();
		int bestSolutionCost = -1;
		List<SynthesisNode> previousNodes = new LinkedList<SynthesisNode>();
		List<SynthesisNode> currentNodes = new LinkedList<SynthesisNode>();
		currentNodes.add(spec.getOutput());
		do {
			if (currentNodes.get(0).getMatches().size() == 0) {
				currentNodes.remove(0);
				if (currentNodes.size() == 0 && previousNodes.size() > 0) {
					if (bestSolutionCost < 0 || solutionCost < bestSolutionCost) {
						bestSolutionCost = solutionCost;
						bestSolutions.add(0, new LinkedList<SynthesisGraph>(solution));
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
				SynthesisGraph cover = currentNodes.get(0).getCover();
				if (!crossTalk(cover.getSignals(), solutionSignals) && 
						ioCompatible(cover.getOutput().getSignal(), currentNodes.get(0).getCoverConstraint())) { 
					if (solutionInBound(solutionCost, currentNodes, bestSolutionCost)) {
						currentNodes.get(0).setUncoveredNodes(
								new LinkedList<SynthesisNode>(currentNodes.subList(1, currentNodes.size())));
						List<SynthesisNode> nextNodes = spec.walkPaths(currentNodes.get(0), cover.getPaths());
						for (SynthesisNode nextNode : nextNodes)
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
	
	private int addCoverToSolution(SynthesisGraph cover, List<SynthesisGraph> solution, 
			int solutionCost, Set<String> solutionSignals) {
		solution.add(cover);
		solutionSignals.addAll(cover.getSignals());
		return solutionCost + calculateCoverCost(cover);
	}
	
	private int removeCoverFromSolution(SynthesisGraph cover, List<SynthesisGraph> solution, int solutionCost,
			Set<String> solutionSignals) {
		solution.remove(solution.size() - 1);
		solutionSignals.removeAll(cover.getSignals());
		return solutionCost - calculateCoverCost(cover);
	}
	
	private int calculateCoverCost(SynthesisGraph cover) {
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
		return inputSignal.equals(outputSignal);
	}

	private boolean solutionInBound(int solutionCost, List<SynthesisNode> currentNodes, int bestSolutionCost) {
		if (bestSolutionCost < 0)
			return true;
		int bestCaseCost = solutionCost;
		for (SynthesisNode currentNode : currentNodes)
			bestCaseCost = bestCaseCost + currentNode.getCoverBound();
		return (bestCaseCost < bestSolutionCost);
	}
	
	private void constrainNodes(List<SynthesisNode> nodes, List<SynthesisNode> nodeCovers) {
		for (int i = 0; i < nodes.size(); i++)
			nodes.get(i).setCoverConstraint(nodeCovers.get(i).getSignal());
	}
	
	public void composeSolutionModel(List<SynthesisGraph> solution, SynthesisGraph spec, BioModel solutionModel) {
		List<SynthesisGraph> solutionCopy = new LinkedList<SynthesisGraph>();
		solutionCopy.addAll(solution);
		List<SynthesisNode> currentNodes = new LinkedList<SynthesisNode>();
		List<SynthesisGraph> previousCovers = new LinkedList<SynthesisGraph>();
		List<Integer> inputIndices = new LinkedList<Integer>();
		int submodelIndex = 0;
		currentNodes.add(spec.getOutput());
		do {
			if (previousCovers.size() == 0) {
				SynthesisGraph currentCover = solution.remove(0);
				composeOutput(currentCover, solutionModel, submodelIndex);
				submodelIndex++;
				List<SynthesisNode> nextNodes = spec.walkPaths(currentNodes.remove(0), currentCover.getPaths());
				currentNodes.addAll(0, nextNodes);
				previousCovers.add(0, currentCover);
				inputIndices.add(0, 0);
			} else if (currentNodes.get(0).getMatches().size() == 0) {
				SynthesisGraph previousCover = previousCovers.get(0);
				composeInput(previousCover, solutionModel, inputIndices);
				currentNodes.remove(0);
				inputIndices.add(0, inputIndices.remove(0) + 1);
				if (inputIndices.get(0) == previousCover.getInputs().size()) {
					previousCovers.remove(0);
					inputIndices.remove(0);
				}
			} else {
				SynthesisGraph currentCover = solution.remove(0);
				SynthesisGraph previousCover = previousCovers.get(0);
				composeIntermediate(currentCover, previousCover, solutionModel, inputIndices, submodelIndex);
				submodelIndex++;
				List<SynthesisNode> nextNodes = spec.walkPaths(currentNodes.remove(0), currentCover.getPaths());
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
	
	private void composeOutput(SynthesisGraph currentCover, BioModel solutionModel, int submodelIndex) {
		currentCover.setSubmodelID("C" + submodelIndex);
		createSubmodel(currentCover.getSubmodelID(), currentCover.getModelFileID(), solutionModel);
		Species species = createIOSpecies(currentCover.getOutput().getID(), solutionModel);
		portMapIOSpecies(species, GlobalConstants.OUTPUT, currentCover.getOutput().getID(), 
				currentCover.getSubmodelID(), solutionModel);
	}
	
	private void composeInput(SynthesisGraph previousCover, BioModel solutionModel, List<Integer> inputIndices) {
		Species species = createIOSpecies(previousCover.getInput(inputIndices.get(0)).getID(), solutionModel);
		portMapIOSpecies(species, GlobalConstants.INPUT, previousCover.getInput(inputIndices.get(0)).getID(), 
				previousCover.getSubmodelID(), solutionModel);
	}
	
	private void composeIntermediate(SynthesisGraph currentCover, SynthesisGraph previousCover, BioModel solutionModel, 
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
