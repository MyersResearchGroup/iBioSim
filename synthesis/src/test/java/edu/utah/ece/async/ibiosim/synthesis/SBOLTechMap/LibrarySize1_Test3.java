package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapRunner;

public class LibrarySize1_Test3 {

	@BeforeClass
	public static void setupTest() {
		SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
		techMapOptions.setSpecificationFile(TestingFiles.SRLATCH_Spec);
		techMapOptions.setLibraryFile(TestingFiles.flat_SRLATCH_Spec);

		try {
			SBOLTechMapRunner.asyncRun(techMapOptions.getSpeficationFile(), techMapOptions.getLibraryFile());
		} 
		catch (SBOLTechMapException | SBOLValidationException | IOException | SBOLConversionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void Test_cdSize(){
		System.out.println("Begin");
	}
}
