package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.ibiosim.synthesis.TestingFiles;
import edu.utah.ece.async.ibiosim.synthesis.GateGenerator.GateGenerationExeception;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraph;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.DecomposedGraphNode;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.GeneticGatesException;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NANDGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.NOTGate;
import edu.utah.ece.async.ibiosim.synthesis.GeneticGates.ORGate;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.DirectMatch;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Match;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.Cover;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapException;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.SBOLTechMapOptions;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapSolution;
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.TechMapUtility;

/**
 * Test branch and bound covering method on varying library gate sizes.
 * 
 * @author Tramy Nguyen
 *
 */
public class BranchAndBound_Test {
	
	
	@Test
	public void Test_Not_LibSize1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NOT_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NOTGate);
	}
	
	@Test
	public void Test_Or_LibSize1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.OR_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof ORGate);
	}
	
	@Test
	public void Test_Not_LibSize2() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.NOT_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NOTGate);
	}
	
	@Test
	public void Test_Nand_LibSize1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NANDGate);
	}
	
	@Test
	public void Test_Nand_LibSize2() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NANDGate);
	}
	
	@Test
	public void Test_Nand_LibSize3() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.NAND_Spec);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NANDGate);
	}
	
	@Test
	public void Test_And_LibSize1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.AND_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof ANDGate);
	}
	
	@Test
	public void Test_SrLatch_LibSize1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.SRLatch_Spec);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSBOL(setupOpt.getLibrary());
		List<DecomposedGraph> specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph.get(0), libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(3, branchAndBoundSolution.getGateMapping().size());
		
		
	}

}
