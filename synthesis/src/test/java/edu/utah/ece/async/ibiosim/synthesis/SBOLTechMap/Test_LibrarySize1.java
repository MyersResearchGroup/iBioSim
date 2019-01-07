package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;



public class Test_LibrarySize1 {
	
	private static SBOLDocument sbolDoc = null;
	private static ModuleDefinition circuitSol = null;
	
	
	
	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-sf", SBOLTechMapTestSuite.NOT_Spec, "-lf", SBOLTechMapTestSuite.NOT2_LibSize1, "-sbol"};
		sbolDoc = SBOLTechMapTestSuite.testEnv.runTechMap(cmd);
		circuitSol = sbolDoc.getModuleDefinition("TechMapSolution_Circuit", "1.0");
	}
	
	
	@Test
	public void Test_cdSize(){
		Assert.assertEquals(7, sbolDoc.getComponentDefinitions().size());
	}
	


}
