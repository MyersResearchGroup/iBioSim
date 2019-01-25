package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class NOTGate implements GeneticGate {

	private SBOLDocument sbolDoc; //where the gate is stored in.
	private FunctionalComponent inputProtein, outputProtein;
	private Interaction inputInteraction, outputInteraction;
	
	public NOTGate(SBOLDocument doc) {
		this.sbolDoc = doc;
		
	}
	
	public void addInputProtein(FunctionalComponent fc) {
		this.inputProtein = fc;
	}
	
	public void addOutputProtein(FunctionalComponent fc) {
		this.outputProtein = fc;
	}
	
	@Override
	public GateType getType() {
		return GateType.NOT;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

}
