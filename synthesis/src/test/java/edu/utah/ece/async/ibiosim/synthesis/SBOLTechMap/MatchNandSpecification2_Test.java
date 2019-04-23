package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.util.Collection;
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

public class MatchNandSpecification2_Test {
	
	private static Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> matchedSolution;
	
	@BeforeClass
	public static void setupTest() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

		List<DecomposedGraph> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertEquals(2, libGraph.size());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		Assert.assertEquals(1, specGraph.size());

		SBOLTechMap techMap = new SBOLTechMap(libGraph, specGraph);
		matchedSolution = techMap.match(specGraph.get(0));
		
	}
	
	@Test
	public void Test_matchSize() { 
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(1, matchedSolution.size());
	}
	
	@Test
	public void Test_gateSolutionSize() { 
		Collection<LinkedList<DecomposedGraph>> weightedGraphs = matchedSolution.values();
		Assert.assertEquals(2, weightedGraphs.iterator().next().size());
	}
}
