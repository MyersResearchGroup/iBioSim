package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Module;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.RefinementType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;

public class SBOLNetList {

	private TechMapSolution solution;
	private DecomposedGraph specGraph;
	private SBOLDocument sbolDoc;
	private ModuleDefinition topLevelCircuit;
	private	Map<ComponentDefinition, FunctionalComponent> topLevelIOMapping;
	private HashSet<DecomposedGraphNode> visited;

	public SBOLNetList(DecomposedGraph specGraph, TechMapSolution solution) {
		this.specGraph = specGraph;
		this.solution = solution;
		SBOLDocument doc = SBOLUtility.getSBOLUtility().createSBOLDocument();
		this.sbolDoc = doc;
		this.topLevelIOMapping = new HashMap<>();
		this.visited = new HashSet<>();
	}

	public SBOLDocument generateSbol() throws SBOLValidationException {
		topLevelCircuit = sbolDoc.createModuleDefinition(specGraph.getGraphId(), "1.0");
		
		genSBOLrecurs(specGraph.getRootNode(), getDirectionType(specGraph.getRootNode()));
		return sbolDoc;
	}

	private void genSBOLrecurs(DecomposedGraphNode node, DirectionType directionType) throws SBOLValidationException {
		GeneticGate gate = solution.getGateFromNode(node);
		if(gate == null || visited.contains(node)) {
			return;
		}
		
		//add gate to solution
		gate.getSBOLDocument().createRecursiveCopy(sbolDoc, gate.getModuleDefinition());
		ModuleDefinition gateMd = sbolDoc.getModuleDefinition(gate.getModuleDefinition().getIdentity());
		Module gateInstance = topLevelCircuit.createModule(gateMd.getDisplayId() + "_", gateMd.getIdentity());
		visited.add(node);
		
		//get next spec node
		List<DecomposedGraphNode> nextSpecNode = EndNode.getMatchingEndNodes(node, gate.getDecomposedGraph().getRootNode());
		for(DecomposedGraphNode childSpec : nextSpecNode) {
			createToplevelFc(childSpec);
		}
		mapIOSignals(gate.getListOfOutputs(), gateInstance, directionType);
		mapIOSignals(gate.getListOfInputs(), gateInstance, directionType);
		
		for(DecomposedGraphNode childSpec : nextSpecNode) {
			genSBOLrecurs(childSpec, getDirectionType(childSpec));
		}
	}
	
	private FunctionalComponent createToplevelFc(DecomposedGraphNode node) throws SBOLValidationException {
		ComponentDefinition cd = solution.getAssignedComponent(node);
		FunctionalComponent topLevelFc = null;
		if(topLevelIOMapping.containsKey(cd)) {
			topLevelFc = topLevelIOMapping.get(cd);
		}
		else {
			DirectionType directionType = getDirectionType(node);
			topLevelFc = topLevelCircuit.createFunctionalComponent(cd.getDisplayId(), AccessType.PUBLIC, cd.getIdentity(), directionType);
			topLevelIOMapping.put(cd, topLevelFc);
		}
		return topLevelFc;
	}
	
	private void mapIOSignals(List<FunctionalComponent> signals, Module gateInstance, DirectionType directionType) throws SBOLValidationException {
		for(FunctionalComponent fc : signals) {
			FunctionalComponent topLevelFc = null;
			if(topLevelIOMapping.containsKey(fc.getDefinition())) {
				topLevelFc = topLevelIOMapping.get(fc.getDefinition());
			}
			else {
				topLevelFc = topLevelCircuit.createFunctionalComponent(fc.getDisplayId(), AccessType.PUBLIC, fc.getDefinition().getIdentity(), directionType);
				topLevelIOMapping.put(fc.getDefinition(), topLevelFc);
			}
			gateInstance.createMapsTo("MT_" + fc.getDisplayId(), RefinementType.VERIFYIDENTICAL, topLevelFc.getIdentity(), fc.getIdentity());
			
		}
	}
	
	private DirectionType getDirectionType(DecomposedGraphNode node) {
		if(specGraph.isLeaf(node)) {
			return DirectionType.IN;
		}
		else if(specGraph.isRoot(node)) {
			return DirectionType.OUT;
		}
		return DirectionType.INOUT;
	}
	


}
