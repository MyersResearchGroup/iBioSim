package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.conversion.VPRModelGenerator;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate.GateType;


/**
 * Test NOT gate construction.
 * @author Tramy Nguyen
 */
public class NOTGate_Test {
	
	private static GateGeneration generator;
	
	@BeforeClass
	public static void setupTest() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.notTU1_File);
		
		generator = GateGenerationRunner.run(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
	}
	
	@Test
	public void Test_librarySize() throws SBOLValidationException {
		Assert.assertEquals(1, generator.getNOTLibrary().getRootModuleDefinitions().size());
	}
	
	@Test 
	public void Test_NOTGateSize() {
		Assert.assertEquals(1, generator.getGeneticGateList().size());
	}
	
	@Test
	public void Test_NOTGate() { 
		GeneticGate notGate = generator.getGeneticGateList().iterator().next();
		Assert.assertEquals(notGate.getType(), GateType.NOT);
	}
	
	@Test 
	public void Test_outputLibrary() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException {
		SBOLDocument inFile = SBOLReader.read(new File(TestingFiles.notTU1_File));
		SBOLDocument enrichedFile = VPRModelGenerator.generateModel("https://synbiohub.programmingbiology.org/", inFile, "");
		Assert.assertTrue(generator.getNOTLibrary().equals(enrichedFile));
	}
}
