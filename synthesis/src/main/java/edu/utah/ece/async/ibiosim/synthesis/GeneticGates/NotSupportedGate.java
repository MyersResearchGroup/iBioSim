package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.List;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.w3c.css.sac.InputSource;

public class NotSupportedGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	private ModuleDefinition md;
	
	public NotSupportedGate(SBOLDocument doc, ModuleDefinition md) {
		this.sbolDoc = doc;
		this.md = md;
	}
	
	@Override
	public GateType getType() {
		return GateType.NOTSUPPORTED;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

	@Override
	public void addInputMolecule(FunctionalComponent inputMolecule) {
		
	}

	@Override
	public void addOutputMolecule(FunctionalComponent outputMolecule) {
		
	}

	@Override
	public ModuleDefinition getModuleDefinition() {
		return this.md; 
	}

	@Override
	public List<FunctionalComponent> getListOfInputs() {
		return null;
	}

	@Override
	public List<FunctionalComponent> getListOfOutputs() {
		return null;
	}

	@Override
	public boolean containsInput(FunctionalComponent fc) {
		return false; 
	}

	@Override
	public boolean containsOutput(FunctionalComponent fc) {
		return false;
	}

}
