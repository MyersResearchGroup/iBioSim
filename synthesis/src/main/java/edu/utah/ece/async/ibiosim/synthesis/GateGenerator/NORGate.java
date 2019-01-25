package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class NORGate implements GeneticGate{

	private SBOLDocument sbolDoc;
	
	public NORGate(SBOLDocument doc) {
		this.sbolDoc = doc;
	}
	
	@Override
	public GateType getType() {
		return GateType.NOR;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

}
