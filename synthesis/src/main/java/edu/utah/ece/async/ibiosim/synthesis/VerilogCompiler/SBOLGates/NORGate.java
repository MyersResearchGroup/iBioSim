package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.SBOLGates;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;

import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;

/**
 * 
 * @author Tramy Nguyen 
 */
public class NORGate extends SBOLLogicGate {

	private FunctionalComponent gate, outNode, inLeftNode, inRightNode;
	private Interaction outInter, inLeftInter, inRightInter;
	private String gateName;

	public NORGate(String gateName, FunctionalComponent gate) {
		this.gateName = gateName;
		this.gate = gate;
	}

	@Override
	public FunctionalComponent getTranscriptionalUnit() {
		return this.gate;
	}

	@Override
	public FunctionalComponent getOutputProtein() {
		return this.outNode;
	}

	@Override
	public Interaction getOutputInteraction() {
		return this.outInter;
	}

	@Override
	public void setOutput(FunctionalComponent outputProtein, Interaction outputWire) {
		this.outNode = outputProtein;
		this.outInter = outputWire;
	}

	@Override
	public void addInput(FunctionalComponent inputProtein, Interaction inputWire) throws VerilogCompilerException {
		if(this.inLeftNode == null && this.inLeftInter == null) {
			this.inLeftNode = inputProtein;
			this.inLeftInter = inputWire;
		}
		else if(this.inRightNode == null && this.inRightInter == null) {
			this.inRightNode = inputProtein;
			this.inRightInter = inputWire;
		}
		else {
			throw new VerilogCompilerException("This NOR gate already has 2 input signals set and will not support more than 2 input NOR gates when converting to the SBOL data model.");
		}
	}
	
	public Interaction getLeftInputInteraction() {
		return this.inLeftInter;
	}

	public Interaction getRightInputInteraction() {
		return this.inRightInter;
	}

	public FunctionalComponent getLeftInputProtein() {
		return this.inLeftNode;
	}
	
	public FunctionalComponent getRightInputProtein() {
		return this.inRightNode;
	}

	public String getGateName() {
		return this.gateName;
	}
	
}

