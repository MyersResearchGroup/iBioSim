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
public class ORGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedOr;
	private ModuleDefinition md;
	private List<FunctionalComponent> inputs, outputs;
	
	public ORGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.md = md;
	}
	
	@Override
	public GateType getType() {
		return GateType.OR;
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
		if (decomposedOr == null) {
			if(inputs.size() >= 2 && outputs.size() >= 1) {
				decomposedOr = createDecomposedGate();
			}
		}
		return decomposedOr;
	}
	
	private DecomposedGraph createDecomposedGate() {
		DecomposedGraph decomposedOR = new DecomposedGraph();
		DecomposedGraphNode norTuNode = new DecomposedGraphNode();
		DecomposedGraphNode notTuNode = new DecomposedGraphNode();
		DecomposedGraphNode internalNorOutNode = new DecomposedGraphNode();
		DecomposedGraphNode inputNode1 = new DecomposedGraphNode(inputs.get(0));
		DecomposedGraphNode inputNode2 = new DecomposedGraphNode(inputs.get(1));
		DecomposedGraphNode outputNode = new DecomposedGraphNode(outputs.get(0));

		decomposedOR.addAllNodes(norTuNode, notTuNode, internalNorOutNode,
				inputNode1, inputNode2, outputNode);
		
	
		decomposedOR.addNodeRelationship(norTuNode, inputNode1, NodeInteractionType.REPRESSION);
		decomposedOR.addNodeRelationship(norTuNode, inputNode2, NodeInteractionType.REPRESSION);
		decomposedOR.addNodeRelationship(internalNorOutNode, norTuNode, NodeInteractionType.PRODUCTION);
		
		decomposedOR.addNodeRelationship(notTuNode, internalNorOutNode, NodeInteractionType.REPRESSION);
		decomposedOR.addNodeRelationship(outputNode, notTuNode, NodeInteractionType.PRODUCTION);
		
		decomposedOR.setNodeAsLeaf(inputNode1);
		decomposedOR.setNodeAsLeaf(inputNode2);
		decomposedOR.setNodeAsOutput(outputNode);
		return decomposedOR;
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
