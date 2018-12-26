package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 * Test hierarchical model for an SR-latch example exported into SBOL.
 * @author Tramy Nguyen 
 *
 */
public class SBOLExample8_Test {

	private static SBOLDocument sbolDoc;
	private static ModuleDefinition sbolDesign;
		
	@BeforeClass
	public static void setupTest() {

		String[] cmd = {"-v", CompilerTestSuite.verilogCont5_file, "-sbol"};
		VerilogCompiler compiledVerilog = CompilerTestSuite.testEnv.runCompiler(cmd); 
		
		String vName = "contAssign5";
		WrappedSBOL sbolWrapper = compiledVerilog.getSBOLWrapper(vName);
		Assert.assertNotNull(sbolWrapper);
	
		sbolDoc = sbolWrapper.getSBOLDocument();
	}

	@Test 
	public void Test_mdSize() {
		Assert.assertEquals(3, sbolDoc.getModuleDefinitions().size());
	}

	@Test
	public void Test_cdSize() {
		Assert.assertEquals(44, sbolDoc.getComponentDefinitions().size());
	}
}
