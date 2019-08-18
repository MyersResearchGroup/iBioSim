package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate.GateType;

/**
 * Test genetic gate type.
 * @author Tramy Nguyen
 *
 */
public class GateType_Test {

	@Test
	public void Test_NOT() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.NOT_LibSize1);
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.generateGatesFromTranscriptionalUnits(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org");
		generator.identifyGeneratedGates(enrichedTU_List);
		List<GeneticGate> notList = generator.getGates(GateType.NOT);
		Assert.assertTrue(1 == notList.size());
	}
	
	@Test
	public void Test_WiredOR() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception, SBOLException {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.NOT_LibSize1);
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.generateGatesFromTranscriptionalUnits(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org");
		generator.identifyGeneratedGates(enrichedTU_List);
		generator.generateWiredORGates(generator.getGates(GateType.NOT));
		List<GeneticGate> wireORList = generator.getGates(GateType.WIREDOR);
		Assert.assertTrue(1 == wireORList.size());
	}
	
	@Test
	public void Test_NAND() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.nandTU_size1_File);
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.generateGatesFromTranscriptionalUnits(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org");
		generator.identifyGeneratedGates(enrichedTU_List);
		List<GeneticGate> notList = generator.getGates(GateType.NAND);
		Assert.assertTrue(1 == notList.size());
	}
}
