package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode.NodeInteractionType;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class NANDGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedNand;
	private ModuleDefinition md;
	private List<FunctionalComponent> inputs, outputs;

	public NANDGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.md = md;
	}
	
	@Override
	public GateType getType() {
		return GateType.NAND;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return sbolDoc;
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
		if (decomposedNand == null) {
			if(inputs.size() >= 2 && outputs.size() >= 1) {
				decomposedNand = createDecomposedGate();
			}
		}
		return decomposedNand;
	}
	
	private DecomposedGraph createDecomposedGate() {
		DecomposedGraph decomposedNand = new DecomposedGraph();
		DecomposedGraphNode norTuNode = new DecomposedGraphNode();
		DecomposedGraphNode notTuNode1 = new DecomposedGraphNode();
		DecomposedGraphNode notTuNode2 = new DecomposedGraphNode();
		DecomposedGraphNode notTuNode3 = new DecomposedGraphNode();
		DecomposedGraphNode internalNotOut1Node = new DecomposedGraphNode();
		DecomposedGraphNode internalNotOut2Node = new DecomposedGraphNode();
		DecomposedGraphNode internalNorOutNode = new DecomposedGraphNode();
		DecomposedGraphNode inputNode1 = new DecomposedGraphNode(inputs.get(0));
		DecomposedGraphNode inputNode2 = new DecomposedGraphNode(inputs.get(1));
		DecomposedGraphNode outputNode = new DecomposedGraphNode(outputs.get(0));
	
		decomposedNand.addAllNodes(norTuNode, notTuNode1, notTuNode2, notTuNode3,
				internalNorOutNode, internalNotOut1Node, internalNotOut2Node,
				inputNode1, inputNode2, outputNode);
	
		decomposedNand.addNodeRelationship(notTuNode1, inputNode1, NodeInteractionType.REPRESSION);
		decomposedNand.addNodeRelationship(internalNotOut1Node, notTuNode1, NodeInteractionType.PRODUCTION);

		decomposedNand.addNodeRelationship(notTuNode2, inputNode2, NodeInteractionType.REPRESSION);
		decomposedNand.addNodeRelationship(internalNotOut2Node, notTuNode2, NodeInteractionType.PRODUCTION);
		
		decomposedNand.addNodeRelationship(norTuNode, internalNotOut1Node, NodeInteractionType.REPRESSION);
		decomposedNand.addNodeRelationship(norTuNode, internalNotOut2Node, NodeInteractionType.REPRESSION);
		decomposedNand.addNodeRelationship(internalNorOutNode, norTuNode, NodeInteractionType.PRODUCTION);

		decomposedNand.addNodeRelationship(notTuNode3, internalNorOutNode, NodeInteractionType.REPRESSION);
		decomposedNand.addNodeRelationship(outputNode, notTuNode3, NodeInteractionType.PRODUCTION);
		
		decomposedNand.setNodeAsLeaf(inputNode1);
		decomposedNand.setNodeAsLeaf(inputNode2);
		decomposedNand.setNodeAsOutput(outputNode);
		return decomposedNand;
		
	}

	@Override
	public List<ComponentDefinition> getListOfInputsAsComponentDefinition() {
		List<ComponentDefinition> cdList = new ArrayList<>();
		for(FunctionalComponent fc : inputs) {
			cdList.add(fc.getDefinition());
		}
		return cdList;
	}

	@Override
	public List<ComponentDefinition> getListOfOutputsAsComponentDefinition() {
		List<ComponentDefinition> cdList = new ArrayList<>();
		for(FunctionalComponent fc : outputs) {
			cdList.add(fc.getDefinition());
		}
		return cdList;
	}

}
