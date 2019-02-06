package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.virtualparts.VPRException;
import org.virtualparts.VPRTripleStoreException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;

public class NORGate_Test {

	private static SBOLDocument norLibrary;

	@BeforeClass
	public static void setupTest() throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException, GateGenerationExeception {
		GateGeneratorOptions setupOpt = new GateGeneratorOptions();
		setupOpt.addTUFile(TestingFiles.norTU1_File);


		GateGeneration generator = GateGenerationRunner.run(setupOpt.getTUSBOLDocumentList(), "https://synbiohub.programmingbiology.org/");
		norLibrary = generator.getNORLibrary();
	}

	@Test
	public void Test_librarySize() {
		Assert.assertEquals(1, norLibrary.getRootModuleDefinitions().size());
	}
}
