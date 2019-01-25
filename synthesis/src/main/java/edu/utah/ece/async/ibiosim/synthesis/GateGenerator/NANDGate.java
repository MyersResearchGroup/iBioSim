package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class NANDGate implements GeneticGate {

	private SBOLDocument sbolDoc; //where the gate is stored in.

	public NANDGate(SBOLDocument doc) {
		this.sbolDoc = doc;
	}
	
	@Override
	public GateType getType() {
		return GateType.NAND;
	}

	@Override
	public SBOLDocument getSBOLDocument() {
		return sbolDoc;
	}

}
