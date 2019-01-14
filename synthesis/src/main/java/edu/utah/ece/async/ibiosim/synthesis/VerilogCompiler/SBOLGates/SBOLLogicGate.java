package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.SBOLGates;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;

import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;

/**
 * An abstract class to define what composes a logic gate in the SBOL form. 
 * @author Tramy Nguyen  
 */
public abstract class SBOLLogicGate {
	
	public abstract String getGateName();
	public abstract FunctionalComponent getTranscriptionalUnit();
	public abstract FunctionalComponent getOutputProtein();
	public abstract Interaction getOutputInteraction();
	
	public abstract void setOutput(FunctionalComponent outputProtein, Interaction outputWire);
	
	public abstract void addInput(FunctionalComponent inputProtein, Interaction inputWire) throws VerilogCompilerException;
}
