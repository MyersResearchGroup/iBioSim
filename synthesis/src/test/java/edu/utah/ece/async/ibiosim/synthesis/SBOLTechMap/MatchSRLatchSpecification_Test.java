package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMap;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

public class MatchSRLatchSpecification_Test {

private static Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> matchedSolution;
	
	@BeforeClass
	public static void setupTest() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		
		setupOpt.setSpecificationFile(TestingFiles.SRLatch_Spec);

		List<DecomposedGraph> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertEquals(4, libGraph.size());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		Assert.assertEquals(1, specGraph.size());

		SBOLTechMap techMap = new SBOLTechMap(libGraph, specGraph);
		matchedSolution = techMap.match(specGraph.get(0));
		
	}
	
	@Test
	public void Test_matchSize() { 
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(3, matchedSolution.size());
	}
	
}
