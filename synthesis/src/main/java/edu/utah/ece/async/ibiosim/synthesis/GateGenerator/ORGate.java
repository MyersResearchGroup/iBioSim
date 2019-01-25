package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class ORGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	
	public ORGate(SBOLDocument doc) {
		this.sbolDoc = doc;
	}
	
	@Override
	public GateType getType() {
		return GateType.OR;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return sbolDoc;
	}

}
