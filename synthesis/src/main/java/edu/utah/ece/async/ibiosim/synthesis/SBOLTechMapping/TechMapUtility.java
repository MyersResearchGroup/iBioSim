package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GateIdentifier;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTSUPPORTEDGate;

/**
 * A utility class to create the primary information needed before performing technology mapping.
 * @author Tramy Nguyen
 */
public class TechMapUtility {

	public static DecomposedGraph createSpecificationGraphFromSBOL(SBOLDocument specificationDocument) throws SBOLTechMapException {
		NOTSUPPORTEDGate specCircuit = new NOTSUPPORTEDGate(specificationDocument, specificationDocument.getModuleDefinitions().iterator().next());
		return specCircuit.getDecomposedGraph();
	}
	
	public static List<GeneticGate> createLibraryGraphFromSbolList (List<SBOLDocument> listOfSbolDoc) throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException {
		List<GeneticGate> library = new ArrayList<>();
		for(SBOLDocument doc : listOfSbolDoc) {
			List<GeneticGate> gates = createLibraryGraphFromSBOL(doc);
			library.addAll(gates);
		}
		return library;
	}

	public static List<GeneticGate> createLibraryGraphFromSBOL (SBOLDocument sbolDoc) throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException {
		List<GeneticGate> library = new ArrayList<>();
		for(ModuleDefinition gateMD : sbolDoc.getRootModuleDefinitions()){
			GateIdentifier gateIdentifier = new GateIdentifier(sbolDoc, gateMD);
			GeneticGate gate = gateIdentifier.getIdentifiedGate();
			if(gate instanceof NOTSUPPORTEDGate) {
				
			}
			else {
				DecomposedGraph decomposedGate = gate.getDecomposedGraph();
				decomposedGate.getRootNode().setScore(getSequenceLength(gateMD));
				library.add(gate);
			}
		}
		return library;
	}

	public static void addPreAssignComponentDefinition(DecomposedGraph specGraph, Map<String, String> preselection) {
		for(String nodeId : preselection.keySet()) {
			DecomposedGraphNode specNode = specGraph.getNodeByFunctionalComponent(URI.create(nodeId));
			if(specNode != null) {
				specNode.setPreselectedComponentDefinition(URI.create(preselection.get(nodeId)));
			}
		}
	}

	private static int getSequenceLength(ModuleDefinition md) {
		int totalScore = 0;
		for(FunctionalComponent fc : md.getFunctionalComponents()) {

			ComponentDefinition cd = fc.getDefinition();
			for(Sequence seq : cd.getSequences()) {
				totalScore += seq.getElements().length();
			}
		}
		return totalScore;
	}


}