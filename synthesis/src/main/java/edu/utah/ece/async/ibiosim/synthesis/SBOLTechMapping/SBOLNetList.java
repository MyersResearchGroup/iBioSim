package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
	private int mapsToCounter, fcCounter, moduleCounter;


	public SBOLNetList(DecomposedGraph specGraph, TechMapSolution solution) {
		this.specGraph = specGraph;
		this.solution = solution; 

	}

	public List<GateConnection> getTopLevelConnections() throws SBOLValidationException {
		List<GateConnection> gateConnections = new ArrayList<>();
		Map<ComponentDefinition, TopLevelConnection> topLevelIOMapping = new HashMap<>();
		HashSet<DecomposedGraphNode> visited = new HashSet<>();
		Queue<DecomposedGraphNode> queue = new LinkedList<>();
		DecomposedGraphNode currNode = specGraph.getRootNode();
		queue.add(currNode);

		while(!queue.isEmpty())
		{
			currNode = queue.poll();
			if(visited.contains(currNode)) {
				continue;
			}
			visited.add(currNode);
			GeneticGate gate = solution.getGateFromNode(currNode);
			if(gate != null) {
				GateConnection gc = new GateConnection(gate);
				
				for(FunctionalComponent fc : gate.getListOfInputs()) {
					ComponentDefinition cd = fc.getDefinition();
					if(topLevelIOMapping.containsKey(cd)) {

						TopLevelConnection topLevelFc = topLevelIOMapping.get(cd);
						if(topLevelFc.dirType == DirectionType.OUT) {
							topLevelFc.dirType = DirectionType.INOUT;
						}
						gc.addConnection(topLevelFc);
						topLevelFc.addGateFc(gate, fc);
					}
					else {
						TopLevelConnection connection = new TopLevelConnection(cd, DirectionType.IN);
						topLevelIOMapping.put(cd, connection);
						gc.addConnection(connection);
						connection.addGateFc(gate, fc);
					}
				}

				for(FunctionalComponent fc : gate.getListOfOutputs()) {
					ComponentDefinition cd = fc.getDefinition();
					if(topLevelIOMapping.containsKey(cd)) {
						TopLevelConnection topLevelFc = topLevelIOMapping.get(cd);
						if(topLevelFc.dirType == DirectionType.IN) {
							topLevelFc.dirType = DirectionType.INOUT;
						}
						gc.addConnection(topLevelFc);
						topLevelFc.addGateFc(gate, fc);
					}
					else {
						TopLevelConnection connection = new TopLevelConnection(cd, DirectionType.OUT);
						topLevelIOMapping.put(cd, connection);
						gc.addConnection(connection);
						connection.addGateFc(gate, fc);
					}
				}
				List<DecomposedGraphNode> nextSpecNode = EndNode.getMatchingEndNodes(currNode, gate.getDecomposedGraph().getRootNode());
				for(DecomposedGraphNode n : nextSpecNode) {
					queue.add(n);
				}
				gateConnections.add(gc);
			}
			
		}
		return gateConnections;
		
	}

	public SBOLDocument generateSbol() throws SBOLValidationException {
		SBOLDocument doc = SBOLUtility.getSBOLUtility().createSBOLDocument();
		ModuleDefinition topLevelCircuit = doc.createModuleDefinition(specGraph.getGraphId(), "1.0");
		
		Map<TopLevelConnection, FunctionalComponent> fcMap = new HashMap<>();
		List<GateConnection> listOfGateConnections = getTopLevelConnections();

		for(GateConnection gc : listOfGateConnections) {
			GeneticGate gate = gc.gate;
			gate.getSBOLDocument().createRecursiveCopy(doc, gate.getModuleDefinition());
			ModuleDefinition gateMd = doc.getModuleDefinition(gate.getModuleDefinition().getIdentity());
			Module gateInstance = topLevelCircuit.createModule(getModuleCounter() + gateMd.getDisplayId(), gateMd.getIdentity());
			
			List<TopLevelConnection> connections = gc.connections;
			for(TopLevelConnection c : connections) {
				if(!fcMap.containsKey(c)) {
					fcMap.put(c, topLevelCircuit.createFunctionalComponent(getFcCounter() + c.cd.getDisplayId(), AccessType.PUBLIC, c.cd.getIdentity(), c.dirType));
						
				}
				FunctionalComponent topFc = fcMap.get(c);
				gateInstance.createMapsTo(getMapsToCounter() + c.cd.getDisplayId(), RefinementType.VERIFYIDENTICAL, topFc.getIdentity(), c.gateFcMap.get(gate).getIdentity());
			}
			
			
		}
		
		return doc;

	}
	


	private String getFcCounter() {
		return "FC" + this.fcCounter++ + "_";
	}

	private String getMapsToCounter() {
		return "MT" + this.mapsToCounter++ + "_";
	}

	private String getModuleCounter() {
		return "M" + this.moduleCounter++ + "_";
	}
	
	private class GateConnection{
		GeneticGate gate;
		List<TopLevelConnection> connections;
		
		GateConnection(GeneticGate g){
			this.gate = g;
			this.connections = new ArrayList<>();
		}
		
		void addConnection(TopLevelConnection c) {
			connections.add(c);
		}
	}

	private class TopLevelConnection{
		ComponentDefinition cd;
		DirectionType dirType;
		Map<GeneticGate, FunctionalComponent> gateFcMap;
		
		TopLevelConnection(ComponentDefinition cd, DirectionType dirType){
			this.cd = cd;
			this.dirType = dirType;
			this.gateFcMap = new HashMap<>();
		}
		
		void addGateFc(GeneticGate gate, FunctionalComponent fc) {
			this.gateFcMap.put(gate, fc);
		}
	}

}
