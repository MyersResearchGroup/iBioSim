package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class ANDGate implements GeneticGate {
	
	private SBOLDocument sbolDoc;
	
	public ANDGate(SBOLDocument doc) {
		this.sbolDoc = doc;
	}
	
	@Override
	public GateType getType() {
		return GateType.AND;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

}
