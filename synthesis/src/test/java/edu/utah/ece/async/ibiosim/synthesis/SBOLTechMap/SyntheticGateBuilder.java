package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.File;
import java.io.FileNotFoundException;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler.VerilogCompilerException;

/**
 * Generate a specified number of NOT gates and 2 Tandem Promoter gates and export to SBOL. 
 * Result of gates are stored in sbolLibFiles directory.
 * 
 * @author Tramy Nguyen
 */
public class SyntheticGateBuilder {

	public static void main(String[] args) {
		int numOfNOTGates = 2, numOfTandemGates = 1;
		String outputFileFullPath = SBOLTechMapTestSuite.sbolLibDir + File.separator + "NORNOTGates_LibrarySize3.xml";
		try {
			GateGenerator gateBuilder = runGateGenerator(numOfNOTGates, numOfTandemGates);
			SBOLUtility.getInstance().writeSBOLDocument(outputFileFullPath, gateBuilder.getGateSBOLDocument());
		} 
		catch (SBOLException | SBOLValidationException | VerilogCompilerException | FileNotFoundException | SBOLConversionException e) {
			e.printStackTrace();
		}
		System.out.println("Finished building synthetic gates.");
	}
	
	public static GateGenerator runGateGenerator(int numOfNOTGates, int numOfTandemGates) throws SBOLException, SBOLValidationException, VerilogCompilerException {
		GateGenerator gateBuilder = new GateGenerator();
		for(int i = 0; i<numOfNOTGates; i++) {
			gateBuilder.createNOTGate();
		}
		
		for(int i = 0; i<numOfTandemGates; i++) {
			gateBuilder.createTandemPromoterGate();
		}
		return gateBuilder;
	}
}
