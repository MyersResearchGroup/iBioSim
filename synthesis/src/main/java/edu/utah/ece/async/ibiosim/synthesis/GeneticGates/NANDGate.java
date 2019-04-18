package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.ArrayList;
import java.util.List;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph.Node;

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
		Node norTuNode = new Node();
		Node notTuNode1 = new Node();
		Node notTuNode2 = new Node();
		Node notTuNode3 = new Node();
		Node internalNotOut1Node = new Node();
		Node internalNotOut2Node = new Node();
		Node internalNorOutNode = new Node();
		Node inputNode1 = new Node(inputs.get(0).getIdentity(), inputs.get(0).getDefinition());
		Node inputNode2 = new Node(inputs.get(1).getIdentity(), inputs.get(1).getDefinition());
		Node outputNode = new Node(outputs.get(0).getIdentity(), outputs.get(0).getDefinition());
	
		decomposedNand.addAllNodes(norTuNode, notTuNode1, notTuNode2, notTuNode3,
				internalNorOutNode, internalNotOut1Node, internalNotOut2Node,
				inputNode1, inputNode2, outputNode);
	
		decomposedNand.addNodeRelationship(notTuNode1, inputNode1);
		decomposedNand.addNodeRelationship(internalNotOut1Node, notTuNode1);

		decomposedNand.addNodeRelationship(notTuNode2, inputNode2);
		decomposedNand.addNodeRelationship(internalNotOut2Node, notTuNode2);
		
		decomposedNand.addNodeRelationship(norTuNode, internalNotOut1Node);
		decomposedNand.addNodeRelationship(norTuNode, internalNotOut2Node);
		decomposedNand.addNodeRelationship(internalNorOutNode, norTuNode);

		decomposedNand.addNodeRelationship(notTuNode3, internalNorOutNode);
		decomposedNand.addNodeRelationship(outputNode, notTuNode3);
		
		decomposedNand.setNodeAsLeaf(inputNode1);
		decomposedNand.setNodeAsLeaf(inputNode2);
		decomposedNand.setNodeAsOutput(outputNode);
		return decomposedNand;
		
	}

}
