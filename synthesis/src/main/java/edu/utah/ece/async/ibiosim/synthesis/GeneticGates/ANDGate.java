package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode.NodeInteractionType;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class ANDGate implements GeneticGate {
	
	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedAnd;
	private ModuleDefinition md;
	private List<FunctionalComponent> inputs, outputs;
	
	public ANDGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.md = md;
	}
	
	@Override
	public GateType getType() {
		return GateType.AND;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

	@Override
	public void addInputMolecule(FunctionalComponent inputMolecule) {
		inputs.add(inputMolecule);
	}

	@Override
	public void addOutputMolecule(FunctionalComponent outputMolecule) {
		outputs.add(outputMolecule);
	}

	@Override
	public ModuleDefinition getModuleDefinition() {
		return this.md;
	}

	@Override
	public List<FunctionalComponent> getListOfInputs() {
		return inputs;
	}

	@Override
	public List<FunctionalComponent> getListOfOutputs() {
		return outputs;
	}

	@Override
	public boolean containsInput(FunctionalComponent fc) {
		if(inputs.isEmpty()) {
			return false;
		}
		return inputs.contains(fc); 
	}

	@Override
	public boolean containsOutput(FunctionalComponent fc) {
		if(outputs.isEmpty()) {
			return false;
		}
		return outputs.contains(fc);
	}

	@Override
	public DecomposedGraph getDecomposedGraph() {
		if (decomposedAnd == null) {
			if(inputs.size() >= 2 && outputs.size() >= 1) {
				decomposedAnd = createDecomposedGate();
			}
		}
		return decomposedAnd;
	}

	private DecomposedGraph createDecomposedGate() {
		DecomposedGraph decomposedAnd = new DecomposedGraph();
		DecomposedGraphNode norTuNode = new DecomposedGraphNode();
		DecomposedGraphNode notTuNode1 = new DecomposedGraphNode();
		DecomposedGraphNode notTuNode2 = new DecomposedGraphNode();
		DecomposedGraphNode internalNotOut1Node = new DecomposedGraphNode();
		DecomposedGraphNode internalNotOut2Node = new DecomposedGraphNode();
		DecomposedGraphNode inputNode1 = new DecomposedGraphNode(inputs.get(0).getIdentity(), inputs.get(0).getDefinition());
		DecomposedGraphNode inputNode2 = new DecomposedGraphNode(inputs.get(1).getIdentity(), inputs.get(1).getDefinition());
		DecomposedGraphNode outputNode = new DecomposedGraphNode(outputs.get(0).getIdentity(), outputs.get(0).getDefinition());

		decomposedAnd.addAllNodes(inputNode1, inputNode2, outputNode, norTuNode, notTuNode1, notTuNode2);
	
		decomposedAnd.addNodeRelationship(notTuNode1, inputNode1, NodeInteractionType.REPRESSION);
		decomposedAnd.addNodeRelationship(internalNotOut1Node, notTuNode1, NodeInteractionType.PRODUCTION);

		decomposedAnd.addNodeRelationship(notTuNode2, inputNode2, NodeInteractionType.REPRESSION);
		decomposedAnd.addNodeRelationship(internalNotOut2Node, notTuNode2, NodeInteractionType.PRODUCTION);
		
		decomposedAnd.addNodeRelationship(norTuNode, internalNotOut1Node, NodeInteractionType.REPRESSION);
		decomposedAnd.addNodeRelationship(norTuNode, internalNotOut2Node, NodeInteractionType.REPRESSION);
		decomposedAnd.addNodeRelationship(outputNode, norTuNode, NodeInteractionType.PRODUCTION);
		
		decomposedAnd.setNodeAsLeaf(inputNode1);
		decomposedAnd.setNodeAsLeaf(inputNode2);
		decomposedAnd.setNodeAsOutput(outputNode);
		return decomposedAnd;
	}
	

}
