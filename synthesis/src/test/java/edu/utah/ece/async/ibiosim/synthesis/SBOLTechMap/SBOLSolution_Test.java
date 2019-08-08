package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Cover;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.DirectMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLNetList;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapSolution;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

/**
 * Test SBOLSolution class
 * @author Tramy Nguyen 
 */
public class SBOLSolution_Test {
	
	@Test
	public void Test_SrLatch_LibSize4() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		SBOLNetList sbolSol = new SBOLNetList(specGraph, branchAndBoundSolution);
		SBOLDocument result = sbolSol.generateSbol();

		SBOLWriter.write(result, TestingFiles.outputDir + File.separator + "srLatch_solution.xml");
	}
}
