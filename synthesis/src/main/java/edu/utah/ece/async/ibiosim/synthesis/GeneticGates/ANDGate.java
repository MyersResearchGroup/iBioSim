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
		Node norTuNode = new Node();
		Node notTuNode1 = new Node();
		Node notTuNode2 = new Node();
		Node internalNotOut1Node = new Node();
		Node internalNotOut2Node = new Node();
		Node inputNode1 = new Node(inputs.get(0).getIdentity(), inputs.get(0).getDefinition());
		Node inputNode2 = new Node(inputs.get(1).getIdentity(), inputs.get(1).getDefinition());
		Node outputNode = new Node(outputs.get(0).getIdentity(), outputs.get(0).getDefinition());
	
		decomposedAnd.addNode(inputNode1);
		decomposedAnd.addNode(inputNode2);
		decomposedAnd.addNode(outputNode);
		decomposedAnd.addNode(norTuNode);
		decomposedAnd.addNode(notTuNode1);
		decomposedAnd.addNode(notTuNode2);
	
		decomposedAnd.addNodeRelationship(notTuNode1, inputNode1);
		decomposedAnd.addNodeRelationship(internalNotOut1Node, notTuNode1);

		decomposedAnd.addNodeRelationship(notTuNode2, inputNode2);
		decomposedAnd.addNodeRelationship(internalNotOut2Node, notTuNode2);
		
		decomposedAnd.addNodeRelationship(norTuNode, internalNotOut1Node);
		decomposedAnd.addNodeRelationship(norTuNode, internalNotOut2Node);
		decomposedAnd.addNodeRelationship(outputNode, norTuNode);
		
		decomposedAnd.setNodeAsLeaf(inputNode1);
		decomposedAnd.setNodeAsLeaf(inputNode2);
		decomposedAnd.setNodeAsOutput(outputNode);
		return decomposedAnd;
	}
	

}
