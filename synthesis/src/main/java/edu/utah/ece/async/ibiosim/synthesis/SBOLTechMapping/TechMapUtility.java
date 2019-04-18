package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph.Node;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GateIdentifier;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;

public class TechMapUtility {

	public static List<DecomposedGraph> createSpecificationGraphFromSBOL(SBOLDocument specificationDocument) throws SBOLTechMapException {
		// Flatten document
		List<DecomposedGraph> specification = new ArrayList<>();
		if(specificationDocument.getModuleDefinitions().size() == 1) {
			DecomposedGraph decomposedGraph = new DecomposedGraph();
			decomposedGraph.createDecomposedGraph(specificationDocument.getModuleDefinitions().iterator().next());
			specification.add(decomposedGraph);
			return specification;
		}

		ModuleDefinition toplevelCircuit = specificationDocument.getRootModuleDefinitions().iterator().next();
		Map<URI, DecomposedGraph> circuitGraphs = new HashMap<>();
		Map<URI, Node> fcMap = new HashMap<>();

		// Create SBOL Graph for each subcircuit and skip the top.
		for(ModuleDefinition circuit : specificationDocument.getModuleDefinitions()) {
			if(circuit == toplevelCircuit) {
				continue;
			}
			DecomposedGraph decomposedGraph = new DecomposedGraph();
			decomposedGraph.createDecomposedGraph(circuit);
			circuitGraphs.put(circuit.getIdentity(), decomposedGraph);
		}

		// Create functional component for each top-level protein and map them to the proteins in the subcircuits.
		for(FunctionalComponent topFC : toplevelCircuit.getFunctionalComponents()) {
			Node node = new Node(); 
			fcMap.put(topFC.getIdentity(), node);

		}
		Set<Module> subcircuits = toplevelCircuit.getModules(); //subcircuits
		for(Module circuit : subcircuits) {
			Set<MapsTo> fcMapsTos = circuit.getMapsTos();
			for(MapsTo mt : fcMapsTos) {
				FunctionalComponent mapsToProtein = (FunctionalComponent) mt.getRemote();
				if(fcMap.containsKey(mapsToProtein.getIdentity())){
					DecomposedGraph graph = circuitGraphs.get(circuit.getDefinitionIdentity());
					graph.addNode(fcMap.get(mapsToProtein.getIdentity()));
				}
			}
		}

		// Initialize SBOL graphs.
		for(Entry<URI, DecomposedGraph> entry : circuitGraphs.entrySet()) {
			DecomposedGraph sbolGraph = entry.getValue();
			sbolGraph.createDecomposedGraph(specificationDocument.getModuleDefinition(entry.getKey()));
			specification.add(sbolGraph);
			//sbolGraph.topologicalSort();
		}
		return specification;
	}

	public static List<DecomposedGraph> createLibraryGraphFromSBOL (SBOLDocument sbolDoc) throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException {
		List<DecomposedGraph> library = new ArrayList<>();
		for(ModuleDefinition gateMD : sbolDoc.getRootModuleDefinitions()){
			GateIdentifier gateType = new GateIdentifier(sbolDoc, gateMD);
			GeneticGate gate = gateType.createGate();
			DecomposedGraph decomposedGate = gate.getDecomposedGraph();
			decomposedGate.getOutputNode().setScore(getSequenceLength(gateMD));
			library.add(decomposedGate);
		}
		return library;
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