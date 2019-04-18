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
public class NORGate implements GeneticGate{

	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedNor;
	private ModuleDefinition md;
	private ArrayList<FunctionalComponent> inputs, outputs;
	private FunctionalComponent tu;
	
	public NORGate(SBOLDocument doc, ModuleDefinition md) {
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
		return GateType.NOR;
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
		if (decomposedNor == null) {
			if(inputs.size() >= 2 && outputs.size() >= 1) {
				decomposedNor = createDecomposedGate();
			}
		}
		return decomposedNor;

	}
	
	private DecomposedGraph createDecomposedGate() {
		DecomposedGraph decomposedNOR = new DecomposedGraph();
		Node tuNode = new Node();
		Node inputNode1 = new Node(inputs.get(0).getIdentity(), inputs.get(0).getDefinition());
		Node inputNode2 = new Node(inputs.get(1).getIdentity(), inputs.get(1).getDefinition());
		Node outputNode = new Node(outputs.get(0).getIdentity(), outputs.get(0).getDefinition());
	
		decomposedNOR.addNode(tuNode);
		decomposedNOR.addNode(inputNode1);
		decomposedNOR.addNode(inputNode2);
		decomposedNOR.addNode(outputNode);
		
		decomposedNOR.addNodeRelationship(tuNode, inputNode1);	
		decomposedNOR.addNodeRelationship(tuNode, inputNode2);	
		decomposedNOR.addNodeRelationship(outputNode, tuNode);
		
		decomposedNOR.setNodeAsLeaf(inputNode1);
		decomposedNOR.setNodeAsLeaf(inputNode2);
		decomposedNOR.setNodeAsOutput(outputNode);

		return decomposedNOR;
	}
	

}
