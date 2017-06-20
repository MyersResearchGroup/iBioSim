package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.IOException;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

public class testingClass {

	public static void main(String[] args) {
		try 
		{
			SBOLReader.setURIPrefix("http://www.async.ece.utah.edu");
			SBOLDocument myFile = SBOLReader.read(new File("/Users/tramyn/Desktop/temp/test3/NegativeAutoRegulatoryDesign_Cello_module.xml"));
//			SBOLDocument gokselFile = SBOLReader.read(new File("/Users/tramyn/Downloads/my_NegativeAutoRegulatoryCelloDesign_output.xm"));
			
//			myFile.equals(gokselFile);
		} catch (SBOLValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
