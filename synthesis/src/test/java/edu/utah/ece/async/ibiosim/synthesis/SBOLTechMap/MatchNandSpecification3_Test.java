package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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



/**
 * Match a NAND spec with a NAND gate, 2 NOT gates, and a NOR gate. 
 * @author Tramy Nguyen
 */
public class MatchNandSpecification3_Test {
	
	private static Map<DecomposedGraphNode, LinkedList<DecomposedGraph>> matchedSolution;
	
	@BeforeClass
	public static void setupTest() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

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
		Assert.assertEquals(4, matchedSolution.size());
	}
	
	
	@Test
	public void Test_gateSolutionPerSpecNode() { 
		
		for(Entry<DecomposedGraphNode, LinkedList<DecomposedGraph>> entry : matchedSolution.entrySet()) {
			DecomposedGraphNode specNode = entry.getKey();
			LinkedList<DecomposedGraph> gates = entry.getValue(); 
			//System.out.println(specNode.toString());
			if(specNode.toString().equals("http://dummy.org/circuit_Nand/FC7_wiredProtein/1.0")) {
				Assert.assertEquals(2, gates.size());
			}
			else if(specNode.toString().equals("http://dummy.org/circuit_Nand/FC6_wiredProtein/1.0")) {
				Assert.assertEquals(2, gates.size());
			}
			else if(specNode.toString().equals("http://dummy.org/circuit_Nand/FC4_wiredProtein/1.0")) {
				Assert.assertEquals(1, gates.size());
			}
			else if(specNode.toString().equals("http://dummy.org/circuit_Nand/FC2_y/1.0")) {
				Assert.assertEquals(3, gates.size());
			}
			else {
				Assert.fail("Unexpected specification node was matched " + specNode.toString());
			}
		}
	}
}
