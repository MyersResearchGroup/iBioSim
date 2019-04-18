package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.ArrayList;
import java.util.Arrays;
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
public class NOTGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private DecomposedGraph decomposedNot;
	private ModuleDefinition md;
	private List<FunctionalComponent> outputs;
	private FunctionalComponent input;
	private FunctionalComponent tu;
	
	
	public NOTGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
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
		return GateType.NOT;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

	@Override
	public void addInputMolecule(FunctionalComponent inputMolecule) {
		input = inputMolecule;
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
		return Arrays.asList(input);
	}

	@Override
	public List<FunctionalComponent> getListOfOutputs() {
		return outputs;
	}

	@Override
	public boolean containsInput(FunctionalComponent fc) {
		if(input == null) {
			return false;
		}
		return input.equals(fc); 
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
		if (decomposedNot == null) {
			if(outputs.size() >= 1) {
				decomposedNot = createDecomposedGate();
			}
		}
		return decomposedNot;
	}

	private DecomposedGraph createDecomposedGate() {
		DecomposedGraph decomposedNOT = new DecomposedGraph();
		Node tuNode = new Node();
		Node inputNode = new Node(input.getIdentity(), input.getDefinition());
		Node outputNode = new Node(outputs.get(0).getIdentity(), outputs.get(0).getDefinition());
		decomposedNOT.addAllNodes(tuNode, inputNode, outputNode);
		
		decomposedNOT.addNodeRelationship(tuNode, inputNode);
		decomposedNOT.addNodeRelationship(outputNode, tuNode);
		
		decomposedNOT.setNodeAsLeaf(inputNode);
		decomposedNOT.setNodeAsOutput(outputNode);
		return decomposedNOT;
	}
	

}
