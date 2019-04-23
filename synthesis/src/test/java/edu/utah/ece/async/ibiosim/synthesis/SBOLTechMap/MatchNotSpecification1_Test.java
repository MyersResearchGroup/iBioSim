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
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.WeightedGraph;

/**
 * Test technology mapping on NOT spec. mapped to a NOT Cello gate.
 * 
 * @author Tramy Nguyen
 */
public class MatchNotSpecification1_Test {

	private static Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> matchedSolution;
	
	@BeforeClass
	public static void setupTest() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NOT_Spec);

		List<DecomposedGraph> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		Assert.assertEquals(libGraph.size(), 1);
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		Assert.assertEquals(specGraph.size(), 1);

		SBOLTechMap techMap = new SBOLTechMap(libGraph, specGraph);
		matchedSolution = techMap.match(specGraph.get(0));
		
	}
	
	@Test
	public void Test_matchSize() { 
		Assert.assertTrue(!matchedSolution.isEmpty());
		Assert.assertEquals(matchedSolution.size(), 1);
	}
	
	@Test
	public void Test_gateSolutionSize() { 
		Assert.assertEquals(1, matchedSolution.values().size());
	}
	
	@Test
	public void Test_gateSolution() { 
		LinkedList<DecomposedGraph> solution = matchedSolution.values().iterator().next();
		DecomposedGraph actualGate = solution.get(0);
		
	}
	
	@Test
	public void Test_Node() { 
		DecomposedGraphNode actualNode = matchedSolution.keySet().iterator().next();
		
		//Assert.assertTrue(actualNode.uri.isPresent());
		//Assert.assertTrue(actualNode.uri.get());
	}
}
