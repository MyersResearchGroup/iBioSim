package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import org.sbolstandard.core2.SBOLDocument;

public interface GeneticGate {
	
	enum GateType{
		NOT, NOR, OR, 
		AND, NAND,
		NOTSUPPORTED;
	}
	
	public GateType getType();
	public SBOLDocument getSBOLDocument();
	
	
}
