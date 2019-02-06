package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Synthesis;

/**
 * 
 * @author Tramy Nguyen
 *
 */
public class LibrarySize6_Test1 {

	private static SBOLDocument sbolDoc = null;
	
	@BeforeClass
	public static void setupTest() {
		try {
			SBOLTechMapOptions techMapOptions = new SBOLTechMapOptions();
			techMapOptions.setSpecificationFile(TestingFiles.SRLATCH_Spec);
			techMapOptions.setLibraryFile(TestingFiles.NORNOT_LibSize6);
			
			Synthesis syn = SBOLTechMap.runSBOLTechMap(techMapOptions.getSpeficationFile(), techMapOptions.getLibraryFile());
			sbolDoc = syn.getSBOLfromTechMapping();
		} 
		catch (SBOLException | SBOLValidationException | IOException | SBOLConversionException | SBOLTechMapException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void Test_cdSize(){
		Assert.assertEquals(16, sbolDoc.getComponentDefinitions().size());
	}
}
