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

public class AndSpecification_Test {

	private static Map<Node, LinkedList<WeightedGraph>> matchedSolution;
	
	@BeforeClass
	public static void setupTest() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.setLibraryFile(TestingFiles.AND1_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.AND_Spec);

		List<DecomposedGraph> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibraryFile());
		Assert.assertEquals(1, libGraph.size());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpeficationFile());
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
		Assert.assertEquals(1, matchedSolution.values().size());
	}
}
