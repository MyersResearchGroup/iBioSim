package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.MapsTo;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

public class TechMapUtility {

	public static List<SBOLGraph> createSpecificationGraphFromSBOL(SBOLDocument specificationDocument) throws SBOLTechMapException {
		// Flatten document
		List<SBOLGraph> specification = new ArrayList<>();
		if(specificationDocument.getModuleDefinitions().size() == 1) {
			SBOLGraph sbolGraph = new SBOLGraph();
			specification.add(sbolGraph);
			sbolGraph.initializeGraph(specificationDocument, specificationDocument.getModuleDefinitions().iterator().next());
			sbolGraph.topologicalSort();
			return specification;
		}

		ModuleDefinition toplevelCircuit = specificationDocument.getRootModuleDefinitions().iterator().next();
		Map<URI, SBOLGraph> circuitGraphs = new HashMap<>();
		Map<URI, FunctionalComponentNode> fcMap = new HashMap<>();

		// Create SBOL Graph for each subcircuit and skip the top.
		for(ModuleDefinition circuit : specificationDocument.getModuleDefinitions()) {
			if(circuit == toplevelCircuit) {
				continue;
			}

			SBOLGraph sbolGraph = new SBOLGraph();
			circuitGraphs.put(circuit.getIdentity(), sbolGraph);
		}

		// Create functional component for each top-level protein and map them to the proteins in the subcircuits.
		for(FunctionalComponent topFC : toplevelCircuit.getFunctionalComponents()) {
			FunctionalComponentNode node = new FunctionalComponentNode(specificationDocument, topFC);
			fcMap.put(topFC.getIdentity(), node);

		}
		Set<Module> subcircuits = toplevelCircuit.getModules(); //subcircuits
		for(Module circuit : subcircuits) {
			Set<MapsTo> fcMapsTos = circuit.getMapsTos();
			for(MapsTo mt : fcMapsTos) {
				FunctionalComponent mapsToProtein = (FunctionalComponent) mt.getRemote();
				if(fcMap.containsKey(mapsToProtein.getIdentity())){
					SBOLGraph graph = circuitGraphs.get(circuit.getDefinitionIdentity());
					graph.addFunctionalComponentNode(fcMap.get(mapsToProtein.getIdentity()), mt.getLocalIdentity());
				}
			}
		}

		// Initialize SBOL graphs.
		for(Entry<URI, SBOLGraph> entry : circuitGraphs.entrySet()) {
			SBOLGraph sbolGraph = entry.getValue();
			specification.add(sbolGraph);
			sbolGraph.initializeGraph(specificationDocument, specificationDocument.getModuleDefinition(entry.getKey()));
			sbolGraph.topologicalSort();
		}
		return specification;
	}

	public static List<SBOLGraph> createLibraryGraphFromSBOL (SBOLDocument sbolDoc) throws SBOLTechMapException {
		List<SBOLGraph> library = new ArrayList<SBOLGraph>();
		for(ModuleDefinition gateMD : sbolDoc.getRootModuleDefinitions()){
			SBOLGraph sbolGraph = new SBOLGraph();
			sbolGraph.initializeGraph(sbolDoc, gateMD);
			sbolGraph.topologicalSort();
			setLibraryGateScores(sbolGraph);
			library.add(sbolGraph);
		}
		return library;
	}

	private static void setLibraryGateScores (SBOLGraph g) {
		int totalScore = 0;
		for(FunctionalComponentNode node : g.getAllNodes()) {
			int score = node.getFlattenedSequence().length() - 1;
			node.setScore(g.getModuleDefinition(), score);
			totalScore += score;
		}
		g.setScore(totalScore);

	}
}