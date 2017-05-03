package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

// TODO: temporarily removed until VPR library sorted
//import uk.ac.ncl.ico2s.VPRException;
//import uk.ac.ncl.ico2s.VPRTripleStoreException;

public class ModelGeneratorTests extends ConversionAbstractTests{

	/*
	 * r1.xml - module_BO_10845_represses_BO_27632; module_BO_28528_encodes_BO_26934
	 * r2.xml - module_BO_10858_represses_BO_27720; module_BO_28529_encodes_BO_26892
	 * r3.xml - module_BO_11205_activates_BO_27654; module_BO_28532_encodes_BO_26966
	 * r4.xml - Pro: BO_2685 RBS: BO_27789 CDS: BO_28531 T: BO_4261
	 */
	// TODO: temporarily removed until VPR library sorted
/*
	@Test
	public void test_repressions() throws VPRException{
		File inputFolder = new File(repressionDir);
		File[] listOfFiles = inputFolder.listFiles();
		int index = 1;
		for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getAbsolutePath());
		        SBOLDocument outDoc = null;
				try {
					outDoc = ModelGenerator.generateModel(listOfFiles[i]);
				} catch (SBOLValidationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SBOLConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VPRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (VPRTripleStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        ModelGenerator.exportModel("/Users/tramyn/Desktop/outputModels/" + "r"+ index++ + "_out.xml", outDoc);
		      } 
		    }
	}
	*/
}
