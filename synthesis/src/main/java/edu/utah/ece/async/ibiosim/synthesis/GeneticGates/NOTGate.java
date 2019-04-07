package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class NOTGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private ModuleDefinition md;
	private List<FunctionalComponent> outputs;
	private FunctionalComponent input;
	private FunctionalComponent tu;
	
	
	public NOTGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.outputs = new ArrayList<>();
		this.md = md;
	}
	
	public void setTranscriptionalUnit(FunctionalComponent fc) {
		tu = fc;
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

	

}
