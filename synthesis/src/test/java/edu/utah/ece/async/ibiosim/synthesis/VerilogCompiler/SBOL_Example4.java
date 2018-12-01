package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.DirectionType;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.Interaction;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.Participation;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SystemsBiologyOntology;


/**
 * Test 2 assign statements that should build two cross coupled NOR gate. 
 * @author Tramy Nguyen
 *
 */
public class SBOL_Example4 extends AbstractVerilogParserTest{

	private static SBOLDocument sbolDoc;
	private static ModuleDefinition sbolDesign;

	@BeforeClass
	public static void setupTest() {
		
		String[] cmd = {"-v", reader.getFile("contAssign5.v"), "-sbol"};
		
		VerilogCompiler compiledVerilog = reader.runCompiler(cmd);
		String vName = "contAssign5";
		WrappedSBOL sbolWrapper = compiledVerilog.getSBOLWrapper(vName);
		Assert.assertNotNull(sbolWrapper);
		sbolDoc = sbolWrapper.getSBOLDocument();
		Assert.assertEquals(1, sbolDoc.getModuleDefinitions().size());
		sbolDesign = sbolDoc.getModuleDefinition(vName, "1.0");
	}
	
	@Test
	public void Test_cdSize() {
		Assert.assertEquals(20, sbolDoc.getComponentDefinitions().size());
	}
}