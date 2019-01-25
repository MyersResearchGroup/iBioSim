package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

public class NotSupportedGate implements GeneticGate {

	private SBOLDocument sbolDoc;
	
	public NotSupportedGate(SBOLDocument doc) {
		this.sbolDoc = doc;
	}
	
	@Override
	public GateType getType() {
		return GateType.NOTSUPPORTED;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

}
