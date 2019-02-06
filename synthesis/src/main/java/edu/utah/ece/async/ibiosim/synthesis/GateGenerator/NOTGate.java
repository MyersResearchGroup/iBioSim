package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class NOTGate implements GeneticGate {

	private SBOLDocument sbolDoc; //where the gate is stored in.
	
	public NOTGate(SBOLDocument doc) {
		this.sbolDoc = doc;
		
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
