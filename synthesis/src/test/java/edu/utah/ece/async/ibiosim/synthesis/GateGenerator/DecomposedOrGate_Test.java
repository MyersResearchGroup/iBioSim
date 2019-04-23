package edu.utah.ece.async.ibiosim.synthesis.GateGenerator;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GateIdentifier;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;

public class DecomposedOrGate_Test {

	
	private static DecomposedGraph decomposedGraph ;
	
	@BeforeClass
	public static void setupTest() throws SBOLValidationException, IOException, SBOLConversionException, GateGenerationExeception { 
		
		SBOLDocument inFile = SBOLReader.read(new File(TestingFiles.OR_LibSize1));
		Assert.assertEquals(1,  inFile.getRootModuleDefinitions().size());
		ModuleDefinition md = inFile.getRootModuleDefinitions().iterator().next();
		GateIdentifier sortInstance = new GateIdentifier(inFile, md);
		GeneticGate gate = sortInstance.createGate();
		Assert.assertTrue(gate instanceof ORGate);
		ORGate orGate = (ORGate) gate;
		decomposedGraph = orGate.getDecomposedGraph();
	}
	
	@Test
	public void Test_graphSize() {
		Assert.assertEquals(6, decomposedGraph.topologicalSort().size());
	}
	
	@Test
	public void Test_outputNode() {
		DecomposedGraphNode n = decomposedGraph.getOutputNode();
		Assert.assertEquals(1, n.getChildrenNodeList().size());
		Assert.assertEquals(0, n.getParentNodeList().size());
	}
	
	@Test
	public void Test_inputNodes() {
		Assert.assertEquals(2, decomposedGraph.getLeafNodes().size());
		for(DecomposedGraphNode n : decomposedGraph.getLeafNodes()) {
			Assert.assertEquals(1, n.getParentNodeList().size());
			Assert.assertEquals(0, n.getChildrenNodeList().size());
		}
	}
}
