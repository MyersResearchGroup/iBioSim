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
	private FunctionalComponent tu;
	
	
	public NANDGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.md = md;
	}
	
	public void setTranscriptionalUnit(FunctionalComponent tu) {
		this.tu = tu;
	}
	
	public FunctionalComponent getTranscriptionalUnit() {
		return tu;
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
		DecomposedGraph decompNand = new DecomposedGraph();
		DecomposedGraphNode norTu = new DecomposedGraphNode();
		DecomposedGraphNode not1Tu = new DecomposedGraphNode();
		DecomposedGraphNode not2Tu = new DecomposedGraphNode();
		DecomposedGraphNode not3Tu = new DecomposedGraphNode();
		DecomposedGraphNode not1Out = new DecomposedGraphNode();
		DecomposedGraphNode not2Out = new DecomposedGraphNode();
		DecomposedGraphNode norOut = new DecomposedGraphNode();
		DecomposedGraphNode in1 = new DecomposedGraphNode(inputs.get(0));
		DecomposedGraphNode in2 = new DecomposedGraphNode(inputs.get(1));
		DecomposedGraphNode out = new DecomposedGraphNode(outputs.get(0));
	
		decompNand.addAllNodes(norTu, not1Tu, not2Tu, not3Tu,
				norOut, not1Out, not2Out,
				in1, in2, out);
	
		decompNand.addNodeRelationship(not1Tu, in1, NodeInteractionType.REPRESSION);
		decompNand.addNodeRelationship(not1Out, not1Tu, NodeInteractionType.PRODUCTION);

		decompNand.addNodeRelationship(not2Tu, in2, NodeInteractionType.REPRESSION);
		decompNand.addNodeRelationship(not2Out, not2Tu, NodeInteractionType.PRODUCTION);
		
		decompNand.addNodeRelationship(norTu, not1Out, NodeInteractionType.REPRESSION);
		decompNand.addNodeRelationship(norTu, not2Out, NodeInteractionType.REPRESSION);
		decompNand.addNodeRelationship(norOut, norTu, NodeInteractionType.PRODUCTION);

		decompNand.addNodeRelationship(not3Tu, norOut, NodeInteractionType.REPRESSION);
		decompNand.addNodeRelationship(out, not3Tu, NodeInteractionType.PRODUCTION);
		
		decompNand.setNodeAsLeaf(in1);
		decompNand.setNodeAsLeaf(in2);
		decompNand.setNodeAsOutput(out);
		return decompNand;
		
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
