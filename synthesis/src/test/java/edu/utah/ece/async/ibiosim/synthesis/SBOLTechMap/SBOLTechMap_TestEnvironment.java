package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Synthesis;

public class SBOLTechMap_TestEnvironment {

	public SBOLDocument runTechMap(String[] args) {
		
		Synthesis syn = new Synthesis();
		SBOLDocument sbolSol = null;
		try {
			CommandLine cmd = SBOLTechMap.parseCommandLine(args);
			SBOLTechMapOptions techMapOptions = SBOLTechMap.createTechMapOptions(cmd);
			SBOLTechMap.runSBOLTechMap(syn, techMapOptions.getSpeficationFile(), techMapOptions.getLibraryFile());
			syn.printCoveredGates(syn.getBestSolution());
			sbolSol = syn.getSBOLfromTechMapping(syn.getBestSolution(), syn.getSpecification());

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SBOLException e) {
			e.printStackTrace();
		} catch (SBOLValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SBOLConversionException e) {
			e.printStackTrace();
		}
		
		return sbolSol;
	}
	
	
	
}
