package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMap;

import java.io.IOException;
import java.net.URI;
import java.util.List;

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
import edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping.PreSelectedMatch;
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
	public void Test_Not1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NOTGate);
	}
	
	@Test
	public void Test_Or() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompOR_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof ORGate);
	}
	
	@Test
	public void Test_Not2() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNOT_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NOTGate);
	}
	
	@Test
	public void Test_Nand() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NANDGate);
	}
	
	@Test
	public void Test_Nand2() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize2);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NANDGate);
	}
	
	@Test
	public void Test_Nand3() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize2);
		setupOpt.addLibraryFile(TestingFiles.NOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompNAND_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof NANDGate);
	}
	
	@Test
	public void Test_And1() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.AND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompAND_File);

		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new DirectMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution branchAndBoundSolution = c.branchAndBoundCover();	
		Assert.assertEquals(1, branchAndBoundSolution.getGateMapping().size());
		Assert.assertTrue(branchAndBoundSolution.getGateMapping().values().iterator().next() instanceof ANDGate);
	}
	
	@Test
	public void Test1_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();	
		Assert.assertTrue(libGraph.stream().mapToDouble(x->x.getDecomposedGraph().getRootNode().getScore()).sum() == coverSols.getScore());
	}
	
	@Test
	public void Test2_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1); 
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1); 
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();
//		Assert.assertTrue(6112.0 == coverSols.getScore());
	}
	
	@Test
	public void Test3_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1); 
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1); 
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();	
		Assert.assertTrue(6112.0 == coverSols.getScore());
		
	}
	
	@Test
	public void Test4_Srlatch_signalMatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NOT_LibSize1); 
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();	
		Assert.assertTrue(coverSols.getScore() == Double.POSITIVE_INFINITY);
		Assert.assertTrue(coverSols.getGateMapping().isEmpty());
	}
	
	@Test
	public void Test5_Srlatch_crosstalk() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();	
		Assert.assertTrue(coverSols.getScore() == Double.POSITIVE_INFINITY);
		Assert.assertTrue(coverSols.getGateMapping().isEmpty());
	}
	
	@Test
	public void Test6_Srlatch() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.OR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.NAND_LibSize1);
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();	
		Assert.assertTrue(coverSols.getScore() == Double.POSITIVE_INFINITY);
		Assert.assertTrue(coverSols.getGateMapping().isEmpty());
	}
	
	@Test
	public void Test7_Srlatch_preselect() throws SBOLTechMapException, GateGenerationExeception, SBOLValidationException, IOException, SBOLConversionException, GeneticGatesException { 
		SBOLTechMapOptions setupOpt = new SBOLTechMapOptions();
		setupOpt.addLibraryFile(TestingFiles.lacINOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.tetRNOR_LibSize1);
		setupOpt.addLibraryFile(TestingFiles.yfp1NOT_LibSize1); 
		setupOpt.addLibraryFile(TestingFiles.yfp2NOT_LibSize1); ;
		setupOpt.setSpecificationFile(TestingFiles.sbolDecompSRLatch_File);
		List<GeneticGate> libGraph = TechMapUtility.createLibraryGraphFromSbolList(setupOpt.getLibrary());
		DecomposedGraph specGraph = TechMapUtility.createSpecificationGraphFromSBOL(setupOpt.getSpefication());
		
		DecomposedGraphNode q_input = specGraph.getNodeByFunctionalComponent(URI.create("http://dummy.org/r_s_q_net/FC3__0_/1.0"));
		q_input.setPreselectedComponentDefinition(URI.create("https://synbiohub.programmingbiology.org/public/Eco1C1G1T1/LacI_protein/1"));
		
		Match m = new PreSelectedMatch(specGraph, libGraph);
		Cover c = new Cover(m);
		TechMapSolution coverSols = c.branchAndBoundCover();
		Assert.assertTrue(6112.0 == coverSols.getScore());
	}
}
