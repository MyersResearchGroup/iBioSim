package edu.utah.ece.async.ibiosim.synthesis.GeneticGates;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

public class WiredOrGate {
	
	private SBOLUtility sbolUtil;
	
	WiredOrGate(){
		this.sbolUtil = SBOLUtility.getInstance();
		
	}

	public void createWiredOrGate(ComponentDefinition cd, SBOLDocument docOfCD) throws SBOLValidationException {
		SBOLDocument doc = docOfCD.createRecursiveCopy(cd);
		//ModuleDefinition gate = doc.createModuleDefinition(displayId)
		
	}
	
	
}
