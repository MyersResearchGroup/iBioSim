package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;


/**
 * Test NOT gate construction.
 * @author Tramy Nguyen
 */
public class NOTGate_Test {
	
	private static SBOLDocument notLibrary;
	
	@BeforeClass
	public static void setupTest() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(GateGeneratorTestSuite.notTU1_File);
		
		
		GateGeneration generator = GateGenerationRunner.run(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
		notLibrary = generator.getNOTLibrary();
	}
	
	@Test
	public void Test_librarySize() {
		Assert.assertEquals(1, notLibrary.getRootModuleDefinitions().size());
	}
	
	@Test
	public void Test_NOTGateTU() {
		
	}
	
	@Test
	public void Test_inputNOTGate() {
		Set<ModuleDefinition> library = notLibrary.getRootModuleDefinitions();
		ModuleDefinition mdGate = library.iterator().next();
		
	}
	
	@Test
	public void Test_cdSize() {
		Assert.assertEquals(9, notLibrary.getComponentDefinitions().size());
	}
	
	@Test 
	public void Test_cdDisplayIds() {
		List<String> expectedIds = Arrays.asList("pSrpR", "A1_AmtR", "BydvJ", "A1", "AmtR", "L3S2P55", "notTU",
				"SrpR_protein", "AmtR_protein");
		for(ComponentDefinition cd : notLibrary.getComponentDefinitions()) {
			Assert.assertTrue(expectedIds.contains(cd.getDisplayId()));
		}
	}
}
