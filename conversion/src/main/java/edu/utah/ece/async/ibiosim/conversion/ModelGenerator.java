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
import uk.ac.ncl.ico2s.sbol.SBOLHandler;
import uk.ac.ncl.ico2s.sbolstack.SBOLInteractionAdder_GeneCentric;

public class ModelGenerator {

	/**
	 * Generate SBOL model from the given design file.
	 * 
	 * @param file - The file to generate the model from
	 * @return
	 * @throws SBOLConversionException 
	 * @throws IOException 
	 * @throws SBOLValidationException 
	 * @throws VPRTripleStoreException 
	 * @throws VPRException 
	 */
	public static SBOLDocument generateModel(File file) throws SBOLValidationException, IOException, SBOLConversionException, VPRException, VPRTripleStoreException
	{
		SBOLDocument generatedModel = SBOLReader.read(file);
		String endpoint="http://synbiohub.org/sparql";  
		SBOLInteractionAdder_GeneCentric interactionAdder = new SBOLInteractionAdder_GeneCentric(URI.create(endpoint));
		interactionAdder.addInteractions(generatedModel);

		return generatedModel;
	}

	public static void exportModel(String outputFile, SBOLDocument outputDoc) throws VPRException
	{
		SBOLHandler.write(outputDoc, new File( outputFile));

	}
}
