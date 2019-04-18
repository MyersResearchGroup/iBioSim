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
	
	public DecomposedGraph getDecomposedGate() {
		DecomposedGraph graph = new DecomposedGraph();
		
		
		return graph;
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
		Node norTuNode = new Node();
		Node notTuNode = new Node();
		Node internalNorOutNode = new Node();
		Node inputNode1 = new Node(inputs.get(0).getIdentity(), inputs.get(0).getDefinition());
		Node inputNode2 = new Node(inputs.get(1).getIdentity(), inputs.get(1).getDefinition());;
		Node outputNode = new Node(outputs.get(0).getIdentity(), outputs.get(0).getDefinition());

		decomposedOR.addAllNodes(norTuNode, notTuNode, internalNorOutNode,
				inputNode1, inputNode2, outputNode);
		
	
		decomposedOR.addNodeRelationship(norTuNode, inputNode1);
		decomposedOR.addNodeRelationship(norTuNode, inputNode2);
		decomposedOR.addNodeRelationship(internalNorOutNode, norTuNode);
		
		decomposedOR.addNodeRelationship(notTuNode, internalNorOutNode);
		decomposedOR.addNodeRelationship(outputNode, notTuNode);
		
		decomposedOR.setNodeAsLeaf(inputNode1);
		decomposedOR.setNodeAsLeaf(inputNode2);
		decomposedOR.setNodeAsOutput(outputNode);
		return decomposedOR;
	}

}
