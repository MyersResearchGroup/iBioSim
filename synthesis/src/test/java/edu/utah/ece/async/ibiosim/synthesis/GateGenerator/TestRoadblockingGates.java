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

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;


/**
 * Test gates with roadblocking. 
 * @author Tramy Nguyen
 */
public class TestRoadblockingGates {
	
	@Test 
	public void Test_pSrpR_RoadblockGates() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.pSrpR_roadblockTU_File);
		
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.enrichedTU(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
		generator.sortEnrichedTUList(enrichedTU_List);
		generator.exportLibrary(generator.getLibrary(), TestingFiles.writeLibDir + File.separator + "pSrpR_Library");
		Assert.assertEquals(11, generator.getGeneticGateList().size());
		
	}

	@Test 
	public void Test_pSrpR_Size2() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.nandTU_size2_File);
		
		GateGeneration generator = new GateGeneration();
		List<SBOLDocument> enrichedTU_List = generator.enrichedTU(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
		generator.sortEnrichedTUList(enrichedTU_List);
		int iteration = 1; 
		for(GeneticGate gate : generator.getGeneticGateList()) {
			generator.exportLibrary(gate.getSBOLDocument(), TestingFiles.writeLibDir + File.separator + gate.getModuleDefinition().getDisplayId() + iteration++ + ".xml");
		}
		Assert.assertEquals(2, generator.getGeneticGateList().size());
		
	}

}
