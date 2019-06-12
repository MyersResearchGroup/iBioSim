package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
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
 * Test gates with roadblocking. 
 * @author Tramy Nguyen
 */
public class GateGeneration_Test {
	
	@Test 
	public void Test_pSrpR_RoadblockGates() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.pSrpR_roadblockTU_File);
		
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.generateGatesFromTranscriptionalUnits(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
		generator.identifyGeneratedGates(enrichedTU_List);
		generator.exportLibrary(generator.getLibrary(), TestingFiles.writeLibDir + File.separator + "pSrpR_Library");
		Assert.assertEquals(11, generator.getLibrary().size());
		
	}
	
	@Test 
	public void Test_wiredORGates() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, SBOLException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.notTU1_Size1_File);
		
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.generateGatesFromTranscriptionalUnits(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
		
		generator.generateWiredORGates();
		List<GeneticGate> notGates = generator.getGatesWithType(GateType.NOT);
	}

}
