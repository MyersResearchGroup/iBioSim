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
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph.Node;
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
public class NotSpecification_Test {

	private static Map<Node, LinkedList<WeightedGraph>> matchedSolution;
	
	@BeforeClass
	public static void setupTest() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.setLibraryFile(TestingFiles.NOT1_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NOT_Spec);

		List<DecomposedGraph> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibraryFile());
		Assert.assertEquals(libGraph.size(), 1);
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpeficationFile());
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
		LinkedList<WeightedGraph> solution = matchedSolution.values().iterator().next();
		WeightedGraph actualGate = solution.get(0);
		
	}
	
	@Test
	public void Test_Node() { 
		Node actualNode = matchedSolution.keySet().iterator().next();
		
		//Assert.assertTrue(actualNode.uri.isPresent());
		//Assert.assertTrue(actualNode.uri.get());
	}
}
