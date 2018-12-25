package SBOLGates;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;

import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;

/**
 * Class to hold the necessary SBOL components to represent a NOT logic gate. 
 * @author Tramy Nguyen 
 */
public class NOTGate extends SBOLLogicGate{
	
	private FunctionalComponent gate, outNode, inNode;
	private Interaction outInter, inInter;
	private String gateName; 
	
	public NOTGate(String gateName, FunctionalComponent gate) {
		this.gateName = gateName;
		this.gate = gate;
	}
	
	@Override
	public String getGateName() {
		return this.gateName;
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
		if(this.inNode == null && this.inInter == null) {
			this.inNode = inputProtein;
			this.inInter = inputWire;
		}
		else {
			throw new VerilogCompilerException("This NOT gate already has its input signal set.");
		}
	}

	public Interaction getInputInteraction() {
		return this.inInter;
	}

	public FunctionalComponent getInputProtein(){
		return this.inNode;
	}


}
