package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import uk.ac.ncl.ico2s.VPRException;
import uk.ac.ncl.ico2s.VPRTripleStoreException;
import uk.ac.ncl.ico2s.sbolstack.SBOLInteractionAdder_GeneCentric;

public class ModelGenerator {

	public SBOLDocument generateModel(String inputFileDir, String inputFileName)
	{
		SBOLDocument doc = null;
		
			try {
				doc = SBOLReader.read(new File(inputFileDir + inputFileName));
				String endpoint="http://synbiohub.org/sparql";  
				SBOLInteractionAdder_GeneCentric interactionAdder = new SBOLInteractionAdder_GeneCentric(URI.create(endpoint));
				interactionAdder.addInteractions(doc);
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
			 
		return doc;
	}
}
