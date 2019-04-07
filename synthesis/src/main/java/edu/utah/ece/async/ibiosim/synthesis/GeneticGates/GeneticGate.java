package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import java.util.List;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public interface GeneticGate {
	
	enum GateType{
		NOT, NOR, OR, 
		AND, NAND,
		NOTSUPPORTED;
	}
	
	public void addInputMolecule(FunctionalComponent inputMolecule);
	public void addOutputMolecule(FunctionalComponent outputMolecule);
	
	public List<FunctionalComponent> getListOfInputs();
	public List<FunctionalComponent> getListOfOutputs();
	
	public boolean containsInput(FunctionalComponent fc);
	public boolean containsOutput(FunctionalComponent fc);
	
	
	public GateType getType();
	public SBOLDocument getSBOLDocument();
	public ModuleDefinition getModuleDefinition();
	
}
